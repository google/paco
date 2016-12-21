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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

/**
 * Servlet that answers queries for Events.
 *
 * @author Bob Evans
 *
 */
public class BQProcessServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(BQProcessServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    String tableName = "hello12";
    User user = AuthUtil.getWhoFromLogin();
    InputStream is = req.getInputStream();
    byte[] body = IOUtils.toByteArray(is);
     BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
     
     TableId tableId = TableId.of("test_from_code", tableName!=null?tableName:DataStrategy.TABLE_NAME);
   
     try{
       Iterable<RowToInsert> rowLst = (Iterable<RowToInsert>) SerializationUtils.deserialize(body);
       
     //insert BQ
     InsertAllResponse qryResponse = bigquery.insertAll(InsertAllRequest.newBuilder(tableId).setRows(rowLst).build());
     if (qryResponse.hasErrors()) {
       // If any of the insertions failed, this lets you inspect the errors
       for (Entry<Long, List<BigQueryError>> entry : qryResponse.getInsertErrors().entrySet()) {
         // inspect row error
         for(BigQueryError bqe : entry.getValue()){
           log.info(bqe.getMessage() + "-->" +bqe.getReason());
         }
       }
     }
     log.info("All records inserted into BQ");
     }catch(Exception e){
       System.out.println("#####################"+e);
     }
   
//    }
  }

  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    setCharacterEncoding(req, resp);
    User who = AuthUtil.getWhoFromLogin();
    if (who == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      
      log.info("^^^^^^^^^^^In bq process servlet-get");
    }
  }

    private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
