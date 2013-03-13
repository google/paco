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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.JsonConverter;
import com.google.sampling.experiential.shared.ExperimentDAO;

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

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  public static final String DEV_HOST = "<Your machine name here>";
  private UserService userService;

  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setContentType("application/json;charset=UTF-8");
    String lastModParam = req.getParameter("lastModification");
    User user = getWhoFromLogin();
    
    if (user == null) {
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
    } else {
      ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
      String experimentsJson = cacheHelper.getExperimentsJsonForUser(user.getUserId());
      if (experimentsJson != null) {
        log.info("Got cached experiments for " + user.getEmail());
      }
      List<ExperimentDAO> experiments;
      if (experimentsJson == null) {
        log.info("No cached experiments for " + user.getEmail());
        experiments = cacheHelper.getJoinableExperiments();
        log.info("joinable experiments " + ((experiments != null) ? Integer.toString(experiments.size()) : "none"));
        String email = getEmailOfUser(req, user);
        List<ExperimentDAO> availableExperiments = null;
        if (experiments == null) {
          experiments = Lists.newArrayList();
          availableExperiments = experiments;        
        } else {
          availableExperiments = getSortedExperimentsAvailableToUser(experiments, email);        
        }
        removeSensitiveFields(availableExperiments);
        experimentsJson = JsonConverter.jsonify(availableExperiments);
        cacheHelper.putExperimentJsonForUser(user.getUserId(), experimentsJson);        
      }    
      resp.getWriter().println(scriptBust(experimentsJson));
    }
  }

  private void removeSensitiveFields(List<ExperimentDAO> availableExperiments) {
    for (ExperimentDAO experimentDAO : availableExperiments) {
      experimentDAO.setPublished(null);
      experimentDAO.setAdmins(null);
    }
    
  }

  private String scriptBust(String experimentsJson) {
    // TODO add scriptbusting prefix to this and client code.
    return experimentsJson;
  }


  private List<ExperimentDAO> getSortedExperimentsAvailableToUser(List<ExperimentDAO> experiments, String email) {
    List<ExperimentDAO> availableExperiments = Lists.newArrayList();
    for (ExperimentDAO experiment : experiments) {
      String creatorEmail = experiment.getCreator().toLowerCase();
      if (creatorEmail.equals(email) || arrayContains(experiment.getAdmins(), email) || 
          (experiment.getPublished() == true && 
                  (experiment.getPublishedUsers().length == 0 || arrayContains(experiment.getPublishedUsers(), email)))) {
        availableExperiments.add(experiment);
      }
    }
    Collections.sort(availableExperiments, new Comparator<ExperimentDAO>() {
      @Override
      public int compare(ExperimentDAO o1, ExperimentDAO o2) {
        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
      }      
    });
    return availableExperiments;
  }


  private boolean arrayContains(String[] strings, String targetString) {
    for (int i = 0; i < strings.length; i++) {
      if (strings[i].toLowerCase().equals(targetString.toLowerCase())) {
        return true;
      }
    }
    return false;
  }


  private String getEmailOfUser(HttpServletRequest req, User user) {
    String email = user != null ? user.getEmail() : null;
    if (email == null && isDevInstance(req)) {
      email = "<put your email here to test in developor mode>";
    }
    if (email == null) {
      throw new IllegalArgumentException("You must login!");
    }
    return email.toLowerCase();
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }
  
  public static boolean isDevInstance(HttpServletRequest req) {
    try {
      return DEV_HOST.equals(InetAddress.getLocalHost().toString());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return false;
  }

}
