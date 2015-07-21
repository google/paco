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
package com.google.sampling.experiential.server.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.sampling.experiential.server.AuthUtil;

/**
 * Servlet that handles migration tasks for data
 *
 */
@SuppressWarnings("serial")
public class MigrationFrontendServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(MigrationFrontendServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType("application/json;charset=UTF-8");

    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else if (AuthUtil.isUserAdmin()) {
      String jobName = req.getParameter("name");
      String jobId = sendMigrateRequestToBackend(req, jobName);
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    } else {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }


  private String sendMigrateRequestToBackend(HttpServletRequest req, String jobName) throws IOException {
    req.getParameter("name");
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));


    try {
      BufferedReader reader = null;
      try {
        reader = sendToBackend(backendAddress, jobName);
      } catch (SocketTimeoutException se) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(backendAddress, jobName);
      }
      if (reader != null) {
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          buf.append(line);
        }
        reader.close();
        return buf.toString();
      }
    } catch (MalformedURLException e) {
      log.severe("MalformedURLException: " + e.getMessage());
    }
    return null;
  }

  private BufferedReader sendToBackend(String backendAddress, String jobName) throws MalformedURLException, IOException {
    URL url = new URL("https://" + backendAddress + "/migrateBackend?who=" + AuthUtil.getWhoFromLogin().getEmail().toLowerCase() +
                      "&migrationName=" + jobName );
    log.info("URL to backend = " + url.toString());
    InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
    BufferedReader reader = new BufferedReader(inputStreamReader);
    return reader;
  }

}
