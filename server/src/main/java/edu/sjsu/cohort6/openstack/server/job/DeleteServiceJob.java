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
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.model.ServiceStatus;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import lombok.extern.java.Log;
import org.openstack4j.model.network.Network;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Service deletion job. It deletes the service and its constituents in the background.
 *
 * @author rwatsh on 11/14/15.
 */
@Log
public class DeleteServiceJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            String user = jobDataMap.getString(JobConstants.USER);
            String password = jobDataMap.getString(JobConstants.PASSWORD);
            String tenantName = jobDataMap.getString(JobConstants.TENANT_NAME);
            DBClient dbClient = (DBClient) jobDataMap.get(JobConstants.DB_CLIENT);
            String serviceName = jobDataMap.getString(JobConstants.SERVICE_NAME);
            OpenStack4JClient client = new OpenStack4JClient(user, password, tenantName);

            ServiceDAO serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
            serviceDAO.updateServiceStatus(serviceName, ServiceStatus.TERMINATING);
            serviceDAO.updateServiceLog(serviceName, "Service " + serviceName + " is being terminated");
            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(serviceName);
            }});
            if (services != null && !services.isEmpty()) {
                Service service = services.get(0);

                // delete servers
                client.deleteServers(service);
                // delete network
                String networkName = service.getNetworkName();
                Network network = client.getNetworkByName(networkName);
                client.deleteNetwork(network);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Deletion of service failed", e);
            throw new JobExecutionException(e);
        }
    }
}
