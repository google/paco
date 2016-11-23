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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Datasets;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableRow;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.JobStatistics.LoadStatistics;
import com.google.cloud.bigquery.TableId;
//import com.google.cloud.bigquery.*;
import com.google.common.collect.Lists;

/**
 * Servlet that answers queries for Events.
 *
 * @author Bob Evans
 *
 */
public class BQServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(EventServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);

  public InsertAllResponse insertAll(String datasetName, String tableName) {
    // [START insertAll]
    TableId tableId = TableId.of(datasetName, tableName);
    // Values of the row to insert
    Map<String, Object> rowContent = new HashMap<>();
    rowContent.put("firstName", "ind");
    // Bytes are passed in base64
    rowContent.put("lastName", "meyy"); // 0xA, 0xD, 0xD, 0xE, 0xD in base64
    // Records are passed as a map
    //Map<String, Object> recordsContent = new HashMap<>();
    //recordsContent.put("stringField", "Hello, World!");
    rowContent.put("middleName", "mid");
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
//    System.out.println("sdfsd"+tableId.getTable());
    InsertAllResponse response = bigquery.insertAll(InsertAllRequest.newBuilder(tableId)
        .addRow("rowId", rowContent)
        // More rows can be added in the same RPC by invoking .addRow() on the builder
        .build());
    if (response.hasErrors()) {
      // If any of the insertions failed, this lets you inspect the errors
      for (Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
        // inspect row error
        System.out.println(entry.getValue());
      }
    }
    // [END insertAll]
    return response;
  }
  
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
//      writer.println(
//          "<link type=\"text/css\" rel=\"stylesheet\" href=\"" + GWT_MODULE_NAME + ".css\">");
//      writer.println("<script type=\"text/javascript\" language=\"javascript\" " + "src=\""
//          + GWT_MODULE_NAME + "/" + GWT_MODULE_NAME + ".nocache.js\"></script>");
      writer.println("</head><body>");
      UserService userService = UserServiceFactory.getUserService();
      InsertAllResponse qryResponse = insertAll("test_from_code","UserName");
//      Bigquery bigquery = ServiceUtils.loadBigqueryClient(userService.getCurrentUser().getUserId());
//      com.google.api.services.bigquery.Bigquery.Datasets.List he = bigquery.datasets().list("hello");
      writer.println(qryResponse.toString());
      writer.println("hello hi eppedi");
      
      writer.println("<div class=\"header\"><b>" + request.getUserPrincipal().getName() + "</b> | "
          + "<a href=\"" + userService.createLogoutURL(request.getRequestURL().toString())
          + "\">Log out</a> | "  
          + "<a href=\"http://code.google.com/p/google-api-java-client/source/browse"
          + "/calendar-appengine-sample?repo=samples\">See source code for "
          + "this sample</a></div>");
      writer.println("<div id=\"main\"/>");
      writer.println("</body></html>");
    }
  }
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    setCharacterEncoding(req, resp);
    User who = AuthUtil.getWhoFromLogin();
    if (who == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      doGet(req, resp);
    }
  }


  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
