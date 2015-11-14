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
import edu.sjsu.cohort6.openstack.common.model.IModel;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import edu.sjsu.cohort6.openstack.job.JobManager;
import edu.sjsu.cohort6.openstack.rest.exception.InternalErrorException;
import io.dropwizard.auth.Auth;
import io.dropwizard.servlets.assets.ResourceNotFoundException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rwatsh on 9/23/15.
 */
public abstract class BaseResource<T extends IModel> {

    protected DBClient dbClient;
    protected JobManager jobManager;
    protected ServiceDAO serviceDAO;

    public BaseResource(DBClient client, JobManager jobManager) {
        this.dbClient = client;
        this.serviceDAO = (ServiceDAO) client.getDAO(ServiceDAO.class);
        this.jobManager = jobManager;
    }

    /*
     * Note: It is important to also define the @POST, @GET... etc annotations with the implementation methods in the
     * derived classes or else they will not be accounted for by dropwizard framework.
     */

    /**
     * Create the resource.
     *
     * @param modelJson
     * @param info
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    abstract public Response create(@Auth User user, @Valid String modelJson, @Context UriInfo info, @PathParam("tenant") String tenant);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    abstract public List<T> list(@Auth User user, @QueryParam("filter") String filter, @PathParam("tenant") String tenant) throws InternalErrorException;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    abstract public  T retrieve(@Auth User user, @PathParam("id") String id, @PathParam("tenant") String tenant)
            throws ResourceNotFoundException, InternalErrorException;

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    abstract public T update(@Auth User user, @PathParam("id") String id,
                             @Valid String entity, @PathParam("tenant") String tenant) throws ResourceNotFoundException, InternalErrorException, IOException;

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    abstract public Response delete(@Auth User user, @PathParam("id") String id, @PathParam("tenant") String tenant)
            throws ResourceNotFoundException, InternalErrorException;

    /**
     * Common methods
     */

    protected List<String> getListFromEntityId(String entityId) {
        List<String> entityIdsList = new ArrayList<>();

        if (entityId != null && !entityId.isEmpty()) {
            entityIdsList.add(entityId);
        } else {
            entityIdsList = null;
        }
        return entityIdsList;
    }

    protected List<T> getListFromEntity(T entity) {
        List<T> entitiesList = new ArrayList<>();

        if (entity != null) {
            entitiesList.add(entity);
        } else {
            entitiesList = null;
        }
        return entitiesList;
    }

    protected boolean isAdminUser(@Auth User user) {
        // TODO not used for now, return true always
        return true;
    }



}

