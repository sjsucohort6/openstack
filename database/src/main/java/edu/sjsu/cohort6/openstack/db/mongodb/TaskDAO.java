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
import edu.sjsu.cohort6.openstack.common.model.Task;
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
import java.util.logging.Logger;

/**
 * @author rwatsh on 11/17/15.
 */
public class TaskDAO extends BasicDAO<Task, String>  implements BaseDAO<Task> {
    private static final Logger log = Logger.getLogger(ServiceDAO.class.getName());
    protected Morphia morphia;

    protected TaskDAO(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
        this.morphia = morphia;
    }

    @Override
    public List<String> add(List<Task> entityList) throws DBException {
        try {
            List<String> insertedIds = new ArrayList<>();
            if (entityList != null) {
                for (Task task : entityList) {
                    Key<Task> key = this.save(task);
                    insertedIds.add(key.getId().toString());
                }
            }
            return insertedIds;
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    public synchronized void addOrUpdate(List<Task> entityList) throws DBException {
        List<String> ids = new ArrayList<>();
        if (entityList != null && !entityList.isEmpty()) {
            Task t = entityList.get(0);
            String id = t.getId();
            List<Task> tasks = fetchById(new ArrayList<String>() {{
                add(id);
            }});
            if (tasks == null || tasks.isEmpty()) {
                // new task so add
                add(entityList);
            } else {
                // existing task, update
                update(entityList);
            }
        }
    }

    @Override
    public long remove(List<String> entityIdsList) throws DBException {
        Query<Task> query = this.createQuery().field(Mapper.ID_KEY).in(entityIdsList);
        return this.deleteByQuery(query).getN();
    }

    @Override
    public void update(List<Task> entityList) throws DBException {
        for (Task task : entityList) {
            List<Task> tasks = fetchById(new ArrayList<String>() {{add(task.getId());}});
            Task existingTask = null;
            if (tasks != null && !tasks.isEmpty()) {
                existingTask = tasks.get(0);
            }
            UpdateOperations<Task> ops = this.createUpdateOperations();
            if (task.getJobName() != null)
                ops.set("jobName", task.getJobName());
            if(task.getMessage() != null)
                ops.set("message", task.getMessage());
            if(task.getJobDataMap() != null)
                ops.set("jobDataMap", task.getJobDataMap());
            if(task.getStartTime() != null)
                ops.set("startTime", task.getStartTime());
            if(task.getTenantName() != null) {
                ops.set("tenantName", task.getTenantName());
            }
            if(task.getTriggerName() != null)
                ops.set("triggerName", task.getTriggerName());


            Query<Task> updateQuery = this.createQuery().field(Mapper.ID_KEY).equal(task.getId());
            this.update(updateQuery, ops);
        }
    }

    @Override
    public List<Task> fetchById(List<String> entityIdsList) throws DBException {
        List<String> objectIds = new ArrayList<>();
        if (entityIdsList != null) {
            for (String id : entityIdsList) {
                objectIds.add(id);
            }
        }

        if (!objectIds.isEmpty()) {
            Query<Task> query = this.createQuery().field(Mapper.ID_KEY).in(objectIds);
            QueryResults<Task> results = this.find(query);
            return results.asList();
        } else {
            Query<Task> query = this.createQuery();
            QueryResults<Task> results = this.find(query);
            return results.asList();
        }
    }

    @Override
    public List<Task> fetch(String query) throws DBException {
        List<Task> tasks = new ArrayList<>();
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
            Task task = morphia.fromDBObject(Task.class, dbObject);
            tasks.add(task);
        }
        return tasks;
    }
}
