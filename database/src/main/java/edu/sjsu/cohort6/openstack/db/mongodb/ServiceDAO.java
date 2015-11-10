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

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import edu.sjsu.cohort6.openstack.common.dto.Service;
import edu.sjsu.cohort6.openstack.db.BaseDAO;
import edu.sjsu.cohort6.openstack.db.DBException;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO for service.
 *
 * @author rwatsh on 11/5/15.
 */
public class ServiceDAO extends BasicDAO<Service, String> implements BaseDAO<Service> {
    protected Morphia morphia;

    protected ServiceDAO(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
        this.morphia = morphia;
    }


    @Override
    public List<String> add(List<Service> entityList) throws DBException {
        try {
            List<String> insertedIds = new ArrayList<>();
            if (entityList != null) {
                for (Service service : entityList) {
                    Key<Service> key = this.save(service);
                    insertedIds.add(key.getId().toString());
                }
            }
            return insertedIds;
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    @Override
    public long remove(List<String> entityIdsList) throws DBException {
        Query<Service> query = this.createQuery().field(Mapper.ID_KEY).in(entityIdsList);
        return this.deleteByQuery(query).getN();
    }

    @Override
    public void update(List<Service> entityList) throws DBException {
        for (Service service : entityList) {
            UpdateOperations<Service> ops = this.createUpdateOperations()
                    .set("name", service.getName())
                    .set("tenant", service.getTenant())
                    .set("type", service.getType())
                    .set("vms", service.getVms())
                    .set("logs", service.getLogs());

            Query<Service> updateQuery = this.createQuery().field(Mapper.ID_KEY).equal(service.getId());
            this.update(updateQuery, ops);
        }
    }

    @Override
    public List<Service> fetchById(List<String> entityIdsList) throws DBException {
        List<String> objectIds = new ArrayList<>();
        if (entityIdsList != null) {
            for (String id : entityIdsList) {
                objectIds.add(id);
            }
        }

        if (!objectIds.isEmpty()) {
            Query<Service> query = this.createQuery().field(Mapper.ID_KEY).in(objectIds);
            QueryResults<Service> results = this.find(query);
            return results.asList();
        } else {
            Query<Service> query = this.createQuery();
            QueryResults<Service> results = this.find(query);
            return results.asList();
        }
    }

    @Override
    public List<Service> fetch(String query) throws DBException {
        List<Service> services = new ArrayList<>();
        DBObject dbObjQuery;
        DBCursor cursor;
        if (!(query == null)) {
            dbObjQuery = (DBObject) JSON.parse(query);
            cursor = this.getCollection().find(dbObjQuery);
        } else {
            cursor = this.getCollection().find();
        }

        List<DBObject> dbObjects = cursor.toArray();
        for (DBObject dbObject: dbObjects) {
            Service service = morphia.fromDBObject(Service.class, dbObject);
            services.add(service);
        }
        return services;

    }
}
