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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.shared.ExperimentDAO;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that answers requests for experiments.
 * 
 * Only used by the Android client to get the current list of experiments.
 * 
 * @author Bob Evans
 *
 */
public class ExperimentServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
  static final String DEV_HOST = "localhost:8080";
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyyMMdd:HH:mm:ssZ");
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    String lastModParam = req.getParameter("lastModification");
    User user = getWhoFromLogin();
    List<ExperimentDAO> experimentDAOs;
    PersistenceManager pm = null;
    String jsonExperimentList;
    try {
      pm = PMF.get().getPersistenceManager();
      javax.jdo.Query q = pm.newQuery(Experiment.class);
      //q.setOrdering("modifyDate desc");
      // TODO (bobevans) Handle query parameters, or lack thereof, more elegantly
//      if (lastModParam != null && !lastModParam.isEmpty()) {
//        q.setFilter("modifyDate == dateParam");
//        q.declareParameters("String dateParam");        
//      }
//      
      q.setFilter("deleted != true");
      List<Experiment> experiments = null;
      if (lastModParam != null && !lastModParam.isEmpty()) {
        experiments = (List<Experiment>) q.execute(Long.valueOf(lastModParam));
      } else {
        experiments = (List<Experiment>) q.execute();
      } 
      
      
      //TODO (bobevans): APPEngine query language does not support || with different properties.
      // But this will not scale to a large number of experiments.
      String email = user != null ? user.getEmail() : null;
      if (email == null && isDevInstance(req)) {
        email = "<put your name here to test in developor mode>";
      }
      if (email == null) {
        throw new IllegalArgumentException("You must login!");
      }
      List<Experiment> availableExperiments = Lists.newArrayList();
      for (Experiment experiment : experiments) {
        String creatorEmail = experiment.getCreator().getEmail();
        if (creatorEmail.equals(email) || 
            experiment.getAdmins().contains(email) || 
            (experiment.getPublished() == true && 
                (experiment.getPublishedUsers().size() == 0 || 
                    experiment.getPublishedUsers().contains(email)))) {
          if (experiment.getInformedConsentForm() != null) {
            experiment.getInformedConsentFormText();
            experiment.getFeedback().get(0).getLongText();
            pm.makePersistent(experiment);
          }
          availableExperiments.add(experiment);
        }
      }
      Collections.sort(availableExperiments, new Comparator<Experiment>() {

        @Override
        public int compare(Experiment o1, Experiment o2) {
          return o1.getTitle().compareTo(o2.getTitle());
        }
        
      });
      jsonify(availableExperiments, resp.getWriter());
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    
  }


  /**
   * @param experiments
   * @param printWriter 
   * @return
   */
  private void jsonify(List<Experiment> experiments, PrintWriter printWriter) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
      for (Experiment experiment : experiments) {
        experimentDAOs.add(MapServiceImpl.createDAO(experiment));  
      }
       
      String json = mapper.writeValueAsString(experimentDAOs);
      printWriter.println(json);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      //printWriter.write("JsonGeneration error getting experiments: " + e.getMessage());
    } catch (JsonMappingException e) {
      e.printStackTrace();
      //printWriter.write("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      //printWriter.write("IO error getting experiments: " + e.getMessage());
    } 
  }


  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }
  
  private boolean isDevInstance(HttpServletRequest req) {
    return DEV_HOST.equals(req.getHeader("Host"));
  }

}
