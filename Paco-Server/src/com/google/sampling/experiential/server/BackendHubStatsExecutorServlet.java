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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.hub.HubStatsCronJob;
import com.google.appengine.api.ThreadManager;

/**
 * Servlet that receives request from frontend to start csv report job.
 *
 * Runs on backend.
 *
 * @author Bob Evans
 *
 */
@SuppressWarnings("serial")
public class BackendHubStatsExecutorServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(BackendReportJobExecutorServlet.class.getName());
    private String defaultAdmin = "bobevans@google.com";
    private List<String> adminUsers = Lists.newArrayList(defaultAdmin);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("BackendHubStatsExecutorServlet servlet was called");
        log.info(req.getHeader("X-Appengine-Inbound-Appid"));

        runStats(req, resp);
    }

    private void runStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");

        log.info("In runStats for hub");
        final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
            @Override
            public void run(){
                final ClassLoader cl = getClass().getClassLoader();
               log.info("HusStatsCronJob thread running");
               Thread.currentThread().setContextClassLoader(cl);
               try{
                   HubStatsCronJob.getInstance().run();
               } catch (Throwable e) {
                log.severe("Could not complete hub job: " + e.getMessage());
                e.printStackTrace();
            }
           }
        });
        thread2.start();
        log.info("Leaving runStats for hub");
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println();
    }
}
