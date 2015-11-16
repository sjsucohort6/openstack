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

package edu.sjsu.cohort6.openstack.server.filter;

import edu.sjsu.cohort6.openstack.server.HttpConstants;
import org.apache.commons.lang3.StringUtils;
import spark.FilterImpl;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

import java.util.Base64;

import static spark.Spark.halt;

/**
 * Based on https://github.com/qmetric/spark-authentication.
 *
 * @author rwatsh on 11/15/15.
 */
public class BasicAuthenticationFilter extends FilterImpl {

    private static final String ACCEPT_ALL_TYPES = "*";
    private final AuthenticationDetails authenticationDetails;

    public BasicAuthenticationFilter(final AuthenticationDetails authenticationDetails) {
        this(SparkUtils.ALL_PATHS, authenticationDetails);
    }

    public BasicAuthenticationFilter(final String path, final AuthenticationDetails authenticationDetails) {
        super(path, ACCEPT_ALL_TYPES);
        this.authenticationDetails = authenticationDetails;
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        final String encodedHeader = StringUtils.substringAfter(request.headers("Authorization"), "Basic");

        if (!authenticatedWith(credentialsFrom(encodedHeader))) {
            //response.header("WWW-Authenticate", "Basic"); -- intentionally commenting out since we dont want
            // the browser to prompt.
            halt(HttpConstants.HTTP_NOT_AUTHORIZED);
        }
    }

    private String[] credentialsFrom(final String encodedHeader) {
        return StringUtils.split(encodedHeader != null ? decodeHeader(encodedHeader) : null, ":");
    }

    private String decodeHeader(final String encodedHeader) {
        return new String(Base64.getDecoder().decode(encodedHeader));
    }

    private boolean authenticatedWith(final String[] credentials) {
        if (credentials != null && credentials.length == 2) {
            final String submittedUsername = credentials[0];
            final String submittedPassword = credentials[1];

            return StringUtils.equals(submittedUsername, authenticationDetails.userName)
                    && StringUtils.equals(submittedPassword, new String(authenticationDetails.password));
        } else {
            return false;
        }
    }
}
