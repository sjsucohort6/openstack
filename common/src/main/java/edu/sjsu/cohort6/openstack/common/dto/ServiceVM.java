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
import org.mongodb.morphia.annotations.Embedded;

/**
 * Model for Service VM.
 *
 * @author rwatsh on 11/5/15.
 */
@Embedded
public class ServiceVM {
    private String name;
    private int vcpus;
    private int memory;
    private int hdd;
    private VmType type;

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public int getVcpus() {
        return vcpus;
    }

    public void setVcpus(int vcpus) {
        this.vcpus = vcpus;
    }

    @JsonProperty
    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    @JsonProperty
    public int getHdd() {
        return hdd;
    }

    public void setHdd(int hdd) {
        this.hdd = hdd;
    }

    @JsonProperty
    public VmType getType() {
        return type;
    }

    public void setType(VmType type) {
        this.type = type;
    }
}
