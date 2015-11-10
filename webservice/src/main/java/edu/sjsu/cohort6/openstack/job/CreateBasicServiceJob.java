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

import edu.sjsu.cohort6.openstack.OpenStack4JClient;
import edu.sjsu.cohort6.openstack.common.api.ServiceSpec;
import edu.sjsu.cohort6.openstack.payload.ServicePayload;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.MessageFormat;
import java.util.logging.Logger;

import static edu.sjsu.cohort6.openstack.job.JobConstants.*;

/**
 * @author rwatsh on 11/5/15.
 */
public class CreateBasicServiceJob implements Job {
    private static final Logger LOGGER = Logger.getLogger(CreateBasicServiceJob.class.getName());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        ServicePayload servicePayload = (ServicePayload) jobDataMap.get(JobConstants.SERVICE_PAYLOAD);
        if (servicePayload != null) {
            String serviceName = servicePayload.getName();
            String tenantName = servicePayload.getTenantName();
            String user = servicePayload.getUser();
            String password = servicePayload.getPassword();
            String flavorName = servicePayload.getFlavorName();
            String imageName = servicePayload.getImageName();
            String networkName = servicePayload.getNetworkName();

            OpenStack4JClient client = new OpenStack4JClient(user, password, tenantName);
            Flavor f = client.getFlavorByName(flavorName);
            Image image = client.getImageByName(imageName);
            Network net = client.getNetworkByName(networkName);
            LOGGER.info(MessageFormat.format("Creating VM with flavor {0} and image {1} and network {2}",
                    f.getName(), image.getName(), net.getName()));
            String vmName = MessageFormat.format(WEB_VM_NAME_FORMAT, serviceName, 1);
            client.startVM(new ServiceSpec(vmName, f.getId(), image.getId(), net.getId()));
        }
    }
}
