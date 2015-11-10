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

package edu.sjsu.cohort6.openstack.db.mongodb;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.MongoClient;
import edu.sjsu.cohort6.openstack.common.dto.Service;
import edu.sjsu.cohort6.openstack.db.BaseDAO;
import edu.sjsu.cohort6.openstack.db.DBClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by rwatsh on 9/20/15.
 */
public class MongoDBClient implements DBClient {
    private final String server;
    private final int port;
    private final String dbName;
    private ServiceDAO serviceDAO;
    private Morphia morphia = null;
    //private static MongoDBClient instance = null;
    private MongoClient mongoClient;
    private Datastore morphiaDatastore;

    /**
     * Constructs a MongoDB client instance.
     *
     * This is private so it can only be instantiated via DI (using Guice).
     *
     * @param server    server hostname or ip
     * @param port      port number for mongodb service
     * @param dbName    name of db to use
     */
    @Inject
    private MongoDBClient(@Assisted("server") String server, @Assisted("port") int port, @Assisted("dbName") String dbName) {
        this.server = server;
        this.port = port;
        this.dbName = dbName;
        mongoClient = new MongoClient(server, port);
        morphia = new Morphia();
        morphia.mapPackageFromClass(Service.class);
        morphiaDatastore = morphia.createDatastore(mongoClient, dbName);
        morphiaDatastore.ensureIndexes();
        serviceDAO = new ServiceDAO(mongoClient, morphia, dbName);

    }

    @Override
    public void dropDB(String dbName) {

    }

    @Override
    public void useDB(String dbName) {

    }

    @Override
    public boolean checkHealth() {
        return false;
    }

    @Override
    public String getConnectString() {
        return null;
    }

    @Override
    public Object getDAO(Class<? extends BaseDAO> clazz) {
        return null;
    }

    @Override
    public Morphia getMorphia() {
        return null;
    }
}
