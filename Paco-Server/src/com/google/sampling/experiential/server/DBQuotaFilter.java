/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance  with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

public class DBQuotaFilter implements Filter {

  private static final Logger log = Logger.getLogger(DBQuotaFilter.class.getName());

  private List<String> blockedEmails;

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException,
      ServletException {
    // TODO Auto-generated method stub
    User user = UserServiceFactory.getUserService().getCurrentUser();
    if (user != null && blockedEmails.contains(user.getEmail())) {
      log.info("Blocked User: " + user.getEmail());
      throw new ServletException("Unauthorized");
    }
    arg2.doFilter(arg0, arg1);
  }

  private boolean isDevServer() {
    return false;
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    // TODO Auto-generated method stub
    blockedEmails = new ArrayList<String>();
    blockedEmails.add("papercaked@gmail.com");
  }

}
