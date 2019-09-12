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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Strings;
import com.google.sampling.experiential.server.stats.usage.UsageStatsEntityManager;
import com.google.sampling.experiential.server.stats.usage.UsageStatsReport;
import com.pacoapp.paco.shared.model2.JsonConverter;

/**
 * Servlet that answers queries for paco Usage
 *
 *
 */
public class UsageServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(UsageServlet.class.getName());
  public String adminDomainSystemSetting;  
 
  
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } // keep this around for handy one-off jobs  
    /* else if (req.getParameter("creator") != null) {
      dumpStats(resp,req);
    }*/ else {
      String adminDomainFilter = null;
      String email = AuthUtil.getEmailOfUser(req, user);
      if (email.split("@")[1].equals(adminDomainSystemSetting)) {
        adminDomainFilter = adminDomainSystemSetting;
      }

      String json = readStats(email, adminDomainFilter);
      resp.getWriter().write(json);
    }
  }
  
  
  private String readStats(String requestorEmail, String adminDomainFilter) {
    UsageStatsEntityManager usageStatsManager = UsageStatsEntityManager.getInstance();
    UsageStatsReport usageStatsReport = usageStatsManager.getStatsReport(adminDomainFilter);
    return jsonifyEvents(usageStatsReport);
  }
  
  private String jsonifyEvents(UsageStatsReport report) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      return mapper.writeValueAsString(report);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      log.severe(e.getMessage());
    } catch (JsonMappingException e) {
      e.printStackTrace();
      log.severe(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      log.severe(e.getMessage());
    }
    return "Error could not write stats report as json";
  }

  private void dumpStats(HttpServletResponse resp, HttpServletRequest req) throws IOException {
    final User whoFromLogin = AuthUtil.getWhoFromLogin();
    boolean isSystemAdmin = UserServiceFactory.getUserService().isUserAdmin();
    if (!isSystemAdmin) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }


    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(AuthUtil.getEmailOfUser(req, whoFromLogin), timeZoneForClient, req, "stats");
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
                                 HttpServletRequest req, String reportFormat) throws IOException {
//    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
//    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));
    String serverName = req.getServerName();
    log.info("request servername = " + serverName);
    PacoModule backendModule = new PacoModule(EventServlet.REPORT_WORKER, serverName);
    
    String backendAddress = backendModule.getAddress();
     try {

      BufferedReader reader = null;
      try {
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat);
      } catch (SocketTimeoutException se) {
        log.info("Timed out sending to backend. Trying again...");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat);
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
                                       String backendAddress, String reportFormat) throws MalformedURLException, IOException {
    String httpScheme = "https";
    String localAddr = req.getLocalAddr();
    if (localAddr != null && localAddr.matches("127.0.0.1")) {
      httpScheme = "http";
    }
    URL url = new URL(httpScheme + "://" + backendAddress + "/backendReportJobExecutor?q=" +
            req.getParameter("q") +
            "&who="+AuthUtil.getWhoFromLogin().getEmail().toLowerCase() +
            "&tz=" + timeZoneForClient +
            "&reportFormat=" + reportFormat);
    log.info("URL to backend = " + url.toString());
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
    BufferedReader reader = new BufferedReader(inputStreamReader);
    return reader;
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }


  @Override
  public void init() throws ServletException {
    super.init();
    adminDomainSystemSetting = System.getProperty("com.pacoapp.adminDomain");
    if (Strings.isNullOrEmpty(adminDomainSystemSetting)) {
      adminDomainSystemSetting = "";
    }
  }
}
