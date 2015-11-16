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

import edu.sjsu.cohort6.openstack.common.model.ServiceStatus;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import lombok.extern.java.Log;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * The job listener will be used to update the service in the DB.
 *
 * @author rwatsh on 11/13/15.
 */
@Log
public class OpenStackJobListener implements JobListener{
    private DBClient dbClient;
    private ServiceDAO serviceDAO;

    public OpenStackJobListener(DBClient dbClient) {
        this.dbClient = dbClient;
        this.serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
    }

    @Override
    public String getName() {
        return "OpenStackJobListener";
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
        String serviceName = jobDetail.getJobDataMap().getString(JobConstants.SERVICE_NAME);
        String msg = "Job completed executing: " + jobDetail.getKey().getName() +
                " for tenant " + jobDetail.getJobDataMap().getString(JobConstants.TENANT_NAME) +
                " for service " + serviceName;
        log.info(msg);
        // Job completed successfully
        serviceDAO.updateServiceStatus(serviceName, ServiceStatus.READY);
        serviceDAO.updateServiceLog(serviceName, msg);
    }
}
