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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;

import com.google.sampling.experiential.server.AuthUtil;
import com.pacoapp.paco.shared.util.TimeUtil;

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
    String cursor = null;
    DateTime stDate = null;
    DateTime endDate = null;
    cursor =  req.getParameter("cursor");
   if(user != null) {
     log.info("user in mig front end" + user.getEmail());
     log.info("front end req id" + req.getSession().getId());
   }
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
      
    } else  if (AuthUtil.isUserAdmin()) { 
      String jobName = req.getParameter("name");
      String startTime = req.getParameter("startTime");
      String endTime = req.getParameter("endTime");
      try{
        DateTimeFormatter formatter  = DateTimeFormat.forPattern(TimeUtil.DATE_TIME_WITH_NO_TZ);
        if(startTime!=null && endTime != null) {
          stDate = formatter.parseDateTime(startTime);
          endDate = formatter.parseDateTime(endTime);
        }
      } catch (IllegalArgumentException e ){
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }
      String jobId = sendMigrateRequestToBackend(req, jobName, cursor, stDate, endDate);
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    } else {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private String sendMigrateRequestToBackend(HttpServletRequest req, String jobName, String cursor, DateTime startDateTime, DateTime endDateTime) throws IOException {
    req.getParameter("name");
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));

    try {
      BufferedReader reader = null;
      try {
        reader = sendToBackend(backendAddress, jobName, cursor, startDateTime, endDateTime);
      } catch (SocketTimeoutException se) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(backendAddress, jobName, cursor, startDateTime, endDateTime);
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

  private BufferedReader sendToBackend(String backendAddress, String jobName, String cursor, DateTime startDateTime, DateTime endDateTime) throws MalformedURLException, IOException {
    URL url = null;
    String scheme = "https";
    HttpURLConnection connection = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader reader = null;
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      scheme = "http";
    }
    StringBuffer urlBase = new StringBuffer(scheme + "://" + backendAddress + "/migrateBackend?who=" + AuthUtil.getWhoFromLogin().getEmail().toLowerCase() +
                                   "&migrationName=" + jobName);
    if ( cursor != null) {
      urlBase.append("&cursor="+ cursor);
    } 
    if(startDateTime != null && endDateTime != null) {
      urlBase.append("&startTime="+ startDateTime + "&endTime=" +  endDateTime);
    }
    url = new URL(urlBase.toString());
    log.info("URL to backend = " + url.toString());
    connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    connection.setReadTimeout(10000);
    inputStreamReader = new InputStreamReader(connection.getInputStream());
    reader = new BufferedReader(inputStreamReader);
    return reader;
  }
}
