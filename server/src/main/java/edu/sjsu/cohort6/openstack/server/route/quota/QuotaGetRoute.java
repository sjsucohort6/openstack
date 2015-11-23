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

package edu.sjsu.cohort6.openstack.server.route.quota;

import edu.sjsu.cohort6.openstack.client.SshClient;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.server.HttpConstants;
import lombok.extern.java.Log;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.logging.Level;

/**
 * Get the tenant quota usage information.
 *
 * @author rwatsh on 11/20/15.
 */
@Log
public class QuotaGetRoute implements Route {

    public static final String QUOTA_SHOW_CMD = "source ~/keystonerc_admin; nova quota-show";
    private final String user;
    private final String password;
    private final String tenant;
    private final DBClient dbClient;

    public QuotaGetRoute(String user, String password, String tenant, DBClient dbClient) {
        this.user = user;
        this.password = password;
        this.tenant = tenant;
        this.dbClient = dbClient;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            SshClient sshClient = new SshClient();
            String command = QUOTA_SHOW_CMD;
            List<String> actual = sshClient.executeCommand(user, password, "localhost", 2022, command);
            StringBuilder sb = new StringBuilder();
            for (String s: actual) {
                sb.append(s);
            }
            response.type(HttpConstants.TEXT_PLAIN);
            response.status(HttpConstants.HTTP_OK);
            return sb.toString();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting quota for tenant " + tenant, e);
            throw e;
        }

    }
}
