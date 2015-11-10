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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sjsu.cohort6.openstack.common.api.ValidationException;
import edu.sjsu.cohort6.openstack.job.CreateBasicServiceJob;
import edu.sjsu.cohort6.openstack.job.JobConstants;
import edu.sjsu.cohort6.openstack.job.JobManager;
import edu.sjsu.cohort6.openstack.job.MyJobListener;
import edu.sjsu.cohort6.openstack.payload.ServicePayload;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.text.MessageFormat;

import static edu.sjsu.cohort6.openstack.common.dto.CommonUtils.convertObjectToJson;
import static edu.sjsu.cohort6.openstack.job.JobConstants.CREATE_BASIC_SERVICE;
import static spark.Spark.*;

/**
 * @author rwatsh on 11/4/15.
 */
public class Application {

    private static final int HTTP_BAD_REQUEST = 400 ;
    private static JobManager jobManager;

    public static void main(String[] args) throws SchedulerException {
        appInit();


        /**
         * Start service
         */
        post("/services", (request, response) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ServicePayload servicePayload = mapper.readValue(request.body(), ServicePayload.class);
                servicePayload.isValid();

                JobDataMap params = new JobDataMap();
                params.put(JobConstants.SERVICE_PAYLOAD, servicePayload);
                jobManager.scheduleJob(CreateBasicServiceJob.class, CREATE_BASIC_SERVICE, servicePayload.getTenantName(), params);
                response.status(201);
                return MessageFormat.format("Service {0} created!", servicePayload.getName());
            } catch (JsonParseException | ValidationException e) {
                response.status(HTTP_BAD_REQUEST);
                return e.getMessage();
            }

        });

        /**
         * Handle after any request to set the content type of the returned response to application/json.
         */
        after((req, res) -> {
            res.type("application/json");
        });

        /**
         * Handle any exception filter.
         */
        exception(Exception.class, (e, req, res) -> {
            res.status(400);
            try {
                res.body(convertObjectToJson(e));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

    }

    /**
     * Initialize application.
     *
     */
    private static void appInit() throws SchedulerException {
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
        /*
         * Start the job scheduler.
         */
        jobManager = JobManager.getInstance();
        MyJobListener jobListener = new MyJobListener("MyJobListener");
        jobManager.registerJobListener(jobListener);
        jobManager.startScheduler();
    }
}
