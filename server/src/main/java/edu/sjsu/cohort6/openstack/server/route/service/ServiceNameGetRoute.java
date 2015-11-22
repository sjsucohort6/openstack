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

import edu.sjsu.cohort6.openstack.client.OpenStack4JClient;
import edu.sjsu.cohort6.openstack.client.OpenStackInterface;
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.util.CommonUtils;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.server.HttpConstants;
import lombok.extern.java.Log;
import org.openstack4j.model.compute.Server;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author rwatsh on 11/16/15.
 */
@Log
public class ServiceNameGetRoute extends BaseServiceRoute implements Route {
    private final String user;
    private final String password;
    private final String tenant;
    private final DBClient dbClient;


    public ServiceNameGetRoute(String user, String password, String tenant, DBClient dbClient) {
        this.user = user;
        this.password = password;
        this.tenant = tenant;
        this.dbClient = dbClient;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        try {
            ServiceDAO serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
            String serviceName = request.params(":serviceName");
            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(serviceName);
            }});
            if (services == null || services.isEmpty()) {
                response.status(HttpConstants.HTTP_NOT_FOUND);
                return "Service " + serviceName + " is not found";
            }

            OpenStackInterface client = new OpenStack4JClient(user, password, tenant);

            List<? extends Server> servers = client.getAllServers();

            for (Server s : servers) {
                serviceDAO.updateNode(s, serviceName);
            }

            updateCumulativeServiceStatus(services.get(0), serviceDAO);
            response.type(HttpConstants.APPLICATION_JSON);
            return CommonUtils.convertObjectToJson(services);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting services for tenant " + tenant, e);
            throw e;
        }
    }
}
