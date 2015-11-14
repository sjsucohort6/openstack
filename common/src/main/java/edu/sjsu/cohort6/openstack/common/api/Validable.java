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

package edu.sjsu.cohort6.openstack.common.api;

import java.text.MessageFormat;

/**
 * @author rwatsh on 11/7/15.
 */
public abstract class Validable {
    public abstract boolean isValid() throws ValidationException;

    protected boolean isReqd(Object obj)  throws ValidationException {
        if (obj == null) {
            throw new ValidationException(MessageFormat.format("{0} cannot be null", obj));
        }
        if (obj instanceof String) {
            if (((String) obj).isEmpty()) {
                throw new ValidationException(MessageFormat.format("{0} cannot be blank", obj));
            }
        }
        return true;
    }
}
