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

import edu.sjsu.cohort6.openstack.common.model.Quota;
import edu.sjsu.cohort6.openstack.common.util.CommonUtils;
import edu.sjsu.cohort6.openstack.db.DBClient;
import edu.sjsu.cohort6.openstack.db.mongodb.QuotaDAO;
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


    private final String user;
    private final DBClient dbClient;
    private final String tenant;

    public QuotaGetRoute(String user, String tenant, DBClient dbClient) {
        this.user = user;
        this.tenant = tenant;
        this.dbClient = dbClient;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            QuotaDAO quotaDAO = (QuotaDAO) dbClient.getDAO(QuotaDAO.class);
            List<Quota> quotas = quotaDAO.fetchByUser(tenant, user);
            response.type(HttpConstants.APPLICATION_JSON);
            if(!quotas.isEmpty()) {
                return CommonUtils.convertObjectToJson(quotas.get(0));
            } else {
                return "{}"; //empty JSON object
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in getting tasks for tenant " + tenant, e);
            throw e;
        }

    }
}
