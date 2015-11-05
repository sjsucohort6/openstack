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

package edu.sjsu.cohort6.openstack.web;

import edu.sjsu.cohort6.openstack.OpenStack4JClient;

import static spark.Spark.*;

/**
 * @author rwatsh on 11/4/15.
 */
public class Application {

    public static void main(String[] args) {
        appInit();

        // RegisterResource resources

        get("/debug", (request, response) -> {
            OpenStack4JClient.testOpenstack();
            return "Hello to OpenStack Debugging";
        });

    }

    /**
     * Initialize application.
     *
     */
    private static void appInit() {
        //port(9090);
        // Configure that static files directory.
        staticFileLocation("/");
        /*Module module = new DatabaseModule();
        MainController mainController = new MainController();
        Guice.createInjector(module).injectMembers(mainController);
        // TODO externalize DB config params
        MainController.client = mainController.getDbFactory().create("localhost", 27017, "crowd_tester_testdb");
*/
        init();
    }
}
