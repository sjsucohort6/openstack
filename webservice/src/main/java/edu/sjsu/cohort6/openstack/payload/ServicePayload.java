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

package edu.sjsu.cohort6.openstack.payload;

import edu.sjsu.cohort6.openstack.common.api.Validable;
import edu.sjsu.cohort6.openstack.common.api.ValidationException;
import lombok.Builder;
import lombok.Data;

import java.text.MessageFormat;

/**
 * @author rwatsh on 11/7/15.
 */
@Data
@Builder
public  class ServicePayload implements Validable {
    private String name;
    private String flavorName;
    private String imageName;
    private String networkName;
    private String tenantName;
    private String user;
    private String password;

    @Override
    public boolean isValid() throws ValidationException {
        return isReqd(name) && isReqd(flavorName) && isReqd(imageName) && isReqd(networkName)
                && isReqd(tenantName) && isReqd(user) && isReqd(password);
    }

    private boolean isReqd(String name)  throws ValidationException {
        if (name == null || name.isEmpty()) {
            throw new ValidationException(MessageFormat.format("{0} cannot be null or empty", name));
        }
        return true;
    }
}
