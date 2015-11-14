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

package edu.sjsu.cohort6.openstack.rest.filter;

import edu.sjsu.cohort6.openstack.job.JobConstants;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Ensure the incoming requests have X-ID-TENANT-NAME in them.
 *
 * @author rwatsh on 11/12/15.
 */
@Provider
public class CheckRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        String tenantName = requestContext.getHeaderString(JobConstants.X_ID_TENANT_NAME);
        if (tenantName == null) {
            requestContext.abortWith(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("X-ID-TENANT-NAME header must be specified in the request.")
                            .build());
        }
    }
}
