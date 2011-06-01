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

//import com.google.appengine.api.keyfact`ory.*;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Bob Evans
 *
 */
public class KeyServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    super.doGet(req, resp);
//    try {
//      KeyFactoryService keyFactory = new KeyFactoryServiceImpl();  // out of scope
//      KeyczarReader reader = keyFactory.getKey(
//        new KeyDescriptor("profile_crypting_key").setUserKey(true),
//        KeyczarKeySpec.getDefaultForPurpose(KeyPurpose.DECRYPT_AND_ENCRYPT));
//      byte[] encrypted_profile_data = new Crypter(reader).encrypt(profile_data);
//      // store encrypted data
//    } catch (UserIsNotLoggedInException ex) {
//    } catch (GeneralSecurityException ex) {
//    }

  }

  
}
