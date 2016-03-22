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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.users.User;
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
    } else {
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
