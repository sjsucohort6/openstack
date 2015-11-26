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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * Quota info will be collected periodically and persisted in DB.
 *
 * @author rwatsh on 11/23/15.
 */
@Log
public class QuotaCollectorJob implements Job {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final long ONE_DAY_IN_MILLISEC = 1000 * 60 * 60 * 24;

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
        String openStackComputeHost = params.getString(JobConstants.OPENSTACK_COMPUTE_HOST);
        JobHelper jobHelper = new JobHelper(dbClient);

        try {
            SshClient sshClient = new SshClient();
            Date today = new Date();
            String endDate = new SimpleDateFormat(DATE_PATTERN).format(today); // today
            Date aWeekAgo = new Date(today.getTime() - (7 * ONE_DAY_IN_MILLISEC));
            String startDate = new SimpleDateFormat(DATE_PATTERN).format(aWeekAgo); // a week ago
            String command = MessageFormat.format(JobConstants.USAGE_LIST_CMD, startDate, endDate);
            log.info("Executing ssh command : " + command);

            List<String> linesList = sshClient.executeCommand(user, password, host, JobConstants.SSH_PORT, command);
            StringBuilder usageListStrBuilder = new StringBuilder();
            StringBuilder meteringData = new StringBuilder();
            double chargeBack = 0.0;
            int i = 0;
            for (String line: linesList) {
                usageListStrBuilder.append(line).append("\n");
                if (i == 4) {
                    // Get the first usage line from the list - assuming this is the only tenant we have.
                    List<String> tokens = tokenize(line);
                    String cpuHours = tokens.get(3);
                    double cpuHoursVal = Double.parseDouble(cpuHours);
                    double costCpuHours = 0.1*cpuHoursVal;
                    meteringData.append("Charges for this week:\n")
                            .append("Based on total CPU hours: [").append(cpuHours).append("] is (@ $0.1 per CPU Hour rate): $").append(costCpuHours);
                }
                i++;
            }

            log.info("SSH response: " + usageListStrBuilder.toString());
            command = MessageFormat.format(JobConstants.RESOURCE_USAGE_CMD, openStackComputeHost);
            linesList = sshClient.executeCommand(user, password, host, JobConstants.SSH_PORT, command);
            StringBuilder resourceUsageSb = new StringBuilder("Resource usage on the compute node:[" + openStackComputeHost + "] is:\n");
            for (String line: linesList) {
                resourceUsageSb.append(line).append("\n");
            }
            String quotaStr = usageListStrBuilder.toString() + "\n"
                    + resourceUsageSb.toString() + "\n" + "\n"
                    + meteringData.toString();
            Quota quota = new Quota();
            quota.setTenant(tenant);
            quota.setUser(openStackUser);
            quota.setQuota(quotaStr);
            QuotaDAO dao = (QuotaDAO) dbClient.getDAO(QuotaDAO.class);

            dao.addOrUpdate(new ArrayList<Quota>(){{add(quota);}});
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting quota for tenant " + tenant, e);
            jobHelper.saveTaskInfo(jobExecutionContext,"Error in getting quota for tenant " + tenant + ". Error: " + e.toString());
            throw new JobExecutionException(e);
        }
    }

    private List<String> tokenize(String usageLine) {
        StringTokenizer st = new StringTokenizer(usageLine, "|");
        List<String> tokens = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            tokens.add(token);
        }
        return tokens;
    }
}
