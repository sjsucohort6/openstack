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

package edu.sjsu.cohort6.openstack.job;

/**
 * Job data map key names and other job related constants.
 *
 * @author rwatsh on 11/6/15.
 */
public interface JobConstants {
    public static final String SERVICE_NAME = "serviceName";
    public static final String WEB_VM_NAME_FORMAT = "{0}_web_vm_{1}";
    public static final String DB_VM_NAME_FORMAT = "{0}_db_vm_{1}";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String TENANT_NAME = "tenantName";
    public static final String FLAVOR_NAME = "flavorName";
    public static final String IMAGE_NAME = "imageName";
    public static final String NETWORK_NAME = "networkName";

    public static final String SERVICE_PAYLOAD = "servicePayload";
    public static final String CREATE_BASIC_SERVICE = "create-basic-service";
}
