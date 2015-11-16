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

package edu.sjsu.cohort6.openstack.server.payload;

import edu.sjsu.cohort6.openstack.common.api.Validable;
import edu.sjsu.cohort6.openstack.common.api.ValidationException;
import edu.sjsu.cohort6.openstack.common.model.VmType;
import lombok.Data;

/**
 * @author rwatsh on 11/15/15.
 */
@Data
public class NodePayload extends Validable {
    private String flavorName;
    private String imageName;
    private VmType type;

    @Override
    public boolean isValid() throws ValidationException {
        return isReqd(flavorName) && isReqd(imageName) && isReqd(type);
    }
}
