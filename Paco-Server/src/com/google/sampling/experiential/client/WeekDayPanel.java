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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Panel that allows selection of days of week to schedule an experiment.
 * 
 * @author Bob Evans
 *
 */
public class WeekDayPanel extends Composite {

  private CheckBox checkBoxSun;
  private CheckBox checkBoxMon;
  private CheckBox checkBoxTue;
  private CheckBox checkBoxWed;
  private CheckBox checkBoxThu;
  private CheckBox checkBoxFri;
  private CheckBox checkBoxSat;
  private boolean multiSelect;
  private CheckBox[] checkBoxes;
  private SignalScheduleDAO schedule;

  public WeekDayPanel(boolean multiSelect, SignalScheduleDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    this.multiSelect = multiSelect;
    VerticalPanel verticalPanel = new VerticalPanel();
    Grid grid = new Grid(2, 7);
    grid.setSize("102px", "49px");
    initWidget(verticalPanel);
    verticalPanel.add(grid);

    checkBoxSun = new CheckBox("");
    grid.setWidget(0, 0, checkBoxSun);
    checkBoxMon = new CheckBox("");
    grid.setWidget(0, 1, checkBoxMon);
    checkBoxTue = new CheckBox("");
    grid.setWidget(0, 2, checkBoxTue);
    checkBoxWed = new CheckBox("");
    grid.setWidget(0, 3, checkBoxWed);
    checkBoxThu = new CheckBox("");
    grid.setWidget(0, 4, checkBoxThu);
    checkBoxFri = new CheckBox("");
    grid.setWidget(0, 5, checkBoxFri);
    checkBoxSat = new CheckBox("");
    grid.setWidget(0, 6, checkBoxSat);

    grid.setText(1, 0, myConstants.sundayInitial());
    grid.setText(1, 1, myConstants.mondayInitial());
    grid.setText(1, 2, myConstants.tuesdayInitial());
    grid.setText(1, 3, myConstants.wednesdayInitial());
    grid.setText(1, 4, myConstants.thursdayInitial());
    grid.setText(1, 5, myConstants.fridayInitial());
    grid.setText(1, 6, myConstants.satdayInitial());
    checkBoxes = new CheckBox[7];
    checkBoxes[0] = checkBoxSun;
    checkBoxes[1] = checkBoxMon;
    checkBoxes[2] = checkBoxTue;
    checkBoxes[3] = checkBoxWed;
    checkBoxes[4] = checkBoxThu;
    checkBoxes[5] = checkBoxFri;
    checkBoxes[6] = checkBoxSat;

    setSelctedCheckboxes();

    checkBoxSun.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(0);
      }
    });
    checkBoxMon.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(1);
      }
    });
    checkBoxTue.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(2);
      }
    });
    checkBoxWed.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(3);
      }
    });
    checkBoxThu.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(4);
      }
    });
    checkBoxFri.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(5);
      }
    });
    checkBoxSat.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        selected(6);
      }
    });
    
  }

  private void setSelctedCheckboxes() {
    int daysScheduled = schedule.getWeekDaysScheduled();
    int bitsSet = 0;
    for (int i = 0; i < checkBoxes.length; i++) {
      // TODO this is slightly lame, but we select only the first day from the
      // schedule if the schedule was previously weekly with multiple days set.
      // and we are letting them pick the day of the month for monthly
      if (!multiSelect && bitsSet == 1) {
        break;
      }
      if ((daysScheduled & SignalScheduleDAO.DAYS_OF_WEEK[i])
          == SignalScheduleDAO.DAYS_OF_WEEK[i]) {
        checkBoxes[i].setValue(Boolean.TRUE);
        bitsSet++;
      }
    }
  }

  protected void selected(int selected) {
    if (!multiSelect) {
      deselectOthers(selected);
    }
    updateSchedule();
  }

  private void updateSchedule() {
    int selected = 0;
    for (int i = 0; i < 7; i++) {
      if (checkBoxes[i].getValue() == Boolean.TRUE) {
        selected |= SignalScheduleDAO.DAYS_OF_WEEK[i];
      }
    }
    schedule.setWeekDaysScheduled(selected);
  }

  protected void deselectOthers(int selected) {
    for (int i = 0; i < 7; i++) {
      if (i == selected) {
        continue;
      }
      checkBoxes[i].setValue(Boolean.FALSE);
    }

  }

  public void setEnabled(boolean b) {
    for (int i = 0; i < checkBoxes.length; i++) {
      checkBoxes[i].setEnabled(b);
    }

  }
}
