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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import edu.sjsu.cohort6.openstack.common.api.OpenStackInterface;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.keystone.v2_0.extensions.TenantAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateTenantOptions;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by rwatsh on 9/19/15.
 *
 * @see https://jclouds.apache.org/start/compute/
 */
public class OpenStackClient implements OpenStackInterface {

    private NovaApi novaApi = null;
    private ComputeService computeService = null;
    private Set<String> regions = null;
    private KeystoneApi keystoneApi = null;
    private String tenantName = "admin";
    private String userName= "admin";
    private String password= "61f23b78184d4b92";
    private String endpoint= "http://localhost:5000/v2.0/";
    private static final Logger log = Logger.getLogger(OpenStackClient.class.getName());

    public OpenStackClient(String tenantName, String userName, String password, String endpoint) {
        this.tenantName = tenantName;
        this.userName = userName;
        this.password = password;
        this.endpoint = endpoint;
        String provider = "openstack-nova";
        Class<NovaApi> apiClass = NovaApi.class;

        String identity = MessageFormat.format("{0}:{1}", tenantName, userName); // tenantName:userName
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
        novaApi = ContextBuilder.newBuilder(provider)
                .endpoint(endpoint)
                .credentials(identity, password)
                .modules(modules)
                .buildApi(apiClass);

        regions = novaApi.getConfiguredRegions();

        computeService = ContextBuilder.newBuilder(provider)
                .endpoint(endpoint)
                .credentials(identity, password)
                .modules(modules).buildView(ComputeServiceContext.class).getComputeService();

        provider = "openstack-keystone";

        keystoneApi = ContextBuilder.newBuilder(provider)
                .endpoint(endpoint)
                .credentials(identity, password)
                .modules(modules)
                .buildApi(KeystoneApi.class);
    }



    public List<Server> listServers() {
        List<Server> servers = new ArrayList<>();
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            System.out.println("Servers in " + region);

            for (Server server : serverApi.listInDetail().concat()) {
                System.out.println("  " + server);
                servers.add(server);
            }
        }
        return servers;
    }

    public Tenant createTenant() {
        System.out.format("  Create Tenant%n");

        Optional<? extends TenantAdminApi> tenantAdminApiExtension = keystoneApi.getTenantAdminApi();

        if (tenantAdminApiExtension.isPresent()) {
            System.out.format("    TenantAdminApi is present%n");

            TenantAdminApi tenantAdminApi = tenantAdminApiExtension.get();
            CreateTenantOptions tenantOptions = CreateTenantOptions.Builder
                    .description("My New Tenant");
            Tenant tenant = tenantAdminApi.create("newTenant", tenantOptions);

            System.out.format("    %s%n", tenant);

            return tenant;
        } else {
            System.out.format("    TenantAdminApi is *not* present%n");
            System.exit(1);

            return null;
        }
    }

    public User createUser(Tenant tenant) {
        System.out.format("  Create User%n");

        Optional<? extends UserAdminApi> userAdminApiExtension = keystoneApi.getUserAdminApi();
        User user = null;
        if (userAdminApiExtension.isPresent()) {
            System.out.format("    UserAdminApi is present%n");

            UserAdminApi userAdminApi = userAdminApiExtension.get();
            CreateUserOptions userOptions = CreateUserOptions.Builder
                    .tenant(tenant.getId())
                    .email("new.email@example.com");
            user = userAdminApi.create("newUser", "newPassword", userOptions);

            System.out.format("    %s%n", user);

        } else {
            System.out.format("    UserAdminApi is *not* present%n");
            System.exit(1);
        }

        return user;
    }

    public void close() throws IOException {
        Closeables.close(novaApi, true);
    }

    public static void main(String[] args) {
        try(OpenStackInterface openStackClient = new OpenStackClient("admin", "admin", "61f23b78184d4b92", "http://10.0.2.15:5000/v2.0/")) {
            List<Server> servers = openStackClient.listServers();
            System.out.println(servers);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
