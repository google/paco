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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;
import org.json.HTTP;

import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class BackendModuleReceiverServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(BackendModuleReceiverServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    log.info("BackendModuleReceiverServlet was called");
    String appIdHeader = req.getHeader("X-Appengine-Inbound-Appid");
    log.info("Appid: " + appIdHeader);
    
    StringBuffer out = new StringBuffer();
    Enumeration headerNames = req.getHeaderNames();
    while(headerNames.hasMoreElements()) {
      String headerName = (String)headerNames.nextElement();
      out.append(headerName + ":");
      out.append(req.getHeader(headerName) + "\n");
    }
    log.info("request headers = " + out.toString());
    String systemAppId = ApiProxy.getCurrentEnvironment().getAppId().replace("s~", "");
    log.info("System Appid: " + systemAppId);
    if (!systemAppId.equals(appIdHeader)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      log.info("all good");
    }
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println("OK");
  }

}
