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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalTimeDAO;

/**
 * Container Panel for the times an experiment is scheduled,
 * shown in TimePanels.
 *
 * @author Bob Evans
 *
 */
public class TimeListPanel extends Composite {
  private VerticalPanel rootPanel;
  private SignalScheduleDAO schedule;
  private LinkedList<TimePanel> timePanelsList;

  public TimeListPanel(SignalScheduleDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);

    Label lblSignalTimes = new Label(myConstants.signalTimes());
    lblSignalTimes.setStyleName("gwt-Label-Header");
    rootPanel.add(lblSignalTimes);

    timePanelsList = new LinkedList<TimePanel>();

    if (schedule.getSignalTimes() == null || schedule.getSignalTimes().isEmpty()) {
      List<SignalTimeDAO> times = new ArrayList<SignalTimeDAO>();
      SignalTimeDAO signalTimeDAO = createBlankSignalTimeDAO();
      times.add(signalTimeDAO);
      TimePanel timePanel = new TimePanel(this, signalTimeDAO, true);
      rootPanel.add(timePanel);
      timePanelsList.add(timePanel);
      schedule.setSignalTimes(times);
    } else {
      for (int i = 0; i < schedule.getSignalTimes().size(); i++) {
        TimePanel time = new TimePanel(this, schedule.getSignalTimes().get(i), i == 0);
        rootPanel.add(time);
        timePanelsList.add(time);
      }
    }
  }

  private SignalTimeDAO createBlankSignalTimeDAO() {
    SignalTimeDAO signalTimeDAO = new SignalTimeDAO();
    signalTimeDAO.setType(SignalTimeDAO.FIXED_TIME);
    signalTimeDAO.setFixedTimeMillisFromMidnight(12 * 60 * 60 * 1000);
    signalTimeDAO.setOffsetTimeMillis(SignalTimeDAO.OFFSET_TIME_DEFAULT);
    signalTimeDAO.setMissedBasisBehavior(SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME);
    return signalTimeDAO;
  }

  public void deleteTime(TimePanel timePanel) {
    if (timePanelsList.size() == 1) {
      return;
    }
    SignalTimeDAO signalTimeDAO = timePanel.getTime();
    schedule.getSignalTimes().remove(signalTimeDAO);

    timePanelsList.remove(timePanel);
    rootPanel.remove(timePanel);

    boolean first = true;
    for (TimePanel currentTimePanel : timePanelsList) {
      currentTimePanel.setFirst(first);
      if (first) {
        first = false;
      }
    }
  }

  public void addTime(TimePanel timePanel) {
    int predecessorIndex = timePanelsList.indexOf(timePanel);


    SignalTimeDAO signalTimeDAO = createBlankSignalTimeDAO();
    schedule.getSignalTimes().add(predecessorIndex + 1, signalTimeDAO);

    TimePanel newTimePanel = new TimePanel(this, signalTimeDAO, false);
    timePanelsList.add(predecessorIndex + 1, newTimePanel);

    int predecessorWidgetIndex = rootPanel.getWidgetIndex(timePanel);
    rootPanel.insert(newTimePanel, predecessorWidgetIndex + 1);

  }

}
