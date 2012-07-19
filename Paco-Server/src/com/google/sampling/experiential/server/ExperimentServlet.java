/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.sampling.experiential.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Experiment;

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

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
  public static final String DEV_HOST = "<Your machine name here>";
  private UserService userService;


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json;charset=UTF-8");
    String lastModParam = req.getParameter("lastModification");
    User user = getWhoFromLogin();

    if (user == null) {
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
    } else {
      ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
      String experimentsJson = cacheHelper.getExperimentsJsonForUser(user.getUserId());
      List<Experiment> experiments;
      if (experimentsJson == null) {
        experiments = cacheHelper.getJoinableExperiments();
        String email = getEmailOfUser(req, user);
        List<Experiment> availableExperiments = null;
        if (experiments == null) {
          experiments = Lists.newArrayList();
          availableExperiments = experiments;
        } else {
          availableExperiments = getSortedExperimentsAvailableToUser(experiments, email);
        }
        experimentsJson = jsonify(availableExperiments);
        cacheHelper.putExperimentJsonForUser(user.getUserId(), experimentsJson);
      }
      resp.getWriter().println(scriptBust(experimentsJson));
    }
  }

  private String scriptBust(String experimentsJson) {
    // TODO add scriptbusting prefix to this and client code.
    return experimentsJson;
  }


  private List<Experiment> getSortedExperimentsAvailableToUser(
      List<Experiment> experiments, String email) {
    List<Experiment> availableExperiments = Lists.newArrayList();
    for (Experiment experiment : experiments) {
      String creatorEmail = experiment.getCreator().toLowerCase();
      if (creatorEmail.equals(email) || experiment.getAdmins().contains(email) || (
          experiment.getPublished() == true && (experiment.getPublishedUsers().size() == 0
              || experiment.getPublishedUsers().contains(email)))) {
        availableExperiments.add(experiment);
      }
    }
    Collections.sort(availableExperiments, new Comparator<Experiment>() {
      @Override
      public int compare(Experiment o1, Experiment o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
    return availableExperiments;
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

  private String jsonify(List<Experiment> experiments) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      return mapper.writeValueAsString(experiments);
    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
      // printWriter.write("JsonGeneration error getting experiments: " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    // TODO bobevans - add error handling into the return so that the client can tell errors
    return null;
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
