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

package edu.sjsu.cohort6.openstack;

import edu.sjsu.cohort6.openstack.auth.SimpleAuthenticator;
import edu.sjsu.cohort6.openstack.auth.User;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.health.DBHealthCheck;
import edu.sjsu.cohort6.openstack.job.AllJobsListener;
import edu.sjsu.cohort6.openstack.job.JobManager;
import edu.sjsu.cohort6.openstack.rest.EndpointUtils;
import edu.sjsu.cohort6.openstack.rest.ServiceResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * OpenStack cloud service application.
 *
 * Created by rwatsh on 9/14/15.
 */
public class OpenStackCloudServiceApplication extends Application<OpenStackCloudServiceConfiguration> {

    private DBClient dbClient;
    private JobManager jobManager;
    private String tenant;


    public static void main(final String[] args) throws Exception {
        new OpenStackCloudServiceApplication().run(args);

    }

    @Override
    public String getName() {
        return "OpenStackCloudService";
    }

    @Override
    public void initialize(final Bootstrap<OpenStackCloudServiceConfiguration> bootstrap) {
        /*
         * Register the static html contents to be served from /assets directory and accessible from browser from
         * http://<host>:<port>/openstack
         */
        bootstrap.addBundle(new AssetsBundle("/assets", "/openstack", "index.html"));
    }

    @Override
    public void run(OpenStackCloudServiceConfiguration OpenStackCloudServiceConfiguration, Environment environment) throws Exception {
        dbClient = OpenStackCloudServiceConfiguration.getDbConfig().build(environment);
        tenant = OpenStackCloudServiceConfiguration.getOpenStackConfig().build(environment);

        /*
         * Setup basic authentication against DB table.
         */
        Authenticator<BasicCredentials, User> simpleAuthenticator = new SimpleAuthenticator(tenant);
        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<User>(simpleAuthenticator,
                "openstack-cloud", // realm name
                User.class))); // backing object

        environment.healthChecks().register("database", new DBHealthCheck(dbClient));

        /*
         * Start the job scheduler.
         */
        jobManager = JobManager.getInstance();
        AllJobsListener jobListener = new AllJobsListener("OpenStackJobListener");
        jobManager.registerJobListener(jobListener);
        jobManager.startScheduler();

        /*
         * Register resources with jersey.
         */
        final ServiceResource serviceResource = new ServiceResource(dbClient, jobManager);

        /*
         * Setup jersey environment.
         */
        environment.jersey().setUrlPattern(EndpointUtils.ENDPOINT_ROOT + "/*");
        environment.jersey().register(serviceResource);
        //environment.jersey().register(new CheckRequestFilter());
        //environment.jersey().register(new ResponseFilter());
        /*
         * Setup CORS filter
         */
        //environment.jersey().register(new CORSFilter());

    }
}
