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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.apphosting.api.ApiProxy;
import com.google.common.collect.Lists;

/**
 * Servlet that answers queries for Events.
 *
 * @author Bob Evans
 *
 */
public class TestModuleCallServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(TestModuleCallServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);
  private static final String REPORT_WORKER = "reportworker";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      runTests(resp, req);
    }
  }

  private void runTests(HttpServletResponse resp, HttpServletRequest req) throws IOException {
    List<String> results = Lists.newArrayList();
    
    PacoModule backendModule = new PacoModule(REPORT_WORKER, req.getServerName());    
    String address = backendModule.getAddress();
    //address = "245-dot-" + REPORT_WORKER + "-dot-quantifiedself.appspot.com";
//    ModulesService moduleApi = ModulesServiceFactory.getModulesService();
//    address = moduleApi.getCurrentVersion() + 
//            "-dot-" + 
//            REPORT_WORKER + 
//            "-dot-" + 
//            ApiProxy.getCurrentEnvironment().getAppId().replace("s~","") + 
//            ".appspot.com";
    address = "https://" + address + "/backendModuleReceiver";
    
    runTest("test 1: " + address, address, results);
//    address = address.replaceAll("-dot-", ".");
//    runTest("test 2: " + address, address, results);
//    runTest("test 2: https://www.pacoapp.com/backendModuleReceiver", "https://www.pacoapp.com/backendModuleReceiver", results);
//    runTest("test 3: http://www.pacoapp.com/backendModuleReceiver", "http://www.pacoapp.com/backendModuleReceiver", results);
//    
    writeResults(resp, results);
   
  }

  private void runTest(String testName, String address, List<String> results) {
    try {
      log.info("running test " + testName);
      results.add(testName);
      String response = "NA";
      try {
        StringBuilder buf = new StringBuilder();
        URL url = new URL(address);
        log.info("URL to backendModuleReceiver = " + url.toString());
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String appId = ApiProxy.getCurrentEnvironment().getAppId();
        log.info("AppId = " + appId);
        //connection.setRequestProperty("X-Appengine-Inbound-Appid", appId);
        // set instance follow redirects should be set to false. Only when it is false, GAE will set the header value to X-Appengine-Inbound-Appid
        connection.setInstanceFollowRedirects(false);
        buf.append("responseCode = " + connection.getResponseCode() +"\n");
        if (connection.getResponseCode() != 200) {
          buf.append("error = " + connection.getResponseMessage() + "\n");
        }
        readStream(buf, connection);
        response = buf.toString();
      } catch (SocketTimeoutException se) {
        log.info("Timed out sending to backend. ");
      } catch (MalformedURLException e) {
        log.severe("MalformedURLException: " + e.getMessage());
      }
      log.info("response = " + response);
      results.add(response);
    } catch (Exception e) {
      results.add("Exception: " + e.getMessage());
      log.log(Level.INFO,"Exception: " + e.getMessage(), e);
    }
  }

  private void writeResults(HttpServletResponse resp, List<String> results) throws IOException {
    PrintWriter writer = resp.getWriter();
    for (String result : results) {
      writer.println(result +"\n");  
    }
    writer.flush();
  }


  private String runReportJob(String address) throws IOException {
      
    return null;
  }

  private void readStream(StringBuilder buf, HttpURLConnection connection) throws IOException {
    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
    BufferedReader reader = new BufferedReader(inputStreamReader);        
    if (reader != null) {
      String line;
      while ((line = reader.readLine()) != null) {
        buf.append(line);
      }
      reader.close();          
    }
  }

 
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    setCharacterEncoding(req, resp);
    User who = AuthUtil.getWhoFromLogin();
    if (who == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      resp.getWriter().println("Nothing to do");
    }
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
