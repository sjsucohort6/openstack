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

package edu.sjsu.cohort6.openstack.rest.exception;

/**
 * Created by rwatsh on 9/23/15.
 */
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InternalErrorException  extends WebApplicationException {
    public InternalErrorException(){
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    public InternalErrorException(Throwable throwable){
        super(Response.Status.INTERNAL_SERVER_ERROR.toString(), throwable);
    }
}
