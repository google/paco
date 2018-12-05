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

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.users.UserService;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.HttpUtil;
import com.google.sampling.experiential.server.ReportJobStatusManager;
import com.pacoapp.paco.shared.util.Constants;


/**
 * Servlet that handles migration tasks for data
 *
 */
@SuppressWarnings("serial")
public class MigrationBackendServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(MigrationBackendServlet.class.getName());

  @Override
  protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.info("MIGRATE BACKEND");
    final String requestorEmail = getRequestorEmail(req);
    final String migrationJobName = HttpUtil.getParam(req, "migrationName");
    final String cursor = HttpUtil.getParam(req, "cursor");
    final String sTime = HttpUtil.getParam(req, "startTime");
    final String eTime = HttpUtil.getParam(req, "endTime");
    
    final String jobId = migrationJobName + "_" +
            DigestUtils.md5Hex(requestorEmail +
            Long.toString(System.currentTimeMillis()));
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
          DateTime startTime  = null;
          DateTime endTime = null;
          if ( sTime !=  null && eTime != null) {
            startTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.sssZ").parseDateTime(sTime);
            endTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.sssZ").parseDateTime(eTime);
          }
          if (doMigration(migrationJobName, cursor, startTime, endTime)) {
            statusMgr.completeReport(requestorEmail, jobId, Constants.LOCATION_NA);
          } else {
            statusMgr.failReport(requestorEmail, jobId, "Check server logs for stacktrace");
          }
        } catch (Throwable e) {
          final String fullStack = ExceptionUtil.getStackTraceAsString(e);
          final String string = fullStack.length()>700 ? fullStack.substring(0, 700): fullStack;
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage() +"\n" + string);
          log.severe("Could not run migration job: " + e.getMessage());

          log.severe("stacktrace: " + fullStack);
        }
      }
    });
    thread2.start();
    log.info("Leaving migration backend");
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }


  private boolean doMigration(String name, String cursor, DateTime startTime, DateTime endTime) {
    MigrationJob job = MigrationLookupTable.getMigrationByName(name);
    if (job != null) {
      return job.doMigration(cursor, startTime, endTime);
    }
    return false;
  }

  private String getRequestorEmail(HttpServletRequest req) {
    String whoParam = HttpUtil.getParam(req, "who");
    if (whoParam == null) {
      throw new IllegalArgumentException("Must pass the who param");
    }
    return whoParam.toLowerCase();
  }

}
