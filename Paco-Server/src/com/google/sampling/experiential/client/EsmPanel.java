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

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.widgetideas.client.SpinnerListener;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Panel for configuring the ESM scheduling for an experiment.
 *
 * @author Bob Evans
 *
 */
public class EsmPanel extends Composite {

  private SignalScheduleDAO schedule;

  public EsmPanel(final SignalScheduleDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    initWidget(verticalPanel);

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(2);
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.setCellVerticalAlignment(horizontalPanel, HasVerticalAlignment.ALIGN_MIDDLE);
    horizontalPanel.setWidth("");

    Label lblFrequency = new Label(myConstants.frequency() + ":");
    lblFrequency.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblFrequency);
    ValueSpinnerFixed frequencySpinner = new ValueSpinnerFixed(schedule.getEsmFrequency(), 0, 100);
    frequencySpinner.getTextBox().setWidth("18px");
    frequencySpinner.setWidth("35px");
    horizontalPanel.add(frequencySpinner);
    frequencySpinner.getSpinner().addSpinnerListener(new SpinnerListener() {
      public void onSpinning(long value) {
        schedule.setEsmFrequency((int) value);
      }
    });

    Label lblPeriod = new Label(myConstants.period() + ": ");
    lblPeriod.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblPeriod);

    final ListBox listBox = new ListBox();
    for (int i = 0; i < SignalScheduleDAO.ESM_PERIODS.length; i++) {
      listBox.addItem(SignalScheduleDAO.ESM_PERIODS_NAMES[i]);
    }
    horizontalPanel.add(listBox);
    listBox.setVisibleItemCount(1);
    Integer period = schedule.getEsmPeriodInDays();
    if (period == null) {
      period = SignalScheduleDAO.DEFAULT_ESM_PERIOD;
      schedule.setEsmPeriodInDays(SignalScheduleDAO.DEFAULT_ESM_PERIOD);
    }
    listBox.setSelectedIndex(period);
    listBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        schedule.setEsmPeriodInDays(listBox.getSelectedIndex());
      }
    });
    verticalPanel.add(horizontalPanel);

    HorizontalPanel weekendsPanel = new HorizontalPanel();
    weekendsPanel.setSpacing(2);
    weekendsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.add(weekendsPanel);
    weekendsPanel.setWidth("");
    Label lblWeekends = new Label(myConstants.includeWeekends() + ": ");
    lblWeekends.setStyleName("gwt-Label-Header");
    weekendsPanel.add(lblWeekends);

    final CheckBox weekendsBox = new CheckBox("");
    weekendsPanel.add(weekendsBox);
    weekendsBox.setValue(schedule.getEsmWeekends());
    weekendsBox.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        schedule.setEsmWeekends(weekendsBox.getValue());
      }

    });

    HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
    horizontalPanel_1.setSpacing(2);
    horizontalPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.add(horizontalPanel_1);
    horizontalPanel_1.setWidth("");

    Label lblStartHour = new Label(myConstants.startTime() + ":");
    lblStartHour.setStyleName("gwt-Label-Header");
    horizontalPanel_1.add(lblStartHour);
    lblStartHour.setWidth("83px");

    Date setTime = null;
    if (schedule.getEsmStartHour() != null) {
      setTime = new Date();
      long offset = schedule.getEsmStartHour();
      int hours = (int) (offset / (60 * 60 * 1000));
      int minutes = (int) (offset - (hours * 60 * 60 * 1000)) / (60 * 1000);
      setTime.setHours(hours);
      setTime.setMinutes(minutes);
      setTime.setSeconds(0);
    } else {
      Date now = new Date();
      now.setMinutes(0);
      now.setSeconds(0);
      setTime = now;
    }

    final TimePickerFixed startTimeBox =
        new TimePickerFixed(setTime, DateTimeFormat.getFormat("aa"), DateTimeFormat.getFormat("hh"),
            DateTimeFormat.getFormat("mm"), null);

    horizontalPanel_1.add(startTimeBox);

    HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
    horizontalPanel_2.setSpacing(2);
    horizontalPanel_2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    verticalPanel.add(horizontalPanel_2);
    horizontalPanel_2.setWidth("");
    startTimeBox.addValueChangeHandler(new ValueChangeHandler() {
      public void onValueChange(ValueChangeEvent event) {
        Date dateTime = startTimeBox.getDateTime();
        long offset = (dateTime.getHours() * 60 * 60 * 1000) + (dateTime.getMinutes() * 60 * 1000);
        schedule.setEsmStartHour(offset);
      }
    });


    Label lblEndTime = new Label(myConstants.endTime() + ":  ");
    lblEndTime.setStyleName("gwt-Label-Header");
    horizontalPanel_2.add(lblEndTime);
    lblEndTime.setWidth("83px");

    setTime = null;
    if (schedule.getEsmEndHour() != null) {
      setTime = new Date();
      long offset = schedule.getEsmEndHour();
      int hours = (int) (offset / (60 * 60 * 1000));
      int minutes = (int) (offset - (hours * 60 * 60 * 1000)) / (60 * 1000);
      setTime.setHours(hours);
      setTime.setMinutes(minutes);
    } else {
      Date now = new Date();
      now.setMinutes(0);
      now.setSeconds(0);
      setTime = now;
    }

    final TimePickerFixed endTimePicker =
        new TimePickerFixed(setTime, DateTimeFormat.getFormat("aa"), DateTimeFormat.getFormat("hh"),
            DateTimeFormat.getFormat("mm"), null);

    horizontalPanel_2.add(endTimePicker);
    endTimePicker.addValueChangeHandler(new ValueChangeHandler() {
      public void onValueChange(ValueChangeEvent event) {
        Date dateTime = endTimePicker.getDateTime();
        long offset = (dateTime.getHours() * 60 * 60 * 1000) + (dateTime.getMinutes() * 60 * 1000);
        schedule.setEsmEndHour(offset);
      }
    });

    TimeoutPanel timeoutPanel = new TimeoutPanel(schedule);
    verticalPanel.add(timeoutPanel);
    timeoutPanel.setWidth("286px");

    MinimumBufferPanel minimumBufferPanel = new MinimumBufferPanel(schedule);
    verticalPanel.add(minimumBufferPanel);
    minimumBufferPanel.setWidth("286px");

    SnoozePanel snoozePanel = new SnoozePanel(schedule);
    verticalPanel.add(snoozePanel);
    snoozePanel.setWidth("286px");

  }

}
