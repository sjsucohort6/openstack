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

package edu.sjsu.cohort6.openstack.server.payload;

import edu.sjsu.cohort6.openstack.common.api.Validable;
import edu.sjsu.cohort6.openstack.common.api.ValidationException;
import edu.sjsu.cohort6.openstack.common.model.ServiceType;
import lombok.Data;

import java.util.List;

/*
 * @author rwatsh on 11/15/15.

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


 */
@Data
public class ServicePayload extends Validable {

    private String name;
    private ServiceType serviceType;
    private String networkName;
    private List<NodePayload> nodes;

    @Override
    public boolean isValid() throws ValidationException {
        boolean valid = isReqd(name) && isReqd(nodes) && isReqd(serviceType) && isReqd(networkName);
        if (valid) {
            for (NodePayload node : nodes) {
                valid = node.isValid();
                if (!valid) {
                    return false;
                }
            }
        }
        return valid;
    }
}
