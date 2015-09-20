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

package edu.sjsu.cohort6.openstack.test;

import edu.sjsu.cohort6.openstack.OpenStackClient;
import edu.sjsu.cohort6.openstack.common.api.OpenStackInterface;
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by rwatsh on 9/20/15.
 */
public class OpenStackClientTest {

    private OpenStackInterface openStackClient;

    @BeforeMethod
    public void setUp() throws Exception {
        openStackClient = new OpenStackClient("admin", "admin", "61f23b78184d4b92", "http://localhost:5000/v2.0/");
    }

    @AfterMethod
    public void tearDown() throws Exception {
        openStackClient.close();
    }

    @Test
    public void testListServers() throws Exception {
        //TODO: first create a server
        List<Server> servers = openStackClient.listServers();
        Assert.assertNotNull(servers);
        // TODO: uncomment once we start with server creation
        // Assert.assertTrue(!servers.isEmpty(), "No servers exist");
    }

    //@Test
    // TenantAdminApi is *not* present
    public void testCreateTenantAndUser() throws Exception {
        Tenant tenant = openStackClient.createTenant();
        Assert.assertNotNull(tenant);
        User user = openStackClient.createUser(tenant);
        Assert.assertNotNull(user);
    }
}
