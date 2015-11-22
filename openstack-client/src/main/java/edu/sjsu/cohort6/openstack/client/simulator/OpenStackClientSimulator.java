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

package edu.sjsu.cohort6.openstack.client.simulator;

import edu.sjsu.cohort6.openstack.client.OpenStackInterface;
import edu.sjsu.cohort6.openstack.client.ServiceSpec;
import edu.sjsu.cohort6.openstack.common.model.Service;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.SimpleTenantUsage;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.compute.domain.NovaFlavor;
import org.openstack4j.openstack.compute.domain.NovaServer;
import org.openstack4j.openstack.compute.domain.NovaSimpleTenantUsage;
import org.openstack4j.openstack.identity.domain.KeystoneTenant;
import org.openstack4j.openstack.identity.domain.KeystoneUser;
import org.openstack4j.openstack.image.domain.GlanceImage;
import org.openstack4j.openstack.networking.domain.NeutronNetwork;
import org.openstack4j.openstack.networking.domain.NeutronSubnet;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulator mode is used during development to speed up work without needing to move the server jar to openstack VM and
 * restarting it. With this during development we can develop the UI locally without requiring access to
 * openstack VM and any real data.
 *
 * @author rwatsh on 11/21/15.
 */
public class OpenStackClientSimulator implements OpenStackInterface {

    public static final String TENANT_ID = "123456";
    public static final String NET_1 = "net1";

    @Override
    public Network getNetworkByName(String net1) {
        Network net = new NeutronNetwork();
        net.setName(NET_1);
        net.setTenantId(TENANT_ID);
        return net;
    }

    @Override
    public Server startVM(ServiceSpec serviceSpec) {
        Server s = new NovaServer();
        return s;
    }

    @Override
    public void deleteServers(Service service) {

    }

    @Override
    public List<? extends Flavor> getFlavors() {
        Flavor f = new NovaFlavor();
        return new ArrayList<Flavor>(){{add(f);}};
    }

    @Override
    public Flavor getFlavorByName(String name) {
        Flavor f = new NovaFlavor();
        return f;
    }

    @Override
    public Server getServerByName(String vmName) {
        return new NovaServer();
    }

    @Override
    public List<? extends Server> getAllServers() {
        return new ArrayList<Server>(){{add(new NovaServer());}};
    }

    @Override
    public Tenant createTenant(String name, String description) {
        return new KeystoneTenant();
    }

    @Override
    public List<? extends Tenant> getAllTenants() {
        return new ArrayList<Tenant>(){{add(new KeystoneTenant());}};
    }

    @Override
    public SimpleTenantUsage getQuotaForTenant() {
        return new NovaSimpleTenantUsage();
    }

    @Override
    public User createUser(String name, String password, String emailId) {
        return new KeystoneUser();
    }

    @Override
    public Tenant getTenantByName() {
        return new KeystoneTenant();
    }

    @Override
    public Network createNetwork(String name) {
        return new NeutronNetwork();
    }

    @Override
    public List<? extends Network> getAllNetworks() {
        return new ArrayList<Network>(){{add(new NeutronNetwork());}};
    }

    @Override
    public Subnet createSubnet(String name, String networkId, String tenantId, String startIpPool, String endIpPool, String cidr) {
        return new NeutronSubnet();
    }

    @Override
    public List<? extends Subnet> getAllSubnets() {
        return new ArrayList<Subnet>(){{add(new NeutronSubnet());}};
    }

    @Override
    public Image getImageByName(String name) {
        return new GlanceImage();
    }

    @Override
    public void deleteNetwork(Network network) {

    }

    @Override
    public void close() throws Exception {

    }
}
