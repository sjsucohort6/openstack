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

package edu.sjsu.cohort6.openstack.server.view;


import edu.sjsu.cohort6.openstack.client.OpenStack4JClient;
import edu.sjsu.cohort6.openstack.client.OpenStackInterface;
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.model.Task;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.db.mongodb.TaskDAO;
import org.openstack4j.model.network.Network;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;

/**
 * Main view handler.
 *
 * @author rwatsh on 11/19/15.
 */
public class MainView {

    private final String user;
    private final String password;
    private final String tenant;
    private final DBClient dbClient;

    public MainView(String user, String password, String tenant, DBClient dbClient) {
        this.dbClient = dbClient;
        this.user = user;
        this.password = password;
        this.tenant = tenant;

        FreeMarkerEngine templateEngine = ResourceUtils.getFreeMarkerEngine();


        get("/openstack/index.ftl", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();

            ServiceDAO serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
            TaskDAO taskDAO = (TaskDAO) dbClient.getDAO(TaskDAO.class);

            // For services table
            List<Service> services = serviceDAO.fetchById(null);
            attributes.put("services", services);

            // For add service form
            OpenStackInterface osClient = new OpenStack4JClient(user, password, tenant);
            List<? extends Network> allNetworks = osClient.getAllNetworks();
            List<String> networks = new ArrayList<String>();
            for (Network network: allNetworks) {
                networks.add(network.getName());
            }
            attributes.put("networks", networks);

            // For tasks table
            List<Task> tasks = taskDAO.fetchById(null);
            attributes.put("tasks", tasks);

            return new ModelAndView(attributes, "index.ftl");
        }, templateEngine);
    }


}
