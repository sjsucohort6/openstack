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

package edu.sjsu.cohort6.openstack.test;

import edu.sjsu.cohort6.openstack.client.SshClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by rwatsh on 9/20/15.
 */
public class SshClientTest {
    @Test
    public void executeCommand() throws IOException {
        // Replace userName, password and host with your specific values
        String userName = "root";
        String password = "CMPE-283";
        String host = "localhost";
        int port = 2022;

        SshClient sshClient = new SshClient();
        String command = "source ~/keystonerc_admin; nova quota-show";
        List<String> actual = sshClient.executeCommand(userName, password, host, port, command);

        for (String s: actual) {
            System.out.println(s);
        }

        Assert.assertFalse(actual.isEmpty(), "The list should contain a few files");
    }
}
