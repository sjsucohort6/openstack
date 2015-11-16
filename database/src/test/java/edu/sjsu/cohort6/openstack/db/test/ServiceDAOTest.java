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

package edu.sjsu.cohort6.openstack.db.test;

import edu.sjsu.cohort6.openstack.common.model.*;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author rwatsh on 11/16/15.
 */

public class ServiceDAOTest extends DBTest<ServiceDAO, Service> {
    private static final Logger log = Logger.getLogger(ServiceDAOTest.class.getName());
    @Override
    public void testAdd() throws Exception {
        testCreateService();
    }

    @Override
    public void testRemove() throws Exception {
        List<String> insertedIds = testCreateService();
        Assert.assertNotNull(insertedIds);
        long countRemovedEntries = dao.remove(insertedIds);
        Assert.assertTrue(countRemovedEntries > 0, "Failed to delete any service");
    }

    @Override
    public void testUpdate() throws Exception {
        List<String> insertedIds = testCreateService();
        Assert.assertNotNull(insertedIds);
        List<Service> services = dao.fetchById(insertedIds);
        Assert.assertNotNull(services);
        for (Service s : services) {
            // append log
            ServiceLog log = new ServiceLog();
            log.setMessage("Updated test message " + getRandomStr());
            log.setTime(new Date());
            s.setLogs(new ArrayList<ServiceLog>(){{add(log);}});

            // update network
            s.setNetworkName("net2");
            // update status
            s.setStatus(ServiceStatus.READY);

            // append node
            Node node = new Node();
            String randomStr = getRandomStr();
            node.setNodeId(randomStr);
            node.setFlavorName("m1.small");
            node.setImageName("UBUNTU-DB-IMG");
            node.setType(VmType.DB);
            node.setNodeName("testDBVM-" + randomStr);
            List<Node> nodes = new ArrayList<Node>() {{add(node);}};
            s.setNodes(nodes);
        }
        log.info("Service modified: " + services);
        dao.update(services);
        services = dao.fetchById(insertedIds);
        Assert.assertNotNull(services);
        log.info("Service updated in DB: " + services);
    }

    @Override
    public void testFetch() throws Exception {
        List<String> insertedIds = testCreateService();
        Assert.assertNotNull(insertedIds);
        List<Service> services = dao.fetchById(insertedIds);
        Assert.assertNotNull(services);
    }
}
