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

package edu.sjsu.cohort6.openstack.rest;

import edu.sjsu.cohort6.openstack.auth.User;
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.util.CommonUtils;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.DBException;
import edu.sjsu.cohort6.openstack.job.CreateServiceJob;
import edu.sjsu.cohort6.openstack.job.DeleteServiceJob;
import edu.sjsu.cohort6.openstack.job.JobConstants;
import edu.sjsu.cohort6.openstack.job.JobManager;
import edu.sjsu.cohort6.openstack.rest.exception.InternalErrorException;
import edu.sjsu.cohort6.openstack.rest.exception.ResourceNotFoundException;
import io.dropwizard.auth.Auth;
import lombok.extern.java.Log;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static edu.sjsu.cohort6.openstack.job.JobConstants.CREATE_SERVICE_JOB;

/**
 * Services endpoint. This endpoint provides interface for creating, updating, reading and deleting services on openstack
 * cloud.
 *
 * @author rwatsh on 11/10/15.
 */
@Log
@Path(EndpointUtils.ENDPOINT_ROOT + "/{tenant}/services")
public class ServiceResource extends BaseResource<Service> {

    public ServiceResource(DBClient client, JobManager jobManager) {
        super(client, jobManager);
    }

    /**
     * Used to validate the user's creds. If the user is valid then framework will let this method
     * be invoked where we simply return 200.
     * <p>
     * Every subsequent user request will also need to have the same auth creds as server wont maintain any session.
     *
     * @param user
     * @param info
     * @return
     */
    @HEAD
    public Response login(@Auth User user, @Context UriInfo info) throws DBException {
        return Response.ok().build();
    }

    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Auth User user, @Valid String modelJson, @Context UriInfo info, @PathParam("tenant") String tenant) {
        try {
            Service createServicePayload = CommonUtils.convertJsonToObject(modelJson, Service.class);
            createServicePayload.isValid();

            JobDataMap params = new JobDataMap();
            params.put(JobConstants.SERVICE_PAYLOAD, createServicePayload);
            params.put(JobConstants.USER, user.getUserName());
            params.put(JobConstants.PASSWORD, user.getPassword());

            final String serviceName = createServicePayload.getName();
            String jobName = CREATE_SERVICE_JOB + "-" + serviceName;

            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(serviceName);
            }});

            /*
             * If service is not already existing in DB or no jobs for the service exists that is currently executing
             * then proceed with the service creation (schedule a service creation job).
             */
            if (!jobManager.findJob(jobName, tenant) && (services == null)) {
                JobDetail jobDetail = jobManager.scheduleJob(CreateServiceJob.class, jobName, tenant, params);
                URI uri = UriBuilder.fromResource(ServiceResource.class).build(jobDetail);
                return Response.created(uri)
                        .entity(Entity.json(jobDetail))
                        .build();
            } else {
                throw new IllegalArgumentException("A job for this service " + serviceName + " is already existing.");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in adding service", e);
            throw new BadRequestException(e);
        }
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Service> list(@Auth User user, @QueryParam("filter") String filter, @PathParam("tenant") String tenant) throws InternalErrorException {
        try {
            return serviceDAO.fetchById(null);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting services for tenant " + tenant, e);
            throw new BadRequestException(e);
        }
    }

    @Override
    @GET
    @Path("/{id}")
    public Service retrieve(@Auth User user, @PathParam("id") String id, @PathParam("tenant") String tenant) throws ResourceNotFoundException, InternalErrorException {
        try {
            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(id);
            }});
            return services != null && !services.isEmpty() ? services.get(0) : null;

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting service" + id +  " for tenant " + tenant, e);
            throw new BadRequestException(e);
        }
    }

    @Override
    @PUT
    @PathParam("/{id}")
    public Service update(@Auth User user, @PathParam("id") String id, @Valid String entity, @PathParam("tenant") String tenant) throws ResourceNotFoundException, InternalErrorException, IOException {
        /*try {
            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(id);
            }});
            Service s =  services != null && !services.isEmpty() ? services.get(0) : null;

            if (s != null) {
                // parse the entity and perform update on s
                serviceDAO.update(new ArrayList<Service>(){{add(s);}});
                return s;
            } else {
                throw new ResourceNotFoundException();
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting service" + id +  " for tenant " + tenant, e);
            throw new BadRequestException(e);
        }*/

        // TODO will be a strecht goal to support update for a service.
        return null;
    }

    @Override
    @DELETE
    @PathParam("/{id}")
    public Response delete(@Auth User user, @PathParam("id") String id, @PathParam("tenant") String tenant) throws ResourceNotFoundException, InternalErrorException {
        try {
            List<Service> services = serviceDAO.fetchById(new ArrayList<String>() {{
                add(id);
            }});

            Service s =  services != null && !services.isEmpty() ? services.get(0) : null;
            if (s != null) {
                log.info("Service " + id + " being deleted.");
                // TODO call delete service job to delete service components from openstack.
                JobDataMap params = new JobDataMap();
                params.put(JobConstants.SERVICE_NAME, id);
                params.put(JobConstants.TENANT_NAME, tenant);
                params.put(JobConstants.USER, user.getUserName());
                params.put(JobConstants.PASSWORD, user.getPassword());
                params.put(JobConstants.DB_CLIENT, dbClient);
                String jobName = "delete-service-job" + "-" + id;
                jobManager.scheduleJob(DeleteServiceJob.class, jobName, tenant, params);
                serviceDAO.remove(new ArrayList<String>(){{add(id);}});
                log.info("Service " + id + " deleted successfully.");
                return Response.ok().build();
            } else {
                throw new ResourceNotFoundException();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in deleting service" + id +  " for tenant " + tenant, e);
            throw new BadRequestException(e);
        }
    }
}
