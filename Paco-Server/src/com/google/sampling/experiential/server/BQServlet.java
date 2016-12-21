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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.SQLQuery1;

/**
 * Servlet that answers queries for Events.

 */
public class BQServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(BQServlet.class.getName());


  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    setCharacterEncoding(request, response);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(request, response);
    } else {
      response.setContentType("text/html");
      response.setCharacterEncoding("UTF-8");
      PrintWriter writer = response.getWriter();
      writer.println("<!doctype html><html><head>");
      writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
      writer.println("<title> PACO </title>");
      writer.println("</head><body>");
    
    }
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    log.info("BQ Started");
    ObjectMapper om = new ObjectMapper();

    setCharacterEncoding(req, resp);
    User who = AuthUtil.getWhoFromLogin();
    //TODO
//    if (who == null) {
//      AuthUtil.redirectUserToLogin(req, resp);
//    } else {
    StringBuilder buffer = new StringBuilder();
    BufferedReader reader = req.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
        buffer.append(line);
    }
    //TODO this has to be passed in processREquest method
    User whoFromLogin = AuthUtil.getWhoFromLogin();
 
    String jsonRequest = buffer.toString();
    DateTimeZone clientTz = TimeUtil.getTimeZoneForClient(req);
//    DateTimeZone modTime = TimeZone.setDefault();
//    DateTimeZone clientTz =
    
    SQLQuery1 sqlObj = om.readValue(jsonRequest, SQLQuery1.class);
    DataContextFactory dcf = new DataContextFactory();
    List<EventDAO> evtList = dcf.processRequest(sqlObj,"bobevans999@gmail.com", clientTz);
    display(resp.getWriter(), evtList);
    log.info("BQ Ended");
//    }
  }

  private void display(PrintWriter pw, List<EventDAO> events){
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    for (EventDAO e: events){
      
      pw.println("Event id->"+ e.getId());
      pw.println("Event experiment id->"+ e.getExperimentId());
      pw.println("ExperimentGroup Name->"+ e.getExperimentGroupName());
      pw.println("Event Experiment NAme->"+ e.getExperimentName());
//      try {
//        pw.println("Event Insert Time->"+ sdf.parse(e.getWhen().toString()));
//      } catch (ParseException e1) {
//        // TODO Auto-generated catch block
//        e1.printStackTrace();
//      }
      pw.println("Event Insert Time->"+ e.getWhen());
      if(e.getResponseTime()!=null)
        pw.println("Event RespondedTime->"+ e.getResponseTime());
      else
        pw.println("Event RespondedTime->"+ "");
        
    }
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
