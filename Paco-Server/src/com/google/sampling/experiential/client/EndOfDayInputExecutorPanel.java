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
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;


import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.Output;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 *
 * Panel for viewing/editing one Input object that can refer to a previously recorded response (Event).
 *
 * @author Bob Evans
 *
 */
public class EndOfDayInputExecutorPanel extends InputExecutorPanel {

  private EventDAO referredEvent;

  public EndOfDayInputExecutorPanel(InputDAO input, EventDAO eventDAO) {
    super(input);
    this.referredEvent = eventDAO;
  }

  public Output getValue() {
    Output output = super.getValue();
    DateTimeFormat df = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);

    output.setName(df.format(referredEvent.getIdFromTimes()) + "_" + input.getName());
    return output;
  }


}
