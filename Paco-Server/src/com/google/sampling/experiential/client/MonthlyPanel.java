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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Configure Monthly scheduling options for an experiment.
 *
 * @author Bob Evans
 *
 */
public class MonthlyPanel extends Composite {

  private SignalScheduleDAO schedule;
  private MyConstants myConstants;

  public MonthlyPanel(final SignalScheduleDAO schedule) {
    myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.setSize("290px", "43px");
    initWidget(verticalPanel);

    RepeatEveryNPanel repeatPanel = new RepeatEveryNPanel(myConstants.repeatTypeMonths(), schedule);
    verticalPanel.add(repeatPanel);
    repeatPanel.setWidth("239px");

    VerticalPanel byWhatPanel = new VerticalPanel();
    byWhatPanel.setSpacing(2);
    verticalPanel.add(byWhatPanel);

    HorizontalPanel domPanel = new HorizontalPanel();
    domPanel.setSpacing(2);
    domPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    byWhatPanel.add(domPanel);
    domPanel.setWidth("184px");
    Label by = new Label("By: ");
    by.setStyleName("gwt-Label-Header");
    domPanel.add(by);
    by.setWidth("30px");

    RadioButton domRadio = new RadioButton(myConstants.byGroup(), myConstants.dayOfMonth());
    domRadio.setHTML(myConstants.dayOfMonth());
    domPanel.add(domRadio);

    final ListBox listBox = createDayOfMonthListBox();
    listBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        schedule.setDayOfMonth(listBox.getSelectedIndex());
      }
    });

    domPanel.add(listBox);
    if (Boolean.TRUE == schedule.getByDayOfMonth()) {
      listBox.setSelectedIndex(schedule.getDayOfMonth());
    }

    HorizontalPanel dowPanel = new HorizontalPanel();
    dowPanel.setSpacing(2);
    dowPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    byWhatPanel.add(dowPanel);

    Label label = new Label(myConstants.by() + ": ");
    label.setStyleName("gwt-Label-Header");
    dowPanel.add(label);
    label.setWidth("30px");

    RadioButton dowRadio = new RadioButton(myConstants.byGroup(), myConstants.dayOfWeek());

    dowRadio.setHTML(myConstants.dayOfWeek());
    dowPanel.add(dowRadio);

    HorizontalPanel weekdayPanel = new HorizontalPanel();
    weekdayPanel.setSpacing(2);
    weekdayPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    weekdayPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    byWhatPanel.add(weekdayPanel);
    byWhatPanel.setCellHorizontalAlignment(weekdayPanel, HasHorizontalAlignment.ALIGN_RIGHT);

    final ListBox nth = createNthDayListBox(schedule, weekdayPanel);

    weekdayPanel.add(nth);

    nth.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        schedule.setNthOfMonth(nth.getSelectedIndex());
      }
    });

    final WeekDayPanel weekDayPanel = new WeekDayPanel(false, schedule);
    weekdayPanel.add(weekDayPanel);
    weekdayPanel.setCellHorizontalAlignment(weekDayPanel, HasHorizontalAlignment.ALIGN_RIGHT);

    dowRadio.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        toggleDayOfMonthDayOfWeekPanels(schedule, listBox, nth, weekDayPanel, true);
      }
    });

    domRadio.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        toggleDayOfMonthDayOfWeekPanels(schedule, listBox, nth, weekDayPanel, false);
      }
    });
    TimeListPanel timeListPanel = new TimeListPanel(schedule);
    verticalPanel.add(timeListPanel);
    timeListPanel.setWidth("286px");

    if (schedule.getByDayOfMonth()) {
      domRadio.setValue(Boolean.TRUE);
      toggleDayOfMonthDayOfWeekPanels(schedule, listBox, nth, weekDayPanel, false);
    } else {
      dowRadio.setValue(Boolean.TRUE);
      toggleDayOfMonthDayOfWeekPanels(schedule, listBox, nth, weekDayPanel, true);
    }

    TimeoutPanel timeoutPanel = new TimeoutPanel(schedule);
    verticalPanel.add(timeoutPanel);
    timeoutPanel.setWidth("286px");

    SnoozePanel snoozePanel = new SnoozePanel(schedule);
    verticalPanel.add(snoozePanel);
    snoozePanel.setWidth("286px");

  }

  private ListBox createNthDayListBox(final SignalScheduleDAO schedule,
      HorizontalPanel weekdayPanel) {
    final ListBox nth = new ListBox();

    weekdayPanel.setCellHorizontalAlignment(nth, HasHorizontalAlignment.ALIGN_RIGHT);
    nth.addItem(myConstants.nthWeekOfMonthFirst());
    nth.addItem(myConstants.nthWeekOfMonthSecond());
    nth.addItem(myConstants.nthWeekOfMonthThird());
    nth.addItem(myConstants.nthWeekOfMonthFourth());
    nth.addItem(myConstants.nthWeekOfMonthFifth());
    if (Boolean.TRUE == schedule.getByDayOfWeek()) {
      nth.setSelectedIndex(schedule.getNthOfMonth());
    }
    return nth;
  }

  private ListBox createDayOfMonthListBox() {
    final ListBox listBox = new ListBox();
    listBox.addItem("1");
    listBox.addItem("2");
    listBox.addItem("3");
    listBox.addItem("4");
    listBox.addItem("5");
    listBox.addItem("6");
    listBox.addItem("7");
    listBox.addItem("8");
    listBox.addItem("9");
    listBox.addItem("10");
    listBox.addItem("11");
    listBox.addItem("12");
    listBox.addItem("13");
    listBox.addItem("14");
    listBox.addItem("15");
    listBox.addItem("16");
    listBox.addItem("17");
    listBox.addItem("18");
    listBox.addItem("19");
    listBox.addItem("20");
    listBox.addItem("21");
    listBox.addItem("22");
    listBox.addItem("23");
    listBox.addItem("24");
    listBox.addItem("25");
    listBox.addItem("26");
    listBox.addItem("27");
    listBox.addItem("28");
    listBox.addItem("29");
    listBox.addItem("30");
    listBox.addItem("31");
    return listBox;
  }

  private void toggleDayOfMonthDayOfWeekPanels(final SignalScheduleDAO schedule,
      final ListBox listBox, final ListBox nth, final WeekDayPanel weekDayPanel, boolean enabled) {
    listBox.setEnabled(!enabled);
    nth.setEnabled(enabled);
    weekDayPanel.setEnabled(enabled);
    schedule.setByDayOfMonth(!enabled);
  }

}
