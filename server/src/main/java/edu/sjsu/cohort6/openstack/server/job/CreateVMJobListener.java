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

package edu.sjsu.cohort6.openstack.server.job;

import edu.sjsu.cohort6.openstack.client.OpenStack4JClient;
import edu.sjsu.cohort6.openstack.common.model.ServiceStatus;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.DBException;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.server.payload.NodePayload;
import lombok.extern.java.Log;
import org.openstack4j.model.compute.Server;
import org.quartz.*;

/**
 * The job listener will be used to update the service in the DB.
 *
 * @author rwatsh on 11/13/15.
 */
@Log
public class CreateVMJobListener implements JobListener{
    private DBClient dbClient;
    private ServiceDAO serviceDAO;

    public CreateVMJobListener(DBClient dbClient) {
        this.dbClient = dbClient;
        this.serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
    }

    @Override
    public String getName() {
        return "CreateVMJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        String serviceName = jobDetail.getJobDataMap().getString(JobConstants.SERVICE_NAME);
        String msg = "Job to be executed: " + jobDetail.getKey().getName() +
                " for tenant " + jobDetail.getJobDataMap().getString(JobConstants.TENANT_NAME) +
                " for service " + serviceName;
        log.info(msg);

        serviceDAO.updateServiceStatus(serviceName, ServiceStatus.IN_PROGRESS);
        serviceDAO.updateServiceLog(serviceName, msg);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        String serviceName = jobDetail.getJobDataMap().getString(JobConstants.SERVICE_NAME);
        String msg = "Job execution vetoed: " + jobDetail.getKey().getName() +
                " for tenant " + jobDetail.getJobDataMap().getString(JobConstants.TENANT_NAME) +
                " for service " + serviceName;
        log.info(msg);
        //For simplicity lets mark it as failed. We could have had a CANCELED status.
        serviceDAO.updateServiceStatus(serviceName, ServiceStatus.FAILED);
        serviceDAO.updateServiceLog(serviceName, msg);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String serviceName = jobDataMap.getString(JobConstants.SERVICE_NAME);
        String tenant = jobDataMap.getString(JobConstants.TENANT_NAME);
        String msg = "Job completed executing: " + jobDetail.getKey().getName() +
                " for tenant " + tenant +
                " for service " + serviceName;
        log.info(msg);
        // Job completed successfully
        String vmName = BaseServiceJob.getVMName(jobDataMap, serviceName);
        String user = jobDataMap.getString(JobConstants.USER);
        String password = jobDataMap.getString(JobConstants.PASSWORD);
        NodePayload node = (NodePayload) jobDataMap.get(JobConstants.VM_PAYLOAD);
        OpenStack4JClient client = new OpenStack4JClient(user, password, tenant);
        Server s = client.getServerByName(vmName);

        serviceDAO.updateServiceLog(serviceName, msg);
        try {
            serviceDAO.addNode(serviceName, node.getType(), s);
        } catch (DBException e) {
            e.printStackTrace();
        }

        /*if (s.getStatus().value().equalsIgnoreCase("error")) {
            serviceDAO.updateServiceStatus(serviceName, ServiceStatus.FAILED);
            serviceDAO.updateServiceLog(serviceName, MessageFormat.format("VM {0} is in error state, reason: {1}. Please refer to horizon UI for details.", vmName, s.getFault().getMessage()));
        } else {

        }*/
    }
}
