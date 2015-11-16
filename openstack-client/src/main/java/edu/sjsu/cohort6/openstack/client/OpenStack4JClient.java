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

package edu.sjsu.cohort6.openstack.client;

import edu.sjsu.cohort6.openstack.common.model.Node;
import edu.sjsu.cohort6.openstack.common.model.Service;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.SimpleTenantUsage;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author rwatsh on 11/1/15.
 */
public class OpenStack4JClient implements OpenStackInterface{

    private static final Logger LOGGER = Logger.getLogger(OpenStack4JClient.class.getName());
    private OSClient os = null;
    private String user = null;
    private String passwd = null;
    private String tenant = null;

    public OpenStack4JClient(String user, String passwd, String tenant) {
        os = authenticateUser(user, passwd, tenant);
        this.user = user;
        this.passwd = passwd;
        this.tenant = tenant;
    }

    public static OSClient authenticateUser(String user, String passwd, String tenant) throws AuthenticationException {
        return OSFactory.builder()
                .endpoint("http://127.0.0.1:5000/v2.0")
                .credentials(user,passwd)
                .tenantName(tenant)
                .authenticate();
    }

    public static void main(String[] args) {
        testOpenstack();

    }

    public static void testOpenstack() {
        try {

            LOGGER.info("Creating service VM.... ");
            OpenStack4JClient client = new OpenStack4JClient("admin", "61f23b78184d4b92", "admin");
            Flavor f = client.getFlavorByName("m1.small");
            Image image = client.getImageByName("MY-UBUNTU-VM");
            Network net = client.getNetworkByName("net1");
            LOGGER.info(MessageFormat.format("Creating VM with flavor {0} and image {1} and network {2}",
                    f.getName(), image.getName(), net.getName()));
            client.startVM(new ServiceSpec("watshVM", f.getId(), image.getId(), net.getId()));


        } catch (Exception e) {
            System.out.println("Got error... ");
            e.printStackTrace();
        }
    }

    @Override
    public Network getNetworkByName(String name) {
        List<? extends Network> networks = getAllNetworks();
        if (networks != null) {
            for (Network network : networks) {
                if (network.getName().equals(name)) {
                    return network;
                }
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Provision a VM and start it.
     *
     * @param serviceSpec
     * @return
     */
    @Override
    public Server startVM(ServiceSpec serviceSpec) {
        ServerCreate sc = Builders.server()
                .name(serviceSpec.getName())
                .flavor(serviceSpec.getFlavorId())
                .image(serviceSpec.getImageId())
                .networks(new ArrayList<String>() {{
                    add(serviceSpec.getNetworkId());
                }})
                .build();
        // Boot the Server
        Server server = os.compute().servers().boot(sc);
        return server;
    }

    public void deleteServers(Service service) {
        if (service != null) {
            List<Node> nodes = service.getNodes();
            if (nodes != null) {
                for (Node node : nodes) {
                    os.compute().servers().delete(node.getNodeId());
                }
            }
        }
    }

    @Override
    public List<? extends Flavor> getFlavors() {
        return os.compute().flavors().list();
    }

    @Override
    public Flavor getFlavorByName(String name) {
        List<? extends Flavor> flavors = os.compute().flavors().list();
        for (Flavor f: flavors) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }



    @Override
    public Tenant createTenant(String name, String description) {
        Tenant tenant = os.identity().tenants()
                .create(new Builders().tenant().name(name).description(description).build());
        return tenant;
    }

    @Override
    public List<? extends Tenant> getAllTenants() {
        return os.identity().tenants().list();
    }

    @Override
    public SimpleTenantUsage getQuotaForTenant() {
        return os.compute().quotaSets().getTenantUsage(tenant);
    }

    @Override
    public User createUser(String name, String password, String emailId) {
        Tenant t = getTenantByName();
        // Create a User associated to the ABC Corporation tenant
        User user = os.identity().users()
                .create(new Builders().user()
                        .name(name)
                        .password(password)
                        .email(emailId)
                        .tenant(t).build());

        return user;

    }

    @Override
    public Tenant getTenantByName() {
        return os.identity().tenants().getByName(tenant);
    }

    @Override
    public Network createNetwork(String name) {
        Tenant tenant = getTenantByName();
        Network network = os.networking().network()
                .create(Builders.network().name(name).tenantId(tenant.getId()).build());
        return network;
    }

    @Override
    public List<? extends Network> getAllNetworks() {
        return os.networking().network().list();
    }

    @Override
    public Subnet createSubnet(String name, String networkId, String tenantId, String startIpPool, String endIpPool, String cidr) {
        return null;
    }

    @Override
    public List<? extends Subnet> getAllSubnets() {
        return null;
    }

    @Override
    public Image getImageByName(String name) {
        List<? extends Image> images = os.images().list();
        for (Image image: images) {
            if (image.getName().equals(name)) {
                return image;
            }
        }
        return null;
    }

    @Override
    public void deleteNetwork(Network network) {
        os.networking().network().delete(network.getId());
    }

    @Override
    public Server getServerByName(String vmName) {
        List<? extends Server> servers = getAllServers();
        if (servers != null) {
            for(Server s: servers) {
                if(s.getName().equalsIgnoreCase(vmName)) {
                    return s;
                }
            }
        }
        return null;
    }

    public List<? extends Server> getAllServers() {
        List<? extends Server> servers = os.compute().servers().list();
        return servers;
    }

    /*@Override
    public Router createRouter(String name, String networkId) {
        return null;
    }*/


}
