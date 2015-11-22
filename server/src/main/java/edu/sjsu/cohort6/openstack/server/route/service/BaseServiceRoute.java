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

package edu.sjsu.cohort6.openstack.server.route.service;

import edu.sjsu.cohort6.openstack.common.model.Node;
import edu.sjsu.cohort6.openstack.common.model.Service;
import edu.sjsu.cohort6.openstack.common.model.ServiceStatus;
import edu.sjsu.cohort6.openstack.db.DBException;
import edu.sjsu.cohort6.openstack.db.mongodb.ServiceDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class with common methods across all /services/* route handlers.
 *
 * @author rwatsh on 11/16/15.
 */
public abstract class BaseServiceRoute {
    /**
     * Update cumulative service status based on status of VMs that are part of the service.
     *
     * @param s service instance.
     * @param serviceDAO
     */
    protected void updateCumulativeServiceStatus(Service s, ServiceDAO serviceDAO) throws DBException {
        List<Node> nodes = s.getNodes();
        if (nodes != null) {
            boolean failed = false;
            boolean inProgress = false;
            for (Node n: nodes) {
                if(n.getNodeStatus().equalsIgnoreCase("error")) {
                    s.setStatus(ServiceStatus.FAILED);
                    failed = true;
                    break;
                } else if (n.getNodeStatus().equalsIgnoreCase("build")) {
                    inProgress = true;
                    // Service will already be marked in IN_PROGRESS state so no need to set it.
                }
            }
            // If none failed and none are in build state then assume service is ready.
            /*
             * If someone goes directly to openstack CLI or UI and say deletes the VM, then we will not be able
             * to tell until the VM is deleted or migrated. If VM is being rebooted or stopped, then we still
             * consider service to be successful.
             */
            if(!failed && !inProgress) {
                s.setStatus(ServiceStatus.READY);
            }
            serviceDAO.update(new ArrayList<Service>(){{add(s);}});
        }
    }
}
