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

/**
 * @author rwatsh on 11/15/15.
 */
public interface HttpConstants {
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_NOT_AUTHORIZED = 401;
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_INTERNAL_ERR = 500;
    String VIRTAPP_API_V1_0 = "/virtapp/api/v1.0";
    String VIRTAPP_API_V1_0_TASKS = VIRTAPP_API_V1_0 + "/tasks";
    String VIRTAPP_API_V1_0_QUOTA = VIRTAPP_API_V1_0 + "/quota";
    String VIRTAPP_API_V1_0_SERVICES = VIRTAPP_API_V1_0 + "/services";
    String VIRTAPP_API_V1_0_SERVICE_NAME = VIRTAPP_API_V1_0_SERVICES + "/:serviceName";
    String WWW_DIR = "/public";
}

