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
import edu.sjsu.cohort6.openstack.common.model.Quota;
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
 * @author rwatsh on 11/23/15.
 */
public class QuotaDAO extends BasicDAO<Quota, String> implements BaseDAO<Quota> {
    private Morphia morphia;

    protected QuotaDAO(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
        this.morphia = morphia;
    }

    @Override
    public List<String> add(List<Quota> entityList) throws DBException {
        try {
            List<String> insertedIds = new ArrayList<>();
            if (entityList != null) {
                for (Quota quota : entityList) {
                    Key<Quota> key = this.save(quota);
                    insertedIds.add(key.getId().toString());
                }
            }
            return insertedIds;
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    public void addOrUpdate(List<Quota> entityList) throws DBException {
        List<String> ids = new ArrayList<>();
        if (entityList != null && !entityList.isEmpty()) {
            Quota t = entityList.get(0);
            String tenant = t.getTenant();
            String user = t.getUser();
            List<Quota> quotas = fetchByUser(tenant, user);
            if (quotas == null || quotas.isEmpty()) {
                // new task so add
                add(entityList);
            } else {
                // existing task, update
                update(entityList);
            }
        }
    }

    public List<Quota> fetchByUser(String tenant, String user) throws DBException {
        return fetch("{user:\"" + user + "\", tenant: \"" + tenant + "\"}");
    }

    @Override
    public long remove(List<String> entityIdsList) throws DBException {
        Query<Quota> query = this.createQuery().field(Mapper.ID_KEY).in(entityIdsList);
        return this.deleteByQuery(query).getN();
    }

    @Override
    public void update(List<Quota> entityList) throws DBException {
        for (Quota quota : entityList) {
            UpdateOperations<Quota> ops = this.createUpdateOperations();
            if(quota.getQuota() != null)
                ops.set("quota", quota.getQuota());

            Query<Quota> updateQuery = this.createQuery().
                                        field("user").equal(quota.getUser()).
                                        field("tenant").equal(quota.getTenant());
            this.update(updateQuery, ops);
        }
    }

    @Override
    public List<Quota> fetchById(List<String> entityIdsList) throws DBException {
        List<String> objectIds = new ArrayList<>();
        if (entityIdsList != null) {
            for (String id : entityIdsList) {
                objectIds.add(id);
            }
        }

        if (!objectIds.isEmpty()) {
            Query<Quota> query = this.createQuery().field(Mapper.ID_KEY).in(objectIds);
            QueryResults<Quota> results = this.find(query);
            return results.asList();
        } else {
            Query<Quota> query = this.createQuery();
            QueryResults<Quota> results = this.find(query);
            return results.asList();
        }
    }

    @Override
    public List<Quota> fetch(String query) throws DBException {
        List<Quota> quotas = new ArrayList<>();
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
            Quota quota = morphia.fromDBObject(Quota.class, dbObject);
            quotas.add(quota);
        }
        return quotas;
    }
}
