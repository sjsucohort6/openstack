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

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A listener for Job which logs the job status in service_logs collection in DB.
 *
 * @author rwatsh on 11/5/15.
 */
public class AllJobsListener extends JobListenerSupport {
    private String name = null;
    private static final Logger LOGGER = Logger.getLogger(AllJobsListener.class.getName());

    public AllJobsListener(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
        // do something with the event
        JobDetail jobDetail = context.getJobDetail();
        String jobParams = jobDetail.getJobDataMap().toString();
        // TODO persist these messages in service_logs collection
        LOGGER.info(MessageFormat.format("Job {0} executed with params {1}", jobDetail.getKey().getName(), jobParams));
        if (jobException != null) {
            LOGGER.log(Level.WARNING,
                    MessageFormat.format("Job {0}, raised exception: {1}",
                            jobDetail.getKey().getName(), jobException.getCause()),
                    jobException);
        }
    }
}
