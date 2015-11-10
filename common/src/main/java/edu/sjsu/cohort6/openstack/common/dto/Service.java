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

package edu.sjsu.cohort6.openstack.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongodb.morphia.annotations.*;

import java.util.List;

/**
 * Represents a platform as a service.
 *
 * Service name is unique for a tenant.
 *
 * @author rwatsh on 11/5/15.
 */
@Entity(value = "services" , noClassnameStored = true, concern = "SAFE")
@Indexes({
        @Index(value = "tenant, name", unique = true)
})
public class Service extends BaseModel {
    @Id
    private String id;
    private String name;
    private ServiceType type;
    private String tenant;
    /*
     * Assuming there will just one network, with 1 or more subnets and the subnets will be connected by
     * 1 or more routers.
     * Each router has one gateway that is connected to a network and many interfaces that are connected to
     * various subnets.
     * For demo, we will have 1 network, 2 subnets, 1 router.
     */
    private String networkId;
    private List<String> subnetIds;
    private List<String> routerIds;

    @Embedded
    private List<ServiceVM> vms;
    @Embedded
    private List<ServiceLog> logs;

    @JsonProperty
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    @JsonProperty
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @JsonProperty
    public List<ServiceVM> getVms() {
        return vms;
    }

    public void setVms(List<ServiceVM> vms) {
        this.vms = vms;
    }

    @JsonProperty
    public List<ServiceLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ServiceLog> logs) {
        this.logs = logs;
    }

    @JsonProperty
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @JsonProperty
    public List<String> getSubnetIds() {
        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    @JsonProperty
    public List<String> getRouterIds() {
        return routerIds;
    }

    public void setRouterIds(List<String> routerIds) {
        this.routerIds = routerIds;
    }
}
