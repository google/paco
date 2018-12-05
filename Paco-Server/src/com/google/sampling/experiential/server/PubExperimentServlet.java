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
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.comm.Outcome;

/**
 * Servlet that answers requests for experiments.
 *
 * Only used by the Android client to get the current list of experiments.
 *
 * @author Bob Evans
 *
 */
@SuppressWarnings("serial")
public class PubExperimentServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(PubExperimentServlet.class.getName());



  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }



  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType("application/json;charset=UTF-8");

      DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
      log.info("Timezone is computed to be: " + timezone.toString());
      logPacoClientVersion(req);

      User user = AuthUtil.getWhoFromLogin();
      String email = null;
      if (user != null) {
        email = AuthUtil.getEmailOfUser(req, user);
      } else {
        email = req.getRemoteAddr();
      }

      String selectedExperimentsParam = req.getParameter("id");

      String pacoProtocol = req.getHeader("pacoProtocol");
      if (pacoProtocol == null) {
        pacoProtocol = req.getParameter("pacoProtocol");
      }

      String experimentsJson = null;
      ExperimentServletHandler handler = null;
      if (selectedExperimentsParam != null) {
        handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, timezone, selectedExperimentsParam, pacoProtocol);
      }
      if (handler != null) {
        log.info("Loading experiments...");
        experimentsJson = handler.performLoad();
        resp.getWriter().println(scriptBust(experimentsJson));
      } else {
        resp.getWriter().println(scriptBust("Unrecognized parameters!"));
      }
  }


  private void logPacoClientVersion(HttpServletRequest req) {
    String pacoVersion = req.getHeader("paco.version");
    if (pacoVersion != null) {
      log.info("Paco version of request = " + pacoVersion);
    }
  }

  private String scriptBust(String experimentsJson) {
    // TODO add scriptbusting prefix to this and client code.
    return experimentsJson;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json;charset=UTF-8");
    DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
    logPacoClientVersion(req);

    User user = AuthUtil.getWhoFromLogin();
    String email = null;
    if (user != null) {
      email = AuthUtil.getEmailOfUser(req, user);
    } else {
      email = req.getRemoteAddr();
    }

    processJsonUpload(req, resp, email);
  }

  private void processJsonUpload(HttpServletRequest req, HttpServletResponse resp, String email) throws IOException {
    String postBodyString;
    try {
      postBodyString = org.apache.commons.io.IOUtils.toString(req.getInputStream(), "UTF-8");
    } catch (IOException e) {
      log.info("IO Exception reading post data stream: " + e.getMessage());
      throw e;
    }
    if (postBodyString.equals("")) {
      throw new IllegalArgumentException("Empty Post body");
    }

    String appIdHeader = req.getHeader("http.useragent");
    String pacoVersion = req.getHeader("paco.version");
    log.info("Paco version = " + pacoVersion);

    String results = EventJsonUploadProcessor.create().processJsonEvents(postBodyString, email, appIdHeader, pacoVersion);
    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().write(results);
  }



  public List<Outcome> createErrorOutcome(String msg) {
    Outcome outcome = new Outcome(0, msg);
    List<Outcome> outcomes = Lists.newArrayList(outcome);
    return outcomes;
  }

}
