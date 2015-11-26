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
import edu.sjsu.cohort6.openstack.server.job.JobConstants;
import edu.sjsu.cohort6.openstack.server.job.JobManager;
import edu.sjsu.cohort6.openstack.server.job.QuotaCollectorJob;
import edu.sjsu.cohort6.openstack.server.route.quota.QuotaGetRoute;
import edu.sjsu.cohort6.openstack.server.route.service.ServiceDeleteRoute;
import edu.sjsu.cohort6.openstack.server.route.service.ServiceGetRoute;
import edu.sjsu.cohort6.openstack.server.route.service.ServiceNameGetRoute;
import edu.sjsu.cohort6.openstack.server.route.service.ServicePostRoute;
import edu.sjsu.cohort6.openstack.server.route.task.TaskGetRoute;
import edu.sjsu.cohort6.openstack.server.view.MainView;
import lombok.extern.java.Log;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;

import static edu.sjsu.cohort6.openstack.server.HttpConstants.*;
import static spark.Spark.*;

/**
 * The start point of application. It initializes everything, db to webserver and launches the
 * embedded spark server.
 *
 * @author rwatsh on 11/15/15.
 */
@Log
public class SparkServer {

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
            staticFileLocation(WWW_DIR);

            //before(new BasicAuthenticationFilter("/api/*",
            // new AuthenticationDetails("expected-username", "expected-password")));

            String tenant = props.getProperty("tenant");
            String user = props.getProperty("user");
            String password = props.getProperty("password");

            AuthenticationDetails authDetails = new AuthenticationDetails(tenant, user, password);

            // initialize route handlers.
            log.info("POST " + VIRTAPP_API_V1_0_SERVICES + " handler added");
            post(VIRTAPP_API_V1_0_SERVICES, new ServicePostRoute(dbClient, authDetails));
            log.info("DELETE " + VIRTAPP_API_V1_0_SERVICE_NAME + " handler added");
            delete(VIRTAPP_API_V1_0_SERVICE_NAME, new ServiceDeleteRoute(dbClient, authDetails));
            log.info("GET " + VIRTAPP_API_V1_0_SERVICES + " handler added");
            get(VIRTAPP_API_V1_0_SERVICES, new ServiceGetRoute(user, password, tenant, dbClient));
            log.info("GET " + VIRTAPP_API_V1_0_SERVICE_NAME + " handler added");
            get(VIRTAPP_API_V1_0_SERVICE_NAME, new ServiceNameGetRoute(user, password, tenant, dbClient));

            log.info("GET " + VIRTAPP_API_V1_0_TASKS + " handler added");
            get(VIRTAPP_API_V1_0_TASKS, new TaskGetRoute(user, password, tenant, dbClient));

            // Start quota collection
            String sshUser = props.getProperty("ssh_user", "root");
            String sshPassword = props.getProperty("ssh_password", "CMPE-283");
            String sshHost = props.getProperty("ssh_host", "localhost");
            String openStackComputeHost = props.getProperty("openstack_compute_host", "CMPE-OPENSTACK.localdomain");
            scheduleQuotaCollectorJob(dbClient, tenant, user, sshUser, sshPassword, sshHost, openStackComputeHost);

            log.info("GET " + VIRTAPP_API_V1_0_QUOTA + " handler added");
            get(VIRTAPP_API_V1_0_QUOTA, new QuotaGetRoute(user, tenant, dbClient));

            enableCORS("*", "*", "*");

            // initialize the views.
            MainView view = new MainView(user, password, tenant, dbClient, sshUser, sshPassword, sshHost);

        } catch (Exception e) {
            halt(HTTP_INTERNAL_ERR, "Internal error occurred on server, exception is: " + e.toString());
        }
    }

    public static void scheduleQuotaCollectorJob(DBClient dbClient, String tenant, String user, String sshUser, String sshPassword, String sshHost, String openStackComputeHost) throws SchedulerException {
        JobDataMap params = new JobDataMap();
        params.put(JobConstants.SSH_USER, sshUser);
        params.put(JobConstants.SSH_PASSWORD, sshPassword);
        params.put(JobConstants.SSH_HOST, sshHost);
        params.put(JobConstants.DB_CLIENT, dbClient);
        params.put(JobConstants.TENANT_NAME, tenant);
        params.put(JobConstants.USER, user);
        params.put(JobConstants.OPENSTACK_COMPUTE_HOST, openStackComputeHost);
        int intervalInMins = 5; // 5 mins
        JobManager.getInstance().scheduleJob(QuotaCollectorJob.class, JobConstants.QUOTA_JOB, tenant, params, intervalInMins);
    }

    private void initJobManager() throws SchedulerException {
        /*
         * Start the job scheduler.
         */
        JobManager jobManager = JobManager.getInstance();
        AllJobsListener jobListener = new AllJobsListener("AllJobListener");
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

    // Cross origin resource sharing filter.
    private static void enableCORS(final String origin, final String methods, final String headers) {
        before(new Filter() {
            @Override
            public void handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", origin);
                response.header("Access-Control-Request-Method", methods);
                response.header("Access-Control-Allow-Headers", headers);
            }
        });
    }

}
