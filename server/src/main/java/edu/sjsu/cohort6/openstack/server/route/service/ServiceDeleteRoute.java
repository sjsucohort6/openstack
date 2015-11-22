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

package edu.sjsu.cohort6.openstack.server.route.service;

import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.server.HttpConstants;
import edu.sjsu.cohort6.openstack.server.filter.AuthenticationDetails;
import edu.sjsu.cohort6.openstack.server.job.BaseServiceJob;
import edu.sjsu.cohort6.openstack.server.job.JobManager;
import org.quartz.SchedulerException;
import spark.Request;
import spark.Response;
import spark.Route;

import static edu.sjsu.cohort6.openstack.server.HttpConstants.HTTP_BAD_REQUEST;

/**
 * @author rwatsh on 11/15/15.
 */
public class ServiceDeleteRoute extends BaseServiceJob implements Route {
    private DBClient dbClient;
    private ServiceDAO serviceDAO;
    private JobManager jobManager;
    private String tenant;
    private String user;
    private String password;

    public ServiceDeleteRoute(DBClient dbClient, AuthenticationDetails authenticationDetails) throws SchedulerException {
        this.dbClient = dbClient;
        serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
        jobManager = JobManager.getInstance();
        this.tenant = authenticationDetails.tenant;
        this.password = new String(authenticationDetails.password);
        this.user = authenticationDetails.userName;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            String serviceName = request.params(":serviceName");
            deleteServiceJob(serviceName, user, password, tenant, dbClient);
            response.status(HttpConstants.HTTP_OK);
            response.type(HttpConstants.APPLICATION_JSON);
            return "Deletion scheduled for service " + serviceName;
        } catch (Exception e) {
            response.status(HTTP_BAD_REQUEST);
            return e.toString();
        }
    }
}
