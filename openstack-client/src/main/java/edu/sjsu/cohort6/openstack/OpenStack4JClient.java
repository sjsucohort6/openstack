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

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;

import java.util.List;

/**
 * @author rwatsh on 11/1/15.
 */
public class OpenStack4JClient {
    public static void main(String[] args) {
        testOpenstack();

    }

    public static void testOpenstack() {
        try {
            Identifier domainIdentifier = Identifier.byName("example-domain");
            /*IOSClientBuilder.V3 osV3Builder = OSFactory.builderV3()
                    .endpoint("http://10.0.2.15:5000/v3")
                    .credentials("admin", "61f23b78184d4b92");

            OSClient os = osV3Builder.authenticate();*/

            OSClient os = OSFactory.builder()
                    .endpoint("http://127.0.0.1:5000/v2.0")
                    .credentials("admin","61f23b78184d4b92")
                    .tenantName("admin")
                    .authenticate();

            // Find all Users
            List<? extends User> users = os.identity().users().list();
            System.out.println(users);

            // List all Tenants
            List<? extends Tenant> tenants = os.identity().tenants().list();
            System.out.println(tenants);

            // Find all Compute Flavors
            List<? extends Flavor> flavors = os.compute().flavors().list();
            System.out.println(flavors);

            // Find all running Servers
            List<? extends Server> servers = os.compute().servers().list();
            System.out.println(servers);

            // Suspend a Server
            //os.compute().servers().action("serverId", Action.SUSPEND);

            // List all Networks

            // List all Subnets
            List<? extends Subnet> subnets = os.networking().subnet().list();
            System.out.println(subnets);
            List<? extends Network> networks = os.networking().network().list();
            System.out.println(networks);

            // List all Routers
            List<? extends Router> routers = os.networking().router().list();
            System.out.println(routers);

            // List all Images (Glance)
            List<? extends Image> images = os.images().list();
            System.out.println(images);

            // Download the Image Data
            //InputStream is = os.images().getAsStream("imageId");
        } catch (Exception e) {
            System.out.println("Got error... ");
            e.printStackTrace();
        }
    }
}
