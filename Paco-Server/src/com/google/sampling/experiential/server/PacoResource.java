/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.sampling.experiential.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class PacoResource extends ServerResource {
  protected DAO dao = DAO.getInstance();
  protected String user;

  @Override
  protected void doInit() throws ResourceException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn() == false) {
      throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    user = userService.getCurrentUser().getEmail();
  }
}
