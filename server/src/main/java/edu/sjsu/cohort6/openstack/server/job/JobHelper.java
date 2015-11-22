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

import edu.sjsu.cohort6.openstack.common.model.Task;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.DBException;
import edu.sjsu.cohort6.openstack.db.mongodb.TaskDAO;
import lombok.extern.java.Log;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Log
public class JobHelper {
    private final DBClient dbClient;
    private TaskDAO taskDAO;

    public JobHelper(DBClient dbClient) {
        this.dbClient = dbClient;
        this.taskDAO = (TaskDAO) dbClient.getDAO(TaskDAO.class);
    }

    /**
     * Save the job execution states with details on any exception that might have occurred during the execution.
     *
     * @param context
     * @param msg
     */
    public void saveTaskInfo(JobExecutionContext context, String msg) {
        Task task = new Task();
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        Map<String, String> params = new HashMap<String, String>();

        Map<String, Object> wrappedMap = jobDataMap.getWrappedMap();
        for (Map.Entry<String, Object> jobDataEntry : wrappedMap.entrySet()) {
            params.put(jobDataEntry.getKey(), jobDataEntry.getValue().toString());
        }
        task.setJobDataMap(params);
        task.setJobName(jobDetail.getKey().getName());
        task.setStartTime(new Date());
        task.setMessage(msg);
        String tenant = jobDataMap.getString(JobConstants.TENANT_NAME);
        task.setTenantName(tenant);
        task.setTriggerName(context.getTrigger().getKey().getName());

        try {
            // Always add as a new task
            taskDAO.add(new ArrayList<Task>() {{
                add(task);
            }});
        } catch (DBException e) {
            log.severe(e.toString());
        }
    }
}