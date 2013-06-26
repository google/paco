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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;

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

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  public static final String DEV_HOST = "<Your machine name here>";
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType("application/json;charset=UTF-8");

    User user = getWhoFromLogin();

    if (user == null) {
      redirectUserToLogin(req, resp);
    } else {
      String tz = req.getParameter("tz");
      logPacoClientVersion(req);

      String shortParam = req.getParameter("short");
      String selectedExperimentsParam = req.getParameter("id");
      String experimentsJson = null;
      if (shortParam != null) {
        experimentsJson = performShortLoad(req, resp, user, tz);
      } else if (selectedExperimentsParam != null && !selectedExperimentsParam.isEmpty()) {
        experimentsJson = performSelectedExperimentsFullLoad(req, resp, user, tz, selectedExperimentsParam);
      } else {
        experimentsJson = performAllExperimentsFullLoad(req, resp, user, tz);
      }

      resp.getWriter().println(scriptBust(experimentsJson));
    }
  }

  private void redirectUserToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
  }

  private void logPacoClientVersion(HttpServletRequest req) {
    String pacoVersion = req.getHeader("paco.version");
    if (pacoVersion != null) {
      log.info("Paco version of request = " + pacoVersion);
    }
  }

  private String performShortLoad(HttpServletRequest req, HttpServletResponse resp, User user, String tz) {
    List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(req, user, tz);
    return JsonConverter.shortJsonify(availableExperiments);
  }

  private String performSelectedExperimentsFullLoad(HttpServletRequest req, HttpServletResponse resp, User user, String tz,
                                                    String selectedExperimentsParam) {
    List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(req, user, tz);
    HashMap<Long, Long> experimentIds = parseExperimentIds(selectedExperimentsParam);
    return loadSelectedExperiments(experimentIds, availableExperiments);
  }
  
  private String performAllExperimentsFullLoad(HttpServletRequest req, HttpServletResponse resp, User user, String tz) {
    ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
    String experimentsJson = cacheHelper.getExperimentsJsonForUser(user.getUserId());
    if (experimentsJson != null) {
      log.info("Got cached experiments for " + user.getEmail());
    } else {
      log.info("No cached experiments for " + user.getEmail());
      List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(req, user, tz);
      experimentsJson = JsonConverter.jsonify(availableExperiments);
      cacheHelper.putExperimentJsonForUser(user.getUserId(), experimentsJson); 
    } 
    return experimentsJson;
  }

  private List<ExperimentDAO> getExperimentsAvailableToUser(HttpServletRequest req, User user, String tz) {
    List<ExperimentDAO> joinableExperiments = getJoinableExperiments(tz);
    String email = getEmailOfUser(req, user);
    List<ExperimentDAO> availableExperiments = null;
    if (joinableExperiments == null) {
      joinableExperiments = Lists.newArrayList();
      availableExperiments = joinableExperiments;        
    } else {
      availableExperiments = ExperimentRetriever.getSortedExperimentsAvailableToUser(joinableExperiments, email);        
    }
    ExperimentRetriever.removeSensitiveFields(availableExperiments);
    return availableExperiments;
  }
  
  private List<ExperimentDAO> getJoinableExperiments(String tz) {
    ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
    List<ExperimentDAO> experiments = cacheHelper.getJoinableExperiments(tz);
    log.info("joinable experiments " + ((experiments != null) ? Integer.toString(experiments.size()) : "none"));
    return experiments;
  }

  private String scriptBust(String experimentsJson) {
    // TODO add scriptbusting prefix to this and client code.
    return experimentsJson;
  }
  
  private String getEmailOfUser(HttpServletRequest req, User user) {
    String email = user != null ? user.getEmail() : null;
    if (email == null && isDevInstance(req)) {
      email = "<put your email here to test in developor mode>";
    }
    if (email == null) {
      throw new IllegalArgumentException("You must login!");
    }
    return email.toLowerCase();
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

  public static boolean isDevInstance(HttpServletRequest req) {
    try {
      return DEV_HOST.equals(InetAddress.getLocalHost().toString());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return false;
  }
  
  private HashMap<Long,Long> parseExperimentIds(String expStr) {
    HashMap<Long,Long> experimentIds = new HashMap<Long, Long>();
    Iterable<String> strIds = Splitter.on(",").trimResults().split(expStr);
    for (String id : strIds) {
      Long experimentId = extractExperimentId(id);
      if (!experimentId.equals(new Long(-1))) {
        experimentIds.put(experimentId, null);
      }
    }
    return experimentIds;
  }

  private Long extractExperimentId(String expStr) {
    try {
      Long experimentId = Long.parseLong(expStr, 10);
      return experimentId;
    } catch (NumberFormatException e) {
      log.severe("Invalid experiment id " + expStr + " sent to server.");
      return new Long(-1);
    }
  }

  private String loadSelectedExperiments(HashMap<Long,Long> experimentIds, List<ExperimentDAO> availableExperiments) {
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (ExperimentDAO experiment : availableExperiments) {
      if (experimentIds.containsKey(experiment.getId())) {
        experiments.add(experiment);
      }
    }
    if (experiments.isEmpty()) {
      log.severe("Experiment id's " + experimentIds + " are all invalid.  No experiments were fetched from server.");
      return "";
    }
    return JsonConverter.jsonify(experiments);
  }

}
