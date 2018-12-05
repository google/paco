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
import com.google.common.base.Strings;
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
public class ExperimentServlet extends HttpServlet {

  private static final int EXPERIMENT_LIMIT_MAX = 50;
  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  public static final String DEV_HOST = "<Your machine name here>";



  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }



  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json;charset=UTF-8");

    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
      log.info("Timezone is computed to be: " + timezone.toString());
      RequestProcessorUtil.logPacoClientVersion(req);

      String email = AuthUtil.getEmailOfUser(req, user);

      String experimentsPublishedToMeParam = req.getParameter("mine");
      String selectedExperimentsParam = req.getParameter("id");
      String experimentsPublishedPubliclyParam = req.getParameter("public");
      String experimentsAdministeredByUserParam = req.getParameter("admin");
      String experimentsJoinedByMeParam = req.getParameter("joined");
      String experimentsPopularParam = req.getParameter("popular");
      String experimentsNewParam = req.getParameter("new");

      String pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsStr(req);

      // String offset = req.getParameter("offset");
      String limitStr = req.getParameter("limit");
      Integer limit = null;
      if (limitStr != null) {
        try {
          limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) {
        }
      }
      // if (limit != null && (limit <= 0 || limit >= EXPERIMENT_LIMIT_MAX)) {
      // throw new IllegalArgumentException("Invalid limit. must be greater
      // than 0 and less than or equal to 50");
      // }
      String cursor = req.getParameter("cursor");

      String experimentsJson = null;
      ExperimentServletHandler handler;
      if (experimentsPublishedToMeParam != null) {
        handler = new ExperimentServletExperimentsForMeLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else if (selectedExperimentsParam != null) {
        handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, timezone, selectedExperimentsParam,
                                                                          pacoProtocol);
      } else if (experimentsPublishedPubliclyParam != null) {
        handler = new ExperimentServletExperimentsShortPublicLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } /*
         * else if (experimentsAdministeredByUserParam != null &&
         * experimentsJoinedByMeParam != null) { handler = new
         * ExperimentServletAdminAndJoinedExperimentsShortLoadHandler(email,
         * timezone, limit, cursor, pacoProtocol); }
         */else if (experimentsJoinedByMeParam != null) {
        handler = new ExperimentServletJoinedExperimentsShortLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else if (experimentsAdministeredByUserParam != null) {
        String sortColumn = req.getParameter("sortColumn");
        String sortOrder = req.getParameter("sortOrder");
        handler = new ExperimentServletAdminExperimentsFullLoadHandler(email, timezone, limit, cursor, pacoProtocol,
                                                                       sortColumn, sortOrder);
      } else if (experimentsPopularParam != null) {
        handler = new ExperimentServletExperimentsPopularLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else if (experimentsNewParam != null) {
        handler = new ExperimentServletExperimentsNewLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else {
        handler = null; // new
                        // ExperimentServletAllExperimentsFullLoadHandler(email,
                        // timezone, limit, cursor, pacoProtocol);
      }
      if (handler != null) {
        log.info("Loading experiments...");
        experimentsJson = handler.performLoad();
        resp.getWriter().println(scriptBust(experimentsJson));
      } else {
        resp.getWriter().println(scriptBust("Unrecognized parameters!"));
      }

    }
  }


  

  private String scriptBust(String experimentsJson) {
    // TODO add scriptbusting prefix to this and client code.
    return experimentsJson;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json;charset=UTF-8");
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
      RequestProcessorUtil.logPacoClientVersion(req);
      String email = AuthUtil.getEmailOfUser(req, user);

      String delete = req.getParameter("delete");
      if (!Strings.isNullOrEmpty(delete)) {
        String selectedExperimentsParam = req.getParameter("id");
        if (Strings.isNullOrEmpty(selectedExperimentsParam)) {
          List<Outcome> outcomes = createErrorOutcome("No experiment ids specified for deletion");
          resp.getWriter().println(ExperimentJsonUploadProcessor.toJson(outcomes));
        } else {
          resp.getWriter().println(ExperimentJsonUploadProcessor.toJson(deleteExperiments(email,
                                                                                          selectedExperimentsParam)));
        }
      } else {
        readExperimentDefinitions(req, resp);
      }
    }
  }



  public List<Outcome> createErrorOutcome(String msg) {
    Outcome outcome = new Outcome(0, msg);
    List<Outcome> outcomes = Lists.newArrayList(outcome);
    return outcomes;
  }

  private List<Outcome> deleteExperiments(String email, String selectedExperimentsParam) {
    ExperimentService expService = ExperimentServiceFactory.getExperimentService();
    List<Long> experimentIds = ExperimentServletSelectedExperimentsFullLoadHandler.parseExperimentIds(selectedExperimentsParam);
    if (experimentIds.isEmpty()) {
      return createErrorOutcome("No experiment ids specified for deletion");
    }
    Outcome outcome = new Outcome();
    List<Outcome> outcomeList = Lists.newArrayList();
    outcomeList.add(outcome);
    try {
      final Boolean deleteExperimentsResult = expService.deleteExperiments(experimentIds, email);
      if (!deleteExperimentsResult) {
        outcome.setError("Could not delete experiments. Rolled back.");
      }
    } catch (Exception e) {
      outcome.setError("Could not delete experiments. Rolled back. Error: " + e.getMessage());
    }

    return outcomeList;
  }



  private void readExperimentDefinitions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String postBodyString;
    try {
      postBodyString = org.apache.commons.io.IOUtils.toString(req.getInputStream(), "UTF-8");
    } catch (IOException e) {
      final String msg = "IO Exception reading post data stream: " + e.getMessage();
      log.info(msg);
      List<Outcome> outcomes = createErrorOutcome(msg);
      resp.getWriter().println(ExperimentJsonUploadProcessor.toJson(outcomes));
      return;
    }

    if (postBodyString.equals("")) {
      List<Outcome> outcomes = createErrorOutcome("Empty Post body");
      resp.getWriter().println(ExperimentJsonUploadProcessor.toJson(outcomes));
      return;
    }

    String appIdHeader = req.getHeader("http.useragent");
    String pacoVersion = req.getHeader("paco.version");
    log.info("Paco version = " + pacoVersion);
    DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
    final User whoFromLogin = AuthUtil.getWhoFromLogin();
    String results = ExperimentJsonUploadProcessor.create().processJsonExperiments(postBodyString, whoFromLogin, appIdHeader, pacoVersion, timezone);
    resp.getWriter().write(results);
  }
}
