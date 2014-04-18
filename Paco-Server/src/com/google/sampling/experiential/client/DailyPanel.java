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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Panel for configuring daily scheduling for an experiment.
 *
 * @author Bob Evans
 *
 */
public class DailyPanel extends Composite {

  private SignalScheduleDAO schedule;

  public DailyPanel(SignalScheduleDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.setSize("290px", "43px");
    initWidget(verticalPanel);

    if (schedule.getScheduleType() == null
        || schedule.getScheduleType() == SignalScheduleDAO.DAILY) {
      RepeatEveryNPanel repeatWeeksPanel = new RepeatEveryNPanel(myConstants.repeatTypeDays(), schedule);
      verticalPanel.add(repeatWeeksPanel);
      repeatWeeksPanel.setWidth("239px");
    }
    TimeListPanel timeListPanel = new TimeListPanel(schedule);
    verticalPanel.add(timeListPanel);
    timeListPanel.setWidth("286px");

    TimeoutPanel timeoutPanel = new TimeoutPanel(schedule);
    verticalPanel.add(timeoutPanel);
    timeoutPanel.setWidth("286px");

    SnoozePanel snoozePanel = new SnoozePanel(schedule);
    verticalPanel.add(snoozePanel);
    snoozePanel.setWidth("286px");
  }

}
