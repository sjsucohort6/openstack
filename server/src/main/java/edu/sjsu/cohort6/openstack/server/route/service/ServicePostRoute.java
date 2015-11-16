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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sjsu.cohort6.openstack.common.model.Node;
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.model.ServiceStatus;
import edu.sjsu.cohort6.openstack.common.util.CommonUtils;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.DBException;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.server.HttpConstants;
import edu.sjsu.cohort6.openstack.server.filter.AuthenticationDetails;
import edu.sjsu.cohort6.openstack.server.job.CreateServiceJob;
import edu.sjsu.cohort6.openstack.server.job.JobConstants;
import edu.sjsu.cohort6.openstack.server.job.JobManager;
import edu.sjsu.cohort6.openstack.server.payload.NodePayload;
import edu.sjsu.cohort6.openstack.server.payload.ServicePayload;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

import static edu.sjsu.cohort6.openstack.server.HttpConstants.HTTP_BAD_REQUEST;
import static edu.sjsu.cohort6.openstack.server.job.JobConstants.*;

/**
 * @author rwatsh on 11/15/15.
 */
public class ServicePostRoute implements Route {

    private DBClient dbClient;
    private ServiceDAO serviceDAO;
    private JobManager jobManager;
    private String tenant;
    private String user;
    private String password;

    public ServicePostRoute(DBClient dbClient, AuthenticationDetails authenticationDetails) throws SchedulerException {
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
            ObjectMapper mapper = new ObjectMapper();
            ServicePayload createServicePayload = CommonUtils.convertJsonToObject(request.body(), ServicePayload.class);
            if (!createServicePayload.isValid()) {
                response.status(HTTP_BAD_REQUEST);
                return "Service payload is not valid";
            }
            JobDataMap params = new JobDataMap();
            params.put(SERVICE_PAYLOAD, createServicePayload);
            params.put(USER, user);
            params.put(PASSWORD, password);
            params.put(TENANT_NAME, tenant);
            params.put(JobConstants.DB_CLIENT, dbClient);

            final String serviceName = createServicePayload.getName();
            String jobName = CREATE_SERVICE_JOB + "-" + serviceName;

            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(serviceName);
            }});

            /*
             * If service is not already existing in DB or no jobs for the service exists that is currently executing
             * then proceed with the service creation (schedule a service creation job).
             */
            if (!jobManager.findJob(jobName, tenant) && (services == null || services.isEmpty())) {
                JobDetail jobDetail = jobManager.scheduleJob(CreateServiceJob.class, jobName, tenant, params);

                // Add service to DB.
                addServiceToDB(createServicePayload, serviceName);

                response.status(HttpConstants.HTTP_OK);
                response.type(HttpConstants.APPLICATION_JSON);
                return CommonUtils.convertObjectToJson(jobDetail.getKey().getName());
            } else {
                throw new IllegalArgumentException("A job for this service " + serviceName + " is already existing.");
            }
        } catch (Exception e) {
            response.status(HTTP_BAD_REQUEST);
            return e.toString();
        }
    }

    public void addServiceToDB(ServicePayload createServicePayload, String serviceName) throws DBException {
        Service service = new Service();
        service.setTenant(tenant);
        service.setStatus(ServiceStatus.IN_PROGRESS);
        /*ServiceLog serviceLog = new ServiceLog();
        *//*serviceLog.setMessage("Starting to provision service");
        serviceLog.setTime(new Date());*//*
        service.setLogs(new ArrayList<ServiceLog>(){{add(serviceLog);}});*/
        service.setName(serviceName);
        service.setNetworkName(createServicePayload.getNetworkName());
        service.setServiceType(createServicePayload.getServiceType());
        List<Node> nodes = new ArrayList<>();
        List<NodePayload> nodePayloads = createServicePayload.getNodes();
        for (NodePayload nodePayload: nodePayloads) {
            Node node = new Node();
            node.setFlavorName(nodePayload.getFlavorName());
            node.setImageName(nodePayload.getImageName());
            node.setType(nodePayload.getType());
        }
        service.setNodes(nodes);
        serviceDAO.add(new ArrayList<Service>(){{add(service);}});
    }
}
