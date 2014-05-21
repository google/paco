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
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.Duration;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalTimeDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.SignalTime;

/**
 * Servlet that handles migration tasks for data
 *
 */
@SuppressWarnings("serial")
public class MigrationBackendServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(MigrationBackendServlet.class.getName());
  private UserService userService;


  private void doMigrations() {
    migration95();
  }

  private void migration95() {
    setFeedbackTypeOnExperiments();
    convertScheduleTimeLongsToSignalTimeObjects();
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

  private static String getParam(HttpServletRequest req, String paramName) {
    try {
      String parameter = req.getParameter(paramName);
      if (parameter == null || parameter.isEmpty()) {
        return null;
      }
      return URLDecoder.decode(parameter, "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      throw new IllegalArgumentException("Unspported encoding");
    }
  }


  private String getRequestorEmail(HttpServletRequest req) {
    String whoParam = getParam(req, "who");
    if (whoParam == null) {
      throw new IllegalArgumentException("Must pass the who param");
    }
    String requestorEmail = whoParam.toLowerCase();
    return requestorEmail;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // resp.setContentType("application/json;charset=UTF-8");

    // User user = getWhoFromLogin();

    final String requestorEmail = getRequestorEmail(req);

    final String jobId = DigestUtils.md5Hex(requestorEmail + Long.toString(System.currentTimeMillis()));

    log.info("In migrate backend for job: " + jobId);
    final ReportJobStatusManager statusMgr = new ReportJobStatusManager();
    statusMgr.startReport(requestorEmail, jobId);
    final ClassLoader cl = getClass().getClassLoader();
    final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
      @Override
      public void run() {

        log.info("MigrationBackend running");
        Thread.currentThread().setContextClassLoader(cl);
        try {
          doMigrations();
          statusMgr.completeReport(requestorEmail, jobId, "NA");
        } catch (Throwable e) {
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage());
          log.severe("Could not run migration job: " + e.getMessage());
        }
      }
    });
    thread2.start();
    log.info("Leaving migration backend");
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }


  private void redirectUserToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

}
