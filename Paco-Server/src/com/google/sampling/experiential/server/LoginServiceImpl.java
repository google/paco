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
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.sampling.experiential.shared.LoginInfo;

@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements
    com.google.sampling.experiential.shared.LoginService {

  public LoginInfo login(String requestUri) {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    LoginInfo loginInfo = new LoginInfo();


    if (user == null) {
      loginInfo.setLoggedIn(false);
      loginInfo.setLoginUrl(userService.createLoginURL(requestUri, "google.com"));
      return loginInfo;
    }

    loginInfo.setLoggedIn(true);
    loginInfo.setEmailAddress(user.getEmail());
    loginInfo.setNickname(user.getNickname());
    loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri, "google.com"));
      return loginInfo;
  }

}
