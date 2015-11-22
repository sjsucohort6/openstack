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

import edu.sjsu.cohort6.openstack.db.DBClient;
import lombok.extern.java.Log;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * @author rwatsh on 11/15/15.
 */
@Log
public class BaseServiceJob {
    protected void deleteServiceJob(String serviceName, String user, String password, String tenantName, DBClient dbClient) {
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
            log.log(Level.SEVERE, "Error occurred while delete service", e1);
        }
    }



    public static String getVMName(JobDataMap jobDataMap, String serviceName) {
        int vmId = jobDataMap.getInt(JobConstants.VM_ID);
        return MessageFormat.format(JobConstants.WEB_VM_NAME_FORMAT, serviceName, vmId);
    }
}
