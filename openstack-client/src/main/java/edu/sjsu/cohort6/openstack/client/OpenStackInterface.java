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

import edu.sjsu.cohort6.openstack.common.model.Service;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.SimpleTenantUsage;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;

import java.util.List;

/**
 * OpenStack client interface.
 *
 * Created by rwatsh on 9/20/15.
 */
public interface OpenStackInterface extends AutoCloseable {

    Network getNetworkByName(String net1);

    //server
    public Server startVM(ServiceSpec serviceSpec);

    void deleteServers(Service service);

    public List<? extends Flavor> getFlavors();
    public Flavor getFlavorByName(String name);
    Server getServerByName(String vmName);
    List<? extends Server> getAllServers();


    //tenant
    public Tenant createTenant(String name, String description);
    public List<? extends Tenant> getAllTenants();
    public SimpleTenantUsage getQuotaForTenant();
    public User createUser(String name, String password, String emailId);
    public Tenant getTenantByName();

    //network
    public Network createNetwork(String name);
    public List<? extends Network> getAllNetworks();
    public Subnet createSubnet(String name, String networkId, String tenantId, String startIpPool, String endIpPool, String cidr);
    public List<? extends Subnet> getAllSubnets();
    //public Router createRouter(String name, String networkId, )

    // Image
    public Image getImageByName(String name);

    void deleteNetwork(Network network);


}
