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

import javax.jdo.PersistenceManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalTimeDAO;
import com.google.sampling.experiential.datastore.PublicExperimentList;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.SignalTime;

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
    doMigrateWork();
  }



  private void doMigrateWork() {
    //populatePublicExperimentsList();
    //setFeedbackTypeOnExperiments();
    //convertScheduleTimeLongsToSignalTimeObjects();
  }



  private void convertScheduleTimeLongsToSignalTimeObjects() {
    long t1 = System.currentTimeMillis();
    log.info("Starting convertScheduleTimes from Long to SignalTime objects");
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      javax.jdo.Query newQuery = pm.newQuery(Experiment.class);
      List<Experiment> updatedExperiments = Lists.newArrayList();
      List<Experiment> experiments = (List<Experiment>)newQuery.execute();
      for (Experiment experiment : experiments) {
        SignalSchedule schedule = experiment.getSchedule();
        if (schedule != null && schedule.getSignalTimes().isEmpty()) {
          if (schedule.getScheduleType() != SignalScheduleDAO.SELF_REPORT &&
                  schedule.getScheduleType() != SignalScheduleDAO.ESM ) {
            log.info("Converting for experiment: " + experiment.getTitle());
            List<SignalTime> signalTimes = Lists.newArrayList();
            List<Long> times = schedule.getTimes();
            for (Long long1 : times) {
              signalTimes.add(new SignalTime(null, SignalTimeDAO.FIXED_TIME,
                                             SignalTimeDAO.OFFSET_BASIS_SCHEDULED_TIME,
                                             (int)long1.longValue(),
                                             SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME,
                                             0, ""));
            }
            schedule.setSignalTimes(signalTimes);
            experiment.setSchedule(schedule);
            updatedExperiments.add(experiment);
          }
        }
      }
      pm.makePersistentAll(updatedExperiments);
    } finally  {
      pm.close();
    }
    long t2 = System.currentTimeMillis();
    long seconds = new Duration(t1, t2).getStandardSeconds();
    log.info("Done converting signal times. " + seconds + "seconds to complete");
  }



  private void setFeedbackTypeOnExperiments() {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      javax.jdo.Query newQuery = pm.newQuery(Experiment.class);
      List<Experiment> experiments = (List<Experiment>)newQuery.execute();
      for (Experiment experiment : experiments) {
        if (experiment.getFeedbackType() == null) {
          if (FeedbackDAO.DEFAULT_FEEDBACK_MSG.equals(experiment.getFeedback().get(0).getLongText())) {
            experiment.setFeedbackType(FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE);
          } else {
            experiment.setFeedbackType(FeedbackDAO.FEEDBACK_TYPE_CUSTOM);
          }
        }
      }
      pm.makePersistentAll(experiments);
    } finally  {
      pm.close();
    }

  }



  private void populatePublicExperimentsList() {
    if (!PublicExperimentList.getPublicExperiments(null).isEmpty()) {
      return;
    }
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      javax.jdo.Query newQuery = pm.newQuery(Experiment.class);
      List<Experiment> experiments = (List<Experiment>)newQuery.execute();
      PublicExperimentList.updatePublicExperimentsList(experiments, new DateTime());
    } finally  {
      pm.close();
    }


  }



  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType("application/json;charset=UTF-8");

    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);

      logPacoClientVersion(req);

      String email = getEmailOfUser(req, user);

      String shortParam = req.getParameter("short");
      String experimentsPublishedToMeParam = req.getParameter("mine");
      String selectedExperimentsParam = req.getParameter("id");
      String experimentsPublishedPubliclyParam = req.getParameter("public");
      String experimentsAdministeredByUserParam = req.getParameter("admin");

      String pacoProtocol = req.getHeader("pacoProtocol");
      if (pacoProtocol == null) {
        pacoProtocol = req.getParameter("pacoProtocol");
      }


      //String offset = req.getParameter("offset");
      String limitStr = req.getParameter("limit");
      Integer limit = null;
      if (limitStr != null) {
        try {
          limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) {
        }
      }
      if (limit != null && (limit <= 0 || limit >= EXPERIMENT_LIMIT_MAX)) {
        throw new IllegalArgumentException("Invalid limit. must be greater than 0 and less than or equal to 50");
      }
      String cursor = req.getParameter("cursor");

      String experimentsJson = null;
      ExperimentServletHandler handler;
      if (experimentsPublishedToMeParam != null) {
        handler = new ExperimentServletExperimentsForMeLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else if (shortParam != null) {
        handler = new ExperimentServletShortLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else if (selectedExperimentsParam != null) {
        handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, timezone, selectedExperimentsParam, pacoProtocol);
      } else if (experimentsPublishedPubliclyParam != null) {
        handler = new ExperimentServletExperimentsShortPublicLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else if (experimentsAdministeredByUserParam != null) {
        handler = new ExperimentServletAdminExperimentsFullLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      } else {
        handler = new ExperimentServletAllExperimentsFullLoadHandler(email, timezone, limit, cursor, pacoProtocol);
      }
      experimentsJson = handler.performLoad();
      resp.getWriter().println(scriptBust(experimentsJson));
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

  private String getEmailOfUser(HttpServletRequest req, User user) {
    String email = user != null ? user.getEmail() : null;
    if (email == null) {
      throw new IllegalArgumentException("User not logged in");
    }
    if (isDevInstance(req)) {
      if ("example@example.com".equalsIgnoreCase(email)) {
        throw new IllegalArgumentException("You need to specify a test acct to return when testing mobile clients.");
        // uncomment the line below and put in the test acct. This is necessary because the dev appengine server
        // only returns example@example.com as the user!!
        //return "<your test google acct here";
      } else {
        User currentUser = UserServiceFactory.getUserService().getCurrentUser();
        if (currentUser != null) {
          return currentUser.getEmail().toLowerCase();
        } else {
          return null;
        }
      }
    }
    return email.toLowerCase();
  }

  public static boolean isDevInstance(HttpServletRequest req) {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    readExperimentDefinitions(req, resp);
  }

  private void readExperimentDefinitions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
    DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
    String results = ExperimentJsonUploadProcessor.create().processJsonExperiments(postBodyString, AuthUtil.getWhoFromLogin(), appIdHeader, pacoVersion, timezone);
    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().write(results);
  }
}
