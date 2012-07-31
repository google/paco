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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Panel to configure an individual time that an experiment is scheduled.
 * 
 * @author Bob Evans
 *
 */
public class TimePanel extends Composite {

  private TimeListPanel timeList;
  private TimePickerFixed timeBox;

  public TimePanel(TimeListPanel timeList) {
    this.timeList = timeList;
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(2);
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(horizontalPanel);
    horizontalPanel.setWidth("258px");

    Label lblTime = new Label("Time: ");
    lblTime.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblTime);
    lblTime.setWidth("45px");

    Date now = new Date();
    now.setMinutes(0);
    now.setSeconds(0);
    timeBox = new TimePickerFixed(now, DateTimeFormat.getFormat("aa"), 
        DateTimeFormat.getFormat("hh"), DateTimeFormat.getFormat("mm"), null);

    horizontalPanel.add(timeBox);
    timeBox.addValueChangeHandler(new ValueChangeHandler() {
      public void onValueChange(ValueChangeEvent event) {
        updateTime();
      }
    });

    Button btnDelete = new Button("-");
    btnDelete.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        deleteThis();
      }

    });
    horizontalPanel.add(btnDelete);

    Button btnAdd = new Button("+");
    horizontalPanel.add(btnAdd);

    btnAdd.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addTimePanel();
      }
    });
  }

  protected void updateTime() {
    timeList.updateTime(this);
  }

  protected void addTimePanel() {
    timeList.addTime(this);
  }

  private void deleteThis() {
    timeList.deleteTime(this);
  }

  public Date getDateTime() {
    return timeBox.getDateTime();
  }

  public void setDateTime(Date dateTime) {
    timeBox.setDateTime(dateTime);
  }

  public Long getTime() {
    Date time = timeBox.getDateTime();
    return new Long((time.getHours() * 60 * 60 * 1000) + (time.getMinutes() * 60 * 1000));
  }

  public void setTime(Long time) {
    Date date = new Date();
    int hours = (int) (time / (60 * 60 * 1000));
    date.setHours(hours);
    date.setMinutes((int) ((time - (hours * 60 * 60 * 1000)) / (60 * 1000)));
    date.setSeconds(0);
    timeBox.setDateTime(date);
  }

}
