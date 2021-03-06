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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * Created by rwatsh on 9/14/15.
 */
public class OpenStackCloudServiceConfiguration extends Configuration {
    @Valid
    @NotNull
    private DBConfig dbConfig = new DBConfig();

    @NotNull
    private OpenStackConfig openStackConfig = new OpenStackConfig();

    @JsonProperty("database")
    public DBConfig getDbConfig() {
        return dbConfig;
    }

    @JsonProperty("database")
    public void setDbConfig(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public OpenStackConfig getOpenStackConfig() {
        return openStackConfig;
    }

    public void setOpenStackConfig(OpenStackConfig openStackConfig) {
        this.openStackConfig = openStackConfig;
    }
}
