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
package com.google.sampling.experiential.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;

/**
 * Dispatch to the correct GWT entry point based on
 * the url and the requestor.
 *
 *  This allows us to use GWT a little more like regular pages on a server
 *  when the one-monolithic app model doesn't make sense.
 *
 *  Also, this will be the point at which we can use a different render for a
 *  mobile version of the server.
 *
 * @author Bob Evans
 *
 */
public class DispatchEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    String path = Window.Location.getPath();
    renderDesktopPage(path);
  }

  private void renderMainPage() {
    new Main().onModuleLoad();
  }

  private void renderDesktopPage(String path) {
    if (path.endsWith("join.html")) {
      new JoinExperimentModule().onModuleLoad();
    } else {
      renderMainPage();
    }
  }

}
