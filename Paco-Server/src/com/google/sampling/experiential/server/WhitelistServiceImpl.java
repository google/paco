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

import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.sampling.experiential.shared.LoginInfo;

@SuppressWarnings("serial")
public class WhitelistServiceImpl extends RemoteServiceServlet implements
    com.google.sampling.experiential.shared.WhitelistService {

  @Override
  public List<String> getWhitelist() {
    DBWhitelist wl = new DBWhitelist();
    return wl.getUsers();
  }

  @Override
  public void addUser(String email) {
    DBWhitelist wl = new DBWhitelist();
    wl.addUser(email.toLowerCase());
  }

  @Override
  public void convertUsers() {
    Whitelist wl = new Whitelist();
    DBWhitelist dbWl = new DBWhitelist();
    dbWl.addAllUsers(wl.getUsers());
    
  }
  

}
