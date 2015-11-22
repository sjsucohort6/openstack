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

package edu.sjsu.cohort6.openstack.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.sjsu.cohort6.openstack.common.api.Validable;
import edu.sjsu.cohort6.openstack.common.api.ValidationException;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;

//import lombok.Data;

/*
 * Service Java object which maps to the input json.
 *
 * A create service request will have following headers and JSON payload:
Authorization header - user creds

Basic service example payload:
 {
    "name": "BasicServ1",
    "serviceType" : "BASIC",
    "networkName" : "net1",
    "nodes": [
        {
        "flavorName" : "m1.small",
        "imageName" : "UBUNTU-WEB-IMG",
        "type" : "WEB"
        }
    ]
 }

Big service example payload:
{
    "name": "BigServ1",
    "serviceType" : "BIG",
    "networkName" : "net1",
    "nodes": [
        {
        "flavorName" : "m1.small",
        "imageName" : "UBUNTU-WEB-IMG",
        "type": "WEB"
        },
        {
        "flavorName" : "m1.small",
        "imageName" : "UBUNTU-DB-IMG",
        "type": "DB"
        }
    ]
 }


 In DB:

 Attributes marked (*DB only) are not meant to be sent by payload.

 {
    "name": "BigServ1",
    "serviceType" : "BIG",
    "networkName" : "net1",
    "tenant" : "admin", (*DB only)
    "status": "RUNNING",  (*DB only)
    "nodes": [
        {
        "flavorName" : "m1.small",
        "imageName" : "UBUNTU-WEB-IMG",
        "type": "WEB",
        "nodeName": "BigServ1_web_vm_1" (*DB only),
        "nodeId" : "serverId assigned by openstack" (*DB only)
        },
        {
        "flavorName" : "m1.small",
        "imageName" : "UBUNTU-DB-IMG",
        "type": "DB",
        "nodeName": "BigServ1_db_vm_1" (*DB only),
        "nodeId" : "serverId assigned by openstack" (*DB only)
        }
    ],
    "logs": [ (*DB only)
        {
            "message": "VM Starting",
            "time": ISODate(44442311414124)
        },
        {
            "message": "VM Started",
            "time": ISODate(44442311414125)
        }
    ]

 }

> db.services.find().pretty()
{
	"_id" : "BasicTestServ--813578692",
	"tenant" : "admin",
	"serviceType" : "BASIC",
	"networkName" : "net2",
	"status" : "READY",
	"nodes" : [
		{
			"flavorName" : "m1.small",
			"imageName" : "UBUNTU-WEB-IMG",
			"type" : "WEB",
			"nodeName" : "testVM",
			"nodeId" : "-1805609176"
		},
		{
			"flavorName" : "m1.small",
			"imageName" : "UBUNTU-DB-IMG",
			"type" : "DB",
			"nodeName" : "testDBVM-165299483",
			"nodeId" : "165299483"
		}
	],
	"logs" : [
		{
			"message" : "test message",
			"time" : ISODate("2015-11-16T17:49:00.337Z")
		},
		{
			"message" : "Updated test message -572621322",
			"time" : ISODate("2015-11-16T17:50:55.303Z")
		}
	]
}


 * @author rwatsh on 11/7/15.
 */
//@Data
@Entity(value = "services" , noClassnameStored = true, concern = "SAFE")
public  class Service extends Validable implements IModel {
    /*
     * Service name
     */
    @Id
    private String name;

    private String tenant;
    /*
     * Edition - BASIC or BIG
     */
    private ServiceType serviceType;
    /*
     * Existing or new network's name. All VMs in the service will be connected to this network and
     * via a router to public network.
     */
    private String networkName;
    /*
     * Service status.
     */
    private ServiceStatus status;
    /*
     * VM payload for each node in the service.
     */
    @Embedded
    private List<Node> nodes = new ArrayList<Node>();

    @Embedded
    private List<ServiceLog> logs = new ArrayList<ServiceLog>();

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @JsonProperty
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty
    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    @JsonProperty
    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @JsonProperty
    public List<ServiceLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ServiceLog> logs) {
        this.logs = logs;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    /**
     * Check if all params are provided in the service provisioning request payload are provided.
     *
     * @return
     * @throws ValidationException
     */
    @Override
    public boolean isValid() throws ValidationException {
        boolean valid =  true;//isReqd(name) && isReqd(nodes) && isReqd(serviceType) && isReqd(networkName);
        if (valid) {
            for (Node node: nodes) {
                valid = node.isValid();
                if (!valid) {
                    return false;
                }
            }
        }
        return valid;
    }
}
