/*
 * Copyright (c) 2015 San Jose State University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */

package edu.sjsu.cohort6.openstack.job;

import edu.sjsu.cohort6.openstack.OpenStack4JClient;
import edu.sjsu.cohort6.openstack.common.model.Node;
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.db.DBClient;
import org.quartz.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * A job to create a basic service. This job will create VM(s) in the background.
 *
 * @author rwatsh on 11/5/15.
 */
public class CreateServiceJob implements Job  {
    private static final Logger LOGGER = Logger.getLogger(CreateServiceJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String serviceName = null;
        String tenantName = null;
        DBClient dbClient = null;
        String user = null;
        String password = null;

        try {
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            Service createServicePayload = (Service) jobDataMap.get(JobConstants.SERVICE_PAYLOAD);
            if (createServicePayload != null) {
                serviceName = createServicePayload.getName();

                user = jobDataMap.getString(JobConstants.USER);
                password = jobDataMap.getString(JobConstants.PASSWORD);
                tenantName = jobDataMap.getString(JobConstants.TENANT_NAME);
                dbClient = (DBClient) jobDataMap.get(JobConstants.DB_CLIENT);
                OpenStack4JClient client = new OpenStack4JClient(user, password, tenantName);
                List<Node> vmPayloads = createServicePayload.getNodes();

                if (vmPayloads != null) {
                    JobManager jobManager = JobManager.getInstance();
                    int num = 0;
                    for (Node vmPayload: vmPayloads) {
                        // Spawn child jobs for each VM so all VMs can be created in parallel
                        JobDataMap params = new JobDataMap();
                        params.put(JobConstants.VM_PAYLOAD, vmPayload);
                        params.put(JobConstants.SERVICE_NAME, serviceName);
                        params.put(JobConstants.OPENSTACK_CLIENT, client);
                        String jobName = JobConstants.CREATE_VM_JOB + "-" + serviceName + "-" + num++;
                        jobManager.scheduleJob(CreateVMJob.class, jobName, tenantName, params);

                        // Attach job listener for tracking the job and updating service metadata in DB.
                        jobManager.registerJobListener(new OpenStackJobListener(dbClient), jobName, tenantName);
                    }
                } else {
                    throw new OpenStackJobException("At least one node should be specified in Service payload.");
                }
            } else {
                throw new OpenStackJobException("Service not specified");
            }
            // TODO add health check for web server and DB server with retries.


        } catch (Exception e) {
            JobManager jobManager = null;
            // provisioning service failed, delete the service
            try {
                jobManager = JobManager.getInstance();
                if (serviceName != null && tenantName != null && dbClient != null && user != null && password != null) {
                    JobDataMap params = new JobDataMap();
                    params.put(JobConstants.SERVICE_NAME, serviceName);
                    params.put(JobConstants.TENANT_NAME, tenantName);
                    params.put(JobConstants.USER, user);
                    params.put(JobConstants.PASSWORD, password);
                    params.put(JobConstants.DB_CLIENT, dbClient);
                    jobManager.scheduleJob(DeleteServiceJob.class, "delete-service-job-" + serviceName, tenantName, params);
                }
            } catch (SchedulerException e1) {
                e1.printStackTrace();
            }

            throw new JobExecutionException(e);
        }
    }

}
