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
import java.net.HttpURLConnection;
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
import com.google.appengine.api.utils.SystemProperty;
import com.google.sampling.experiential.server.AuthUtil;

/**
 * Servlet that retrieves data for making visualizations
 *
 */
@SuppressWarnings("serial")
public class VizDataRetrieverServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(VizDataRetrieverServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType("application/json;charset=UTF-8");
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else  if (AuthUtil.isUserAdmin()) { 
      String jobName = req.getParameter("jname");
      String jobType = req.getParameter("jtype");
      String expId = req.getParameter("expId");
      String jobId = sendMigrateRequestToBackend(req, jobName, jobType, expId);
      if (jobId != null  && !jobId.equals("")) {
        resp.sendRedirect("/jobStatus?jobId=" + jobId);
      }
    } else {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private String sendMigrateRequestToBackend(HttpServletRequest req, String jobName, String jobType, String expId) throws IOException {
    
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));

    try {
      BufferedReader reader = null;
      try {
        reader = sendToBackend(backendAddress, jobName, jobType, expId);
      } catch (SocketTimeoutException se) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
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

  private BufferedReader sendToBackend(String backendAddress, String jobName, String jobType, String expId) throws MalformedURLException, IOException {
    URL url = null;
    String scheme = "https";
    HttpURLConnection connection = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader reader = null;
    StringBuffer urlBase = null;
    String whoFromServer = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      scheme = "http";
    }
    if (jobType != null && jobType.equals("1")) {
      urlBase = new StringBuffer(scheme + "://" + backendAddress + "/csReports?who=" + whoFromServer +
                                 "&name=" + jobName + "&expId="+ expId);
    } else {
      urlBase = new StringBuffer(scheme + "://" + backendAddress + "/csStoredProc?who=" + whoFromServer +
                                 "&name=" + jobName + "&expId="+ expId);
    }

    url = new URL(urlBase.toString());
    log.info("URL to backend = " + url.toString());
    connection = (HttpURLConnection) url.openConnection();
    
    connection.setInstanceFollowRedirects(false);
    connection.setReadTimeout(10000);
    connection.setDoInput(true);
    inputStreamReader = new InputStreamReader(connection.getInputStream());
    reader = new BufferedReader(inputStreamReader);
    return reader;
  }
}
