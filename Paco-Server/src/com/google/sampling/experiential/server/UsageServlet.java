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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;

/**
 * Servlet that answers queries for paco Usage
 *
 *
 */
public class UsageServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(UsageServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      dumpStats(resp, req);
    }
  }

  private void dumpStats(HttpServletResponse resp, HttpServletRequest req) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String adminDomainFilter = req.getParameter("adminDomainFilter");
    String jobId = runReportJob(loggedInuser, timeZoneForClient, req, "stats", adminDomainFilter);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    resp.sendRedirect("/jobStatus?jobId=" + jobId);

  }

  /**
   * Triggers a backend instance call to start the potentially-long-running job
   *
   * @param anon
   * @param loggedInuser
   * @param timeZoneForClient
   * @param req
   * @param reportFormat
   * @param adminDomainFilter
   * @return the jobId to check in on the status of this background job
   * @throws IOException
   */
  private String runReportJob(String loggedInuser, DateTimeZone timeZoneForClient,
                                 HttpServletRequest req, String reportFormat, String adminDomainFilter) throws IOException {
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));
     try {

      BufferedReader reader = null;
      try {
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, adminDomainFilter);
      } catch (SocketTimeoutException se) {
        log.info("Timed out sending to backend. Trying again...");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, adminDomainFilter);
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

  private BufferedReader sendToBackend(DateTimeZone timeZoneForClient, HttpServletRequest req,
                                       String backendAddress, String reportFormat, String adminDomainFilter) throws MalformedURLException, IOException {
    String httpScheme = "https";
    String localAddr = req.getLocalAddr();
    if (localAddr != null && localAddr.matches("127.0.0.1")) {
      httpScheme = "http";
    }
    URL url = new URL(httpScheme + "://" + backendAddress + "/backendReportJobExecutor?q=" +
            req.getParameter("q") +
            "&who="+AuthUtil.getWhoFromLogin().getEmail().toLowerCase() +
            "&tz=" + timeZoneForClient +
            "&reportFormat=" + reportFormat +
            "&adminDomainFilter=" + adminDomainFilter);
    log.info("URL to backend = " + url.toString());
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
    BufferedReader reader = new BufferedReader(inputStreamReader);
    return reader;
  }

  private String stripQuotes(String parameter) {
    if (parameter == null) {
      return null;
    }
    if (parameter.startsWith("'") || parameter.startsWith("\"")) {
      parameter = parameter.substring(1);
    }
    if (parameter.endsWith("'") || parameter.endsWith("\"")) {
      parameter = parameter.substring(0, parameter.length() - 1);
    }
    return parameter;
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
