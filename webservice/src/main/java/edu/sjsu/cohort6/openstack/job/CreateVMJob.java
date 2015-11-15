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

package edu.sjsu.cohort6.openstack.job;

import edu.sjsu.cohort6.openstack.OpenStackInterface;
import edu.sjsu.cohort6.openstack.common.api.ServiceSpec;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.MessageFormat;
import java.util.logging.Logger;

import static edu.sjsu.cohort6.openstack.job.JobConstants.WEB_VM_NAME_FORMAT;

/**
 * Create VM Job.
 *
 * @author rwatsh on 11/12/15.
 */
public class CreateVMJob implements Job {
    private static final Logger LOGGER = Logger.getLogger(CreateVMJob.class.getName());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            String flavorName = jobDataMap.getString(JobConstants.FLAVOR_NAME);
            String imageName = jobDataMap.getString(JobConstants.IMAGE_NAME);
            String networkName = jobDataMap.getString(JobConstants.NETWORK_NAME);
            String serviceName = jobDataMap.getString(JobConstants.SERVICE_NAME);
            OpenStackInterface client = (OpenStackInterface) jobDataMap.get(JobConstants.OPENSTACK_CLIENT);

            Flavor f = client.getFlavorByName(flavorName);
            if (f == null) {
                throw new OpenStackJobException("Could not find flavor " + flavorName);
            }
            Image image = client.getImageByName(imageName);
            if (image == null) {
                throw new OpenStackJobException("Could not find image " + imageName);
            }
            Network net = client.getNetworkByName(networkName);
            if (net == null) {
                LOGGER.info("Could not find network " + networkName + ". Attempting to create the network.");
                net = client.createNetwork(networkName);
                if (net == null) {
                    throw new OpenStackJobException("Could not create network " + networkName);
                }
            }
            String vmName = MessageFormat.format(WEB_VM_NAME_FORMAT, serviceName, 1);
            LOGGER.info(MessageFormat.format("Creating VM {0} with flavor {1} and image {2} and network {3}",
                    vmName, f.getName(), image.getName(), net.getName()));

            Server server = client.startVM(new ServiceSpec(vmName, f.getId(), image.getId(), net.getId()));

            // check if server was created and started.
            if (server == null) {
                throw new OpenStackJobException("Failed to create server");
            } else if (!server.getStatus().value().equalsIgnoreCase("running")) {
                throw new OpenStackJobException("Failed to start server " + server.getName() +
                        ". Current status is: " + server.getStatus().value());
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
