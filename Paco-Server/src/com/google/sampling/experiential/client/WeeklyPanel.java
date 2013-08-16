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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Panel that allows configuration of a weekly experiment schedule.
 * 
 * @author Bob Evans
 *
 */
public class WeeklyPanel extends Composite {

  private SignalScheduleDAO schedule;
  private MyConstants myConstants;

  public WeeklyPanel(SignalScheduleDAO schedule, SignalMechanismChooserPanel ancestor) {
    myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.setSize("290px", "43px");
    initWidget(verticalPanel);

    RepeatEveryNPanel repeatWeeksPanel = new RepeatEveryNPanel(myConstants.repeatTypeWeeks(), schedule);
    verticalPanel.add(repeatWeeksPanel);
    repeatWeeksPanel.setWidth("239px");

    createRepeatWeeklyPanel(verticalPanel);

    TimeListPanel timeListPanel = new TimeListPanel(schedule);
    verticalPanel.add(timeListPanel);
    timeListPanel.setWidth("286px");
    
    TimeoutPanel timeoutPanel = new TimeoutPanel(schedule, ancestor);
    verticalPanel.add(timeoutPanel);
    timeoutPanel.setWidth("286px");
  }

  private void createRepeatWeeklyPanel(VerticalPanel verticalPanel) {
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    verticalPanel.add(horizontalPanel);
    horizontalPanel.setWidth("239px");
    Label lblRepeatOn = new Label(myConstants.repeatOn() + ":");
    lblRepeatOn.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblRepeatOn);
    horizontalPanel.setCellVerticalAlignment(lblRepeatOn, HasVerticalAlignment.ALIGN_MIDDLE);
    lblRepeatOn.setSize("82px", "19px");

    WeekDayPanel weekDay = new WeekDayPanel(true, schedule);
    horizontalPanel.add(weekDay);
    horizontalPanel.setCellVerticalAlignment(weekDay, HasVerticalAlignment.ALIGN_MIDDLE);
  }

}
