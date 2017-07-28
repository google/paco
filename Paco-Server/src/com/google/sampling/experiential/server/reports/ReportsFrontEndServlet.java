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
package com.google.sampling.experiential.server.reports;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.server.AuthUtil;

/**
 * Servlet that retrieves data for making visualizations
 *
 */
@SuppressWarnings("serial")
public class ReportsFrontEndServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(ReportsFrontEndServlet.class.getName());
  private static final String REPORT_WORKER = "reportworker";
  private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType(JSON_CONTENT_TYPE);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else { 
      ReportRequest reportRequestObj = new ReportRequest(req, user.getEmail().toLowerCase(), REPORT_WORKER);
      ReportRequestProcessor rrp = new ReportRequestProcessor();
      String jobId = rrp.sendReportRequest(reportRequestObj);
      if (jobId != null  && !jobId.equals("")) {
        resp.sendRedirect(ReportRequestProcessor.getUrlwithJobId(jobId));
      }
    }
  }
 }
