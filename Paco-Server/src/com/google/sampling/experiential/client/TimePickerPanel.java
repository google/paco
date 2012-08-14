/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Panel to configure an individual time that an experiment is scheduled.
 *
 * @author Bob Evans
 *
 */
public class TimePickerPanel extends Composite {
  private HorizontalPanel mainPanel;
  private TimePickerFixed timePicker;
  private Button removeButton;

  /**
   *
   */
  public TimePickerPanel() {
    mainPanel = new HorizontalPanel();

    initWidget(mainPanel);

    timePicker = new TimePickerFixed(
        null, DateTimeFormat.getFormat("aa"), DateTimeFormat.getFormat("hh"),
        DateTimeFormat.getFormat("mm"), null);
    removeButton = new Button("-");

    mainPanel.add(timePicker);
    mainPanel.add(removeButton);
  }

  /**
   * @return the time
   */
  public Date getTime() {
    return timePicker.getDateTime();
  }

  /**
   * @param time the time
   */
  public void setTime(Date time) {
    timePicker.setDateTime(time);
  }

  /**
   * @param handler a click handler
   */
  public void addClickHandler(ClickHandler handler) {
    removeButton.addClickHandler(handler);
  }
}
