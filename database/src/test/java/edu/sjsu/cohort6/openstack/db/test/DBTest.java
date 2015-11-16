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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import edu.sjsu.cohort6.openstack.common.model.*;
import edu.sjsu.cohort6.openstack.db.*;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;
import org.testng.Assert;
import org.testng.annotations.*;

import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This class is meant to be inherited from by all DB tests.
 * It has methods for test setup and tear down.
 *
 * @author rwatsh on 9/24/15.
 */
public abstract class DBTest<T extends BaseDAO, S> {
    @Inject
    private DBFactory dbFactory;
    protected T dao;
    private Class<T> tClass;

    public String dbName = "openstack_db";
    private static final Logger log = Logger.getLogger(DBTest.class.getName());
    protected static DBClient client;
    private long startTime;

    public DBTest() {
        Module module = new DatabaseModule();
        Guice.createInjector(module).injectMembers(this);

    }

    @BeforeClass
    @Parameters({"server", "port", "dbName"})
    public void setUp(@Optional("localhost") String server,
                      @Optional("27017") String port,
                      @Optional("openstack_db") String dbName) throws Exception {
        client = dbFactory.create(server, Integer.parseInt(port), dbName);

        this.dbName = dbName;
        /*
         * Use reflection to infer the class for T type.
         */
        this.tClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        dao = (T) client.getDAO(tClass);
        client.dropDB(this.dbName);
    }


    @AfterClass
    public void tearDown() throws Exception {
        client.close();
    }

    @BeforeMethod
    public void createDB() {
        client.useDB(dbName);
        log.info("********************");
        startTime = System.currentTimeMillis();
    }

    @AfterMethod
    public void dropDB() {
        //client.dropDB(dbName);
        long endTime = System.currentTimeMillis();
        long diff = endTime - startTime;
        log.info(MessageFormat.format("********* Time taken: {0} ms", diff));
    }

    /*
     * Abstract test methods to be implemented by concrete test classes.
     */
    @Test
    abstract public void testAdd() throws Exception;

    @Test
    abstract public void testRemove() throws Exception;

    @Test
    abstract public void testUpdate() throws Exception;

    @Test
    abstract public void testFetch() throws Exception;

    /**
     * Common test methods shared across test sub classes.
     */



    /**
     * This method is common code between student and course tests so kept in the base class.
     *
     * @return
     * @throws ParseException
     */
    public static List<String> testCreateService() throws ParseException, DBException {
        Service service = getTestService();
        log.info("Service : " + service);
        ServiceDAO serviceDAO = (ServiceDAO) client.getDAO(ServiceDAO.class);
        List<String> insertedIds = serviceDAO.add(new ArrayList<Service>() {{
            add(service);
        }});

        List<Service> services = serviceDAO.fetchById(insertedIds);
        Assert.assertNotNull(services);
        log.info("Service created: " + services);
        return insertedIds;
    }

    public static Service getTestService() throws ParseException {
        Service service = new Service();
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setMessage("test message");
        serviceLog.setTime(new Date());
        List<ServiceLog> serviceLogs = new ArrayList<ServiceLog>() {{add(serviceLog);}};
        service.setLogs(serviceLogs);
        Node node = new Node();
        node.setType(VmType.WEB);
        node.setNodeName("testVM");
        node.setImageName("UBUNTU-WEB-IMG");
        node.setFlavorName("m1.small");
        node.setNodeId(getRandomStr());
        List<Node> nodes = new ArrayList<Node>() {{add(node);}};
        service.setNodes(nodes);
        service.setServiceType(ServiceType.BASIC);
        service.setStatus(ServiceStatus.IN_PROGRESS);
        service.setNetworkName("net1");
        service.setTenant("admin");
        service.setName("BasicTestServ-" + getRandomStr());

        return service;
    }

    public static String getRandomStr() {
        Random rand = new Random();
        return Integer.toString(rand.nextInt());
    }
}
