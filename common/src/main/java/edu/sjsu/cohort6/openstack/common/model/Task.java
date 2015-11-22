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
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Job info. This is to persist the job message of all jobs that were started.
 *
 * @author rwatsh on 11/16/15.
 */
@Entity(value = "tasks" , noClassnameStored = true, concern = "SAFE")
public class Task implements IModel {
    @Id
    private String id = new ObjectId().toHexString();
    private String jobName;
    private String tenantName;
    private String triggerName;
    private Map<String, String> jobDataMap = new HashMap<String, String>();
    private Date startTime;
    private String message;

    @JsonProperty
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @JsonProperty
    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    @JsonProperty
    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    @JsonProperty
    public Map<String, String> getJobDataMap() {
        return jobDataMap;
    }

    public void setJobDataMap(Map<String, String> jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    @JsonProperty
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? new ObjectId(id).toHexString() : new ObjectId().toHexString();
    }
}
