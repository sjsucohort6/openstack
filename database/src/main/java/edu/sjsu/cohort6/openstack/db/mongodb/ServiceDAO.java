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
import edu.sjsu.cohort6.openstack.common.model.*;
import edu.sjsu.cohort6.openstack.db.BaseDAO;
import edu.sjsu.cohort6.openstack.db.DBException;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateOperations;
import org.openstack4j.model.compute.Server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO for service.
 *
 * @author rwatsh on 11/5/15.
 */
public class ServiceDAO extends BasicDAO<Service, String> implements BaseDAO<Service> {
    private static final Logger log = Logger.getLogger(ServiceDAO.class.getName());
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
            List<Service> services = fetchById(new ArrayList<String>() {{add(service.getName());}});
            Service existingService = null;
            if (services != null && !services.isEmpty()) {
                existingService = services.get(0);
            }
            UpdateOperations<Service> ops = this.createUpdateOperations();
                if (service.getName() != null)
                    ops.set("name", service.getName());
                if(service.getTenant() != null)
                    ops.set("tenant", service.getTenant());
                if(service.getNetworkName() != null)
                    ops.set("networkName", service.getNetworkName());
                if(service.getServiceType() != null)
                    ops.set("serviceType", service.getServiceType());
                if(service.getNodes() != null) {
                    // Replace any existing nodes with updated nodes.
                    // It is assumed the caller is updating any existing nodes or appending.
                    ops.set("nodes", service.getNodes());
                }
                if(service.getStatus() != null)
                    ops.set("status", service.getStatus());
                if(service.getLogs() != null) {
                    // Replace logs with updated logs.
                    // It is assumed the caller is appending the logs.
                    ops.set("logs", service.getLogs());
                }

            Query<Service> updateQuery = this.createQuery().field(Mapper.ID_KEY).equal(service.getName());
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

    /**
     * Known issue with the below implementation is the service status may switch between ready and in-progress but it
     * eventually will be set to either FAILED or READY depending on the last job completes with a failure or success.
     * If the status at anytime has been marked as failed, then it will remain set to failed regardless of other child jobs
     * of the service being successful or in-progress.
     *
     * The service status will be the cumulative status of all jobs that were started for the service.
     *  @param serviceName
     *  @param statusToSet
     */
    public void updateServiceStatus(final String serviceName, ServiceStatus statusToSet) {
        try {
            List<Service> services = fetchById(new ArrayList<String>() {{
                add(serviceName);
            }});
            if (services != null && !services.isEmpty()) {
                Service s = services.get(0);
                ServiceStatus status = s.getStatus();
                switch (statusToSet) {
                    case FAILED:
                        s.setStatus(ServiceStatus.FAILED);
                        break;
                    case IN_PROGRESS:
                        if (status != ServiceStatus.FAILED) {
                            s.setStatus(ServiceStatus.IN_PROGRESS);
                        }
                        break;
                    case READY:
                        if (status != ServiceStatus.FAILED) {
                            s.setStatus(ServiceStatus.READY);
                        }
                }

                update(new ArrayList<Service>(){{ add(s);}});
            }
        } catch (DBException e) {
            e.printStackTrace();
            log.severe("Failed to update status for service: " + serviceName);
        }
    }

    public void updateServiceLog(final String serviceName, String message) {
        try {
            List<Service> services = fetchById(new ArrayList<String>() {{
                add(serviceName);
            }});
            if (services != null && !services.isEmpty()) {
                Service s = services.get(0);
                ServiceLog serviceLog = new ServiceLog();
                serviceLog.setMessage(message);
                serviceLog.setTime(new Date());
                List<ServiceLog> serviceLogs = s.getLogs();
                if (serviceLogs == null) {
                    serviceLogs = new ArrayList<>();
                }
                serviceLogs.add(serviceLog);
                s.setLogs(serviceLogs);
                update(new ArrayList<Service>(){{ add(s);}});
            }
        } catch (DBException e) {
            e.printStackTrace();
            log.severe("Failed to update logs for service: " + serviceName);
        }
    }

    public void updateNode(Server s) throws DBException {
        List<Service> services = fetch("{\"nodes.nodeName\":\"" + s.getName() + "\"}");
        updateNode(s, services);
    }

    public void updateNode(Server s, List<Service> services) throws DBException {
        if (services != null && !services.isEmpty()) {
            Service service = services.get(0);
            List<Node> nodes = service.getNodes();
            boolean foundExistingNode = false;
            if (nodes != null) {
                for (Node node : nodes) {
                    if (node.getNodeName().equalsIgnoreCase(s.getName())) {
                        foundExistingNode = true;
                        node.setFlavorName(s.getFlavor().getName());
                        node.setImageName(s.getImage().getName());
                        node.setNodeStatus(s.getStatus().value());
                        node.setNodeId(s.getId());
                        if (s.getStatus() == Server.Status.ERROR) {
                            node.setStatusDescription(s.getFault().getDetails());
                            service.setStatus(ServiceStatus.FAILED);
                        }
                        break;
                    }
                }
                if (foundExistingNode) {
                    update(new ArrayList<Service>() {{
                        add(service);
                    }});
                } else {
                    // Should not generally require this... but adding just in case we missed to add a node
                    // then we do an upsert (insert while updating).

                    // TODO revisit this .., it may not be needed ??
                    String vmName = s.getName();
                    VmType type = null;
                    if (vmName.contains("_web_")) {
                        type = VmType.WEB;
                    } else if(vmName.contains("_db_")) {
                        type = VmType.DB;
                    }
                    if (type != null) {
                        addNode(service.getName(), type, s);
                    }
                }
            }
        }
    }

    public void addNode(String serviceName, VmType type, Server s) throws DBException {
        List<Service> services = fetchById(new ArrayList<String>(){{add(serviceName);}});
        if (services != null && !services.isEmpty()) {
            Service service = services.get(0);


            List<Node> nodes = service.getNodes();
            if (nodes == null) {
                nodes = new ArrayList<>();
            }
            Node node = new Node();
            node.setFlavorName(s.getFlavor().getName());
            node.setNodeId(s.getId());
            node.setStatusDescription(s.getFault() != null ? s.getFault().getDetails(): "");
            node.setNodeStatus(s.getStatus().value());
            node.setImageName(s.getImage().getName());
            node.setNodeName(s.getName());
            node.setType(type);
            nodes.add(node);
            service.setNodes(nodes);
            update(new ArrayList<Service>(){{add(service);}});
        }
    }

    /**
     * Update the node if the node is found in the specified service name.
     *
     * @param s
     * @param serviceName
     * @throws DBException
     */
    public void updateNode(Server s, String serviceName) throws DBException {
        List<Service> services = fetchById(new ArrayList<String>(){{add(serviceName);}});
        updateNode(s, services);
    }
}
