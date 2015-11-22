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


import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.model.Task;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.db.mongodb.TaskDAO;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

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

    private final DBClient dbClient;

    public MainView(DBClient dbClient) {
        this.dbClient = dbClient;

        FreeMarkerEngine templateEngine = ResourceUtils.getFreeMarkerEngine();


        get("/openstack/index.ftl", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();

            ServiceDAO serviceDAO = (ServiceDAO) dbClient.getDAO(ServiceDAO.class);
            TaskDAO taskDAO = (TaskDAO) dbClient.getDAO(TaskDAO.class);

            // For services table
            List<Service> services = serviceDAO.fetchById(null);
            attributes.put("services", services);

            // For tasks table
            List<Task> tasks = taskDAO.fetchById(null);
            attributes.put("tasks", tasks);

            return new ModelAndView(attributes, "index.ftl");
        }, templateEngine);
    }


}
