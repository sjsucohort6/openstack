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

package edu.sjsu.cohort6.openstack.health;

import com.codahale.metrics.health.HealthCheck;
import edu.sjsu.cohort6.openstack.db.DBClient;


/**
 * Database health check
 *
 * Created by rwatsh on 9/18/15.
 */
public class DBHealthCheck extends HealthCheck{
    private DBClient client;

    public DBHealthCheck(DBClient client) {
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {

        if (client.checkHealth()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Cannot connect to DB " + client.getConnectString());
        }
    }
}
