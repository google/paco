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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Servlet that returns the current user's email address or a URL for logging
 * in to App Engine.
 *
 * @author Ian Spiro
 *
 */
@SuppressWarnings("serial")
public class AuthServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    resp.setContentType("application/json;charset=UTF-8");
    UserService userService = UserServiceFactory.getUserService();
    User user = AuthUtil.getWhoFromLogin();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> mapObject = new HashMap<String, String>();

    if (user != null) {
      mapObject.put("user", AuthUtil.getEmailOfUser(req, user));
    } 

    // TODO(ispiro): This should only be returned if there is no current user.
    // Always returning for now because the dev server returns
    // 'bobevans999@gmail.com' even if the user is logged out.
    // We may want to reconsider using referer header since it's spoofable.
    String url = req.getHeader("referer");

    if (url == null) {
      url = "";
    }

    String login = userService.createLoginURL(url);
    String logout = userService.createLogoutURL(url);
    mapObject.put("login", login);
    mapObject.put("logout", logout);
    
    String json = mapper.writeValueAsString(mapObject);
    resp.getWriter().println(json);
  }
}