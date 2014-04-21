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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Container for all scheduling configuration panels.
 *
 * @author Bob Evans
 *
 */
public class SchedulePanel extends Composite {

  private static final boolean EVERYDAY = true;
  private VerticalPanel scheduleDetailsPanel;
  private SignalScheduleDAO schedule;
  private MyConstants myConstants;

  public SchedulePanel(SignalScheduleDAO schedule) {
    this.schedule = schedule;
    myConstants = GWT.create(MyConstants.class);

    VerticalPanel verticalPanel = new VerticalPanel();
    initWidget(verticalPanel);

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(3);
    verticalPanel.add(horizontalPanel);

    Label lblSignalSchedule = new Label(myConstants.signalSchedule() + ":");
    lblSignalSchedule.setStyleName("keyLabel");
    horizontalPanel.add(lblSignalSchedule);
    horizontalPanel.setCellVerticalAlignment(lblSignalSchedule, HasVerticalAlignment.ALIGN_MIDDLE);
    lblSignalSchedule.setWidth("114px");

    final ListBox listBox = createScheduleTypeListBox();
    horizontalPanel.add(listBox);
    horizontalPanel.setCellVerticalAlignment(listBox, HasVerticalAlignment.ALIGN_MIDDLE);
    listBox.setVisibleItemCount(1);

    scheduleDetailsPanel = new VerticalPanel();
    verticalPanel.add(scheduleDetailsPanel);
    setPanelForScheduleType();
    addListSelectionListener(listBox);
    verticalPanel.add(createUserEditable(schedule));
    verticalPanel.add(createUserEditableOnce(schedule));

  }

  private Widget createUserEditable(SignalScheduleDAO schedule2) {
    HorizontalPanel userEditablePanel = new HorizontalPanel();
    userEditablePanel.setSpacing(2);
    userEditablePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    userEditablePanel.setWidth("");
    Label lblUserEditable = new Label("User Editable: ");
    lblUserEditable.setStyleName("gwt-Label-Header");
    userEditablePanel.add(lblUserEditable);

    final CheckBox userEditableCheckBox = new CheckBox("");
    userEditablePanel.add(userEditableCheckBox);
    userEditableCheckBox.setValue(schedule.getUserEditable() != null ? schedule.getUserEditable() : Boolean.TRUE);
    userEditableCheckBox.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        schedule.setUserEditable(userEditableCheckBox.getValue());
      }

    });
    return userEditablePanel;
  }

  private Widget createUserEditableOnce(SignalScheduleDAO schedule2) {
    HorizontalPanel userEditablePanel = new HorizontalPanel();
    userEditablePanel.setSpacing(2);
    userEditablePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    userEditablePanel.setWidth("");
    Label lblUserEditable = new Label("Only Editable on Join: ");
    lblUserEditable.setStyleName("gwt-Label-Header");
    userEditablePanel.add(lblUserEditable);

    final CheckBox userEditableCheckBox = new CheckBox("");
    userEditablePanel.add(userEditableCheckBox);
    userEditableCheckBox.setValue(schedule.getOnlyEditableOnJoin() != null ? schedule.getOnlyEditableOnJoin() : Boolean.FALSE);
    userEditableCheckBox.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        schedule.setOnlyEditableOnJoin(userEditableCheckBox.getValue());
      }

    });
    return userEditablePanel;
  }



  private void addListSelectionListener(final ListBox listBox) {
    listBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int index = listBox.getSelectedIndex();
        respondToListSelection(index);
      }

    });
  }

  private void respondToListSelection(int index) {
    schedule.setScheduleType(index);
    setPanelForScheduleType();
  }

  private void setPanelForScheduleType() {
    switch (schedule.getScheduleType()) {
      case 0:
        setDailyPanel(EVERYDAY);
        break;
      case 1:
        setWeekdayPanel(!EVERYDAY);
        break;
      case 2:
        setWeeklyPanel();
        break;
      case 3:
        setMonthlyPanel();
        break;
      case 4:
        setEsmPanel();
        break;
      case 5:
        scheduleDetailsPanel.clear();
        break;
      case 6:
        scheduleDetailsPanel.clear();
        scheduleDetailsPanel.add(new HTML("<b>Advanced is not implemented yet!</b>"));
        break;
      default:
        throw new IllegalArgumentException("No case to match default list selection!");
    }
  }

  private ListBox createScheduleTypeListBox() {
    return createListbox(SignalScheduleDAO.SCHEDULE_TYPES_NAMES, schedule.getScheduleType());
  }

  public static ListBox createListbox(String[] choices, Integer chosenItem) {
    final ListBox listBox = new ListBox();
    for (int i = 0; i < choices.length; i++) {
      listBox.addItem(choices[i]);
    }

    listBox.setSelectedIndex(chosenItem != null ? chosenItem : 0);
    return listBox;
  }

  protected void setEsmPanel() {
    setPanel(new EsmPanel(schedule));
  }

  private void setDailyPanel(boolean everyday) {
    setPanel(new DailyPanel(schedule));
  }

  private void setWeekdayPanel(boolean everyday) {
    setPanel(new DailyPanel(schedule));
  }

  private void setPanel(Widget panel) {
    scheduleDetailsPanel.clear();
    scheduleDetailsPanel.add(panel);
  }

  private void setWeeklyPanel() {
    setPanel(new WeeklyPanel(schedule));
  }

  private void setMonthlyPanel() {
    setPanel(new MonthlyPanel(schedule));
  }

}
