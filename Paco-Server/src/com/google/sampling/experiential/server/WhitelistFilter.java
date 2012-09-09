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
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sampling.experiential.shared.Whitelist;

public class WhitelistFilter implements Filter {

  private Whitelist whitelist;
  private static final Logger log = Logger.getLogger(WhitelistFilter.class.getName());

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException,
      ServletException {
    User user = UserServiceFactory.getUserService().getCurrentUser();
    if (!isDevServer((HttpServletRequest) arg0) && (user == null)) {
      log.info("Error logging in from: " + arg0.getRemoteAddr() +" user: " + (user != null ? user.getEmail() : "not logged in"));
      
      HttpServletResponse resp = (HttpServletResponse)arg1;
      String loginUrl = UserServiceFactory.getUserService().createLoginURL(((HttpServletRequest)arg0).getRequestURL().toString(), "google.com");
      resp.sendRedirect(loginUrl);
    } else if (!whitelist.allowed(user.getEmail())) {
      ((HttpServletResponse)arg1).sendError(HttpStatus.SC_FORBIDDEN);
    } else {
      log.info("Allowing user: " + user.getEmail());
      arg2.doFilter(arg0, arg1);
    }
  }

  private boolean isDevServer(HttpServletRequest arg0) {
    return ExperimentServlet.isDevInstance(arg0);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    whitelist = new Whitelist();
  }

}
