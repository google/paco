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
import com.google.paco.shared.model2.Schedule;
import com.google.paco.shared.model2.ScheduleTrigger;

/**
 * Container Panel for the schedule panels
 *
 */
public class ScheduleListPanel extends Composite {
  private VerticalPanel rootPanel;
  private ScheduleTrigger trigger;
  private LinkedList<SchedulePanel> schedulePanelsList;

  public ScheduleListPanel(ScheduleTrigger trigger) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.trigger = trigger;
    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);

    Label lblSignalTimes = new Label(myConstants.triggerSchedules());
    lblSignalTimes.setStyleName("gwt-Label-Header");
    rootPanel.add(lblSignalTimes);

    schedulePanelsList = new LinkedList<SchedulePanel>();

    if (trigger.getSchedules() == null || trigger.getSchedules().isEmpty()) {
      List<Schedule> triggerSchedules = new ArrayList<Schedule>();
      Schedule schedule = createBlankDAO();
      triggerSchedules.add(schedule);
      SchedulePanel schedulePanel = new SchedulePanel(this, schedule);
      rootPanel.add(schedulePanel);
      schedulePanelsList.add(schedulePanel);
      trigger.setSchedules(triggerSchedules);
    } else {
      for (int i = 0; i < trigger.getSchedules().size(); i++) {
        SchedulePanel time = new SchedulePanel(this, trigger.getSchedules().get(i));
        rootPanel.add(time);
        schedulePanelsList.add(time);
      }
    }
  }

  private Schedule createBlankDAO() {
    return new Schedule();
  }

  public void deleteSchedule(SchedulePanel schedulePanel) {
    if (schedulePanelsList.size() == 1) {
      return;
    }
    Schedule schedule = schedulePanel.getSchedule();
    trigger.getSchedules().remove(schedule);

    schedulePanelsList.remove(schedulePanel);
    rootPanel.remove(schedulePanel);
  }

  public void addSchedule(SchedulePanel schedulePanel) {
    int predecessorIndex = schedulePanelsList.indexOf(schedulePanel);


    Schedule schedule = createBlankDAO();
    trigger.getSchedules().add(predecessorIndex + 1, schedule);

    SchedulePanel newSchedulePanel = new SchedulePanel(this, schedule);
    schedulePanelsList.add(predecessorIndex + 1, newSchedulePanel);

    int predecessorWidgetIndex = rootPanel.getWidgetIndex(schedulePanel);
    rootPanel.insert(newSchedulePanel, predecessorWidgetIndex + 1);

  }

}
