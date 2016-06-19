
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

import org.joda.time.DateTimeZone;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.common.base.Strings;

/**
 * Servlet triggered by cron job in cron.xml to generate stats needed for the Experiment Hub
 */
public class HubCronJobServlet extends HttpServlet {

    public static final Logger log = Logger.getLogger(HubCronJobServlet.class.getName());
    public String adminDomainSystemSetting;



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCharacterEncoding(req, resp);

        String isLaunchedByCron = req.getHeader("X-Appengine-Cron");
        if (Strings.isNullOrEmpty(isLaunchedByCron) || !Boolean.parseBoolean(isLaunchedByCron)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            dumpStats(resp, req);
        }
    }

    private void dumpStats(HttpServletResponse resp, HttpServletRequest req) throws IOException {
        DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
        String jobId = runReportJob("cron", timeZoneForClient, req, "stats");
        // Give the backend time to startup and register the job.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();

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
        ModulesService modulesApi = ModulesServiceFactory.getModulesService();
        String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));
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
        URL url = new URL(httpScheme + "://" + backendAddress + "/hubJobExecutor");
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