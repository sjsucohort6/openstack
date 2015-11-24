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

import edu.sjsu.cohort6.openstack.client.SshClient;
import edu.sjsu.cohort6.openstack.common.model.Quota;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.QuotaDAO;
import lombok.extern.java.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Quota info will be collected periodically and persisted in DB.
 *
 * @author rwatsh on 11/23/15.
 */
@Log
public class QuotaCollectorJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // Collect Quota
        JobDataMap params = jobExecutionContext.getMergedJobDataMap();
        String user = params.getString(JobConstants.SSH_USER);
        String password = params.getString(JobConstants.SSH_PASSWORD);
        String host = params.getString(JobConstants.SSH_HOST);
        String tenant = params.getString(JobConstants.TENANT_NAME);
        String openStackUser = params.getString(JobConstants.USER);
        DBClient dbClient = (DBClient) params.get(JobConstants.DB_CLIENT);
        JobHelper jobHelper = new JobHelper(dbClient);

        try {
            SshClient sshClient = new SshClient();
            String command = MessageFormat.format(JobConstants.QUOTA_SHOW_CMD, tenant);
            log.info("Executing ssh command : " + command);

            List<String> actual = sshClient.executeCommand(user, password, host, JobConstants.SSH_PORT, command);
            StringBuilder sb = new StringBuilder();
            for (String s: actual) {
                sb.append(s).append("\n");
            }

            log.info("SSH response: " + sb.toString());
            Quota quota = new Quota();
            quota.setTenant(tenant);
            quota.setUser(openStackUser);
            quota.setQuota(sb.toString());
            QuotaDAO dao = (QuotaDAO) dbClient.getDAO(QuotaDAO.class);

            dao.addOrUpdate(new ArrayList<Quota>(){{add(quota);}});
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting quota for tenant " + tenant, e);
            jobHelper.saveTaskInfo(jobExecutionContext,"Error in getting quota for tenant " + tenant + ". Error: " + e.toString());
            throw new JobExecutionException(e);
        }
    }
}
