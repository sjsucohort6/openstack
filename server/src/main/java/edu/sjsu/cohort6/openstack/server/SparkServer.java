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

package edu.sjsu.cohort6.openstack.server;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.DBFactory;
import edu.sjsu.cohort6.openstack.db.DatabaseModule;
import edu.sjsu.cohort6.openstack.server.filter.AuthenticationDetails;
import edu.sjsu.cohort6.openstack.server.job.AllJobsListener;
import edu.sjsu.cohort6.openstack.server.job.JobManager;
import edu.sjsu.cohort6.openstack.server.route.ServicePostRoute;
import lombok.extern.java.Log;
import org.quartz.SchedulerException;

import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;

import static spark.Spark.halt;
import static spark.Spark.post;

/**
 * @author rwatsh on 11/15/15.
 */
@Log
public class SparkServer {

    public static final String VIRTAPP_API_V1_0_SERVICES = "/virtapp/api/v1.0/services";
    @Inject
    private DBFactory dbFactory;

    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("config.properties"));

            SparkServer app = new SparkServer();
            String dbServer = props.getProperty("dbHost", "localhost");
            int dbPort = Integer.parseInt(props.getProperty("dbPort", "27017"));
            String dbName = props.getProperty("dbName", "openstack_db");
            DBClient dbClient = app.initDB(dbServer, dbPort, dbName);
            app.initJobManager();

            //before(new BasicAuthenticationFilter("/api/*",
            // new AuthenticationDetails("expected-username", "expected-password")));

            String tenant = props.getProperty("tenant");
            String user = props.getProperty("user");
            String password = props.getProperty("password");

            AuthenticationDetails authDetails = new AuthenticationDetails(tenant, user, password);

            // initialize route handlers.
            log.info("POST " + VIRTAPP_API_V1_0_SERVICES + " handler added");
            post(VIRTAPP_API_V1_0_SERVICES, new ServicePostRoute(dbClient, authDetails));

        } catch (Exception e) {
            halt(HttpConstants.HTTP_INTERNAL_ERR, "Internal error occurred on server, exception is: " + e.toString());
        }
    }

    private void initJobManager() throws SchedulerException {
        /*
         * Start the job scheduler.
         */
        JobManager jobManager = JobManager.getInstance();
        AllJobsListener jobListener = new AllJobsListener("OpenStackJobListener");
        jobManager.registerJobListener(jobListener);
        jobManager.startScheduler();
    }

    private DBClient initDB(String server, int port, String dbName) {
        log.info(MessageFormat.format("Initializing DB {0}, {1} name {2}", server, port, dbName));
        Module module = new DatabaseModule();
        Guice.createInjector(module).injectMembers(this);
        DBClient dbClient = dbFactory.create(server, port, dbName);
        return dbClient;
    }

}
