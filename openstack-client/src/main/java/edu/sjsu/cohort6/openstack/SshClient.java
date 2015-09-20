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

package edu.sjsu.cohort6.openstack;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * SSH Client using Ganymed SSH-2 for Java library.
 *
 * @see {@link http://www.ganymed.ethz.ch/ssh2/ Ganymed}
 *
 * @author rwatsh
 */
public class SshClient {

    public List<String> executeCommand(String userName, String password, String host, int port, String command) throws IOException {
        Connection connection = null;

        try {
            connection = connectTo(host, userName, password, port);
            return executeCommand(connection, command);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private Connection connectTo(String host, String userName, String password, int port) throws IOException {
        Connection connection = new Connection(host, port);
        connection.connect();
        connection.authenticateWithPassword(userName, password);

        return connection;
    }

    private List<String> executeCommand(Connection connection, String command) throws IOException {
        List<String> result = new LinkedList<>();
        Session session = null;

        try {
            session = connection.openSession();
            session.execCommand(command);
            InputStream stdout = new StreamGobbler(session.getStdout());

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stdout))) {
                String line = br.readLine();
                while (line != null) {
                    result.add(line);
                    line = br.readLine();
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }
}
