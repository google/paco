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

import java.util.LinkedList;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.SignalScheduleDAO;

/**
 * Container Panel for the times an experiment is scheduled,
 * shown in TimePanels.
 * 
 * @author Bob Evans
 *
 */
public class TimeListPanel extends Composite {
  private VerticalPanel verticalPanel_1;
  private SignalScheduleDAO schedule;
  private LinkedList<TimePanel> timesPanelList;

  public TimeListPanel(SignalScheduleDAO schedule) {
    this.schedule = schedule;
    verticalPanel_1 = new VerticalPanel();
    verticalPanel_1.setSpacing(2);
    initWidget(verticalPanel_1);
    Label lblSignalTimes = new Label("Signal Time (s)");
    lblSignalTimes.setStyleName("gwt-Label-Header");
    verticalPanel_1.add(lblSignalTimes);

    timesPanelList = new LinkedList<TimePanel>();
    Long[] times2 = schedule.getTimes();
    if (times2 == null || times2.length == 0) {
      TimePanel timePanel = new TimePanel(this);
      Long time = timePanel.getTime();
      times2 = new Long[] {time};
      verticalPanel_1.add(timePanel);
      timesPanelList.add(timePanel);
      schedule.setTimes(times2);
    } else {
      for (int i = 0; i < times2.length; i++) {
        TimePanel time1 = new TimePanel(this);
        time1.setTime(times2[i]);
        verticalPanel_1.add(time1);
        timesPanelList.add(time1);
      }
    }
  }

  public void deleteTime(TimePanel timePanel) {
    if (timesPanelList.size() == 1) {
      return;
    }
    timesPanelList.remove(timePanel);
    verticalPanel_1.remove(timePanel);
    updateScheduleTimes();
  }

  public void addTime(TimePanel timePanel) {
    int index = timesPanelList.indexOf(timePanel);

    TimePanel timePanel2 = new TimePanel(this);
    timesPanelList.add(index + 1, timePanel2);

    int widgetIndex = verticalPanel_1.getWidgetIndex(timePanel);
    verticalPanel_1.insert(timePanel2, widgetIndex + 1);

    updateScheduleTimes();

  }

  // TODO this is not very efficient.
  private void updateScheduleTimes() {
    Long[] newTimes = new Long[timesPanelList.size()];
    for (int i = 0; i < timesPanelList.size(); i++) {
      newTimes[i] = timesPanelList.get(i).getTime();
    }
    schedule.setTimes(newTimes);
  }

  public void updateTime(TimePanel timePanel) {
    int index = timesPanelList.indexOf(timePanel);
    schedule.getTimes()[index] = timePanel.getTime();
  }

}
