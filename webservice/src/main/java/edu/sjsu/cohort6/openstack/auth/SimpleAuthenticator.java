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

package edu.sjsu.cohort6.openstack.auth;

import com.google.common.base.Optional;
import edu.sjsu.cohort6.openstack.OpenStack4JClient;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Setup basic authenticator which authenticates the user creds against openstack keystone.
 *
 * @author rwatsh
 */
public class SimpleAuthenticator implements Authenticator<BasicCredentials, User> {
    private String tenant;
    private static final Logger log = Logger.getLogger(SimpleAuthenticator.class.getName());

    public SimpleAuthenticator(String tenant) {
        this.tenant = tenant;
    }


    @Override
    public Optional<User> authenticate(BasicCredentials basicCredentials) throws AuthenticationException {
        //return getUserByCredentials(credentials.getUsername(), credentials.getPassword());
        User user = null;
        try {
            OpenStack4JClient.authenticateUser(basicCredentials.getUsername(),
                    basicCredentials.getPassword(), tenant);
            user = new User();
            user.setUserName(basicCredentials.getUsername());
            user.setPassword(basicCredentials.getPassword());
            user.setTenant(tenant);
        } catch (Exception e) {
            log.log(Level.SEVERE, MessageFormat.format("Authentication failed for tenant {0} user {1}",
                    tenant, basicCredentials.getUsername()));
        }
        return Optional.fromNullable(user);
    }

    /*private Optional<User> getUserByCredentials(String username, String password) {
        UserDAO userDAO = (UserDAO) dbClient.getDAO(UserDAO.class);
        return userDAO.getUserByCredentials(username, password);
    }*/

    /*@Override
    public Optional<User> authenticate(SimpleCredentials simpleCredentials) throws AuthenticationException {
        User user = null;
        try {
            OpenStack4JClient.authenticateUser(simpleCredentials.getUser(),
                    simpleCredentials.getPassword(), simpleCredentials.getTenant());
            user = new User();
            user.setUserName(simpleCredentials.getUser());
            user.setPassword(simpleCredentials.getPassword());
            user.setTenant(simpleCredentials.getTenant());
        } catch (Exception e) {
            log.log(Level.SEVERE, MessageFormat.format("Authentication failed for tenant {0} user {1}",
                    simpleCredentials.getTenant(), simpleCredentials.getUser()));
        }
        return Optional.fromNullable(user);
    }*/
}
