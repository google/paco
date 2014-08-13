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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.SignalTimeDAO;

/**
 * Panel to configure an individual time that an experiment is scheduled.
 *
 * @author Bob Evans
 *
 */
public class TimePanel extends Composite {

  private TimeListPanel timeList;
  private TimePickerFixed timeBox;
  private SignalTimeDAO signalTime;
  private HorizontalPanel mainPanel;
  private Widget typeDetailsPanel;
  private VerticalPanel middlePanel;
  private ListBox typeChooser;
  private boolean firstTimePanel;
  private ListBox skipBehavior;
  private HorizontalPanel missedBehaviorPanel;
  private MyConstants myConstants;

  public TimePanel(TimeListPanel timeList, final SignalTimeDAO signalTime, boolean firstTime) {
    myConstants = GWT.create(MyConstants.class);

    this.timeList = timeList;
    this.signalTime = signalTime;
    this.firstTimePanel = firstTime;
    mainPanel = new HorizontalPanel();
    mainPanel.setSpacing(3);
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    mainPanel.setStyleName("bordered");
    initWidget(mainPanel);
    mainPanel.setWidth("100%");

    typeChooser = new ListBox();
    typeChooser.addItem("At Time: ");
    typeChooser.addItem("At Offset: ");
    mainPanel.add(typeChooser);
    typeChooser.setItemSelected(signalTime.getType(), true);
    typeChooser.addChangeHandler(new ChangeHandler(){

      @Override
      public void onChange(ChangeEvent event) {
        int currentType = signalTime.getType();
        int newType = typeChooser.getSelectedIndex();
        signalTime.setType(newType);
        if (currentType != newType) {
          if (newType == SignalTimeDAO.FIXED_TIME) {
            signalTime.setMissedBasisBehavior(SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME);
          } else {
            signalTime.setMissedBasisBehavior(SignalTimeDAO.MISSED_BEHAVIOR_SKIP);
          }
        }
        updateTypeDetailsPanel();
      }

    });


    middlePanel = new VerticalPanel();
    mainPanel.add(middlePanel);

    if (signalTime.getType() == SignalTimeDAO.FIXED_TIME) {
      typeDetailsPanel = createFixedTimePanel(signalTime);
    } else {
      typeDetailsPanel = createOffsetTimePanel();
    }
    middlePanel.add(typeDetailsPanel);

    missedBehaviorPanel = createMissedBehaviorPanel();
    middlePanel.add(missedBehaviorPanel);

    HorizontalPanel labelPanel = createLabelPanel();
    middlePanel.add(labelPanel);

    Button btnDelete = new Button("-");
    btnDelete.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        deleteThis();
      }

    });
    mainPanel.add(btnDelete);

    Button btnAdd = new Button("+");
    mainPanel.add(btnAdd);

    btnAdd.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addTimePanel();
      }
    });
    if (firstTimePanel) {
      setFirst(true);
    }
  }

  private HorizontalPanel createLabelPanel() {
    HorizontalPanel labelPanel = new HorizontalPanel();
    labelPanel.add(new Label("Label: "));
    final TextBox labelBox = new TextBox();
    labelPanel.add(labelBox);
    labelBox.setText(signalTime.getLabel());
    labelBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        signalTime.setLabel(labelBox.getText());
      }
    });
    return labelPanel;
  }

  private void updateTypeDetailsPanel() {
    int index = middlePanel.getWidgetIndex(typeDetailsPanel);
    middlePanel.remove(typeDetailsPanel);
    if (signalTime.getType() == SignalTimeDAO.FIXED_TIME) {
      typeDetailsPanel = createFixedTimePanel(signalTime);
    } else {
      typeDetailsPanel = createOffsetTimePanel();
    }
    middlePanel.insert(typeDetailsPanel, index);
  }

  private HorizontalPanel createMissedBehaviorPanel() {
    HorizontalPanel missedBehaviorPanel = new HorizontalPanel();
    missedBehaviorPanel.add(new Label("(For offsets), if previous time is missed: "));
    skipBehavior = new ListBox();
    skipBehavior.addItem("Skip this time");
    skipBehavior.addItem("Use previous time's scheduled time");
    skipBehavior.setItemSelected(signalTime.getMissedBasisBehavior(), true);
    skipBehavior.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        signalTime.setMissedBasisBehavior(skipBehavior.getSelectedIndex());
      }
    });
    missedBehaviorPanel.add(skipBehavior);
    return missedBehaviorPanel;
  }

  private Widget createOffsetTimePanel() {
    VerticalPanel container = new VerticalPanel();
    container.add(new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>"));

    HorizontalPanel offsetPanel = new HorizontalPanel();
    container.add(offsetPanel);
    offsetPanel.add(new Label("From: "));
    final ListBox basisChooser = new ListBox();
    basisChooser.addItem("Previous Scheduled Time");
    basisChooser.addItem("Previous Response Time");
    basisChooser.setItemSelected(signalTime.getBasis(), true);
    offsetPanel.add(basisChooser);
    basisChooser.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        signalTime.setBasis(basisChooser.getSelectedIndex());
      }
    });

    offsetPanel.add(new Label(" " + "by" + " "));
    final TextBox minuteBox = new TextBox();
    int minutes = signalTime.getOffsetTimeMillis() / 1000 / 60;
    minuteBox.setText(Integer.toString(minutes));
    offsetPanel.add(minuteBox);
    offsetPanel.add(new Label("(minutes)"));
    minuteBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String value = minuteBox.getValue();
        try {
          int minutes = Integer.parseInt(value);
          int millis = minutes * 60 * 1000;
          signalTime.setOffsetTimeMillis(millis);
        } catch (NumberFormatException e) {
        }
      }
    });

    return container;
  }

  private Widget createFixedTimePanel(SignalTimeDAO signalTime2) {

    timeBox = new TimePickerFixed(getFixedTimeAsDate(), DateTimeFormat.getFormat("aa"),
                                  DateTimeFormat.getFormat("hh"), DateTimeFormat.getFormat("mm"), null);
    timeBox.addValueChangeHandler(new ValueChangeHandler() {
      public void onValueChange(ValueChangeEvent event) {
        updateTime();
      }
    });
    return timeBox;
  }

  protected void updateTime() {
    Date time = timeBox.getDateTime();
    int fixedTimeOffsetMillisFromMidnight = (time.getHours() * 60 * 60 * 1000) + (time.getMinutes() * 60 * 1000);

    signalTime.setFixedTimeMillisFromMidnight(fixedTimeOffsetMillisFromMidnight);
  }

  protected void addTimePanel() {
    timeList.addTime(this);
  }

  private void deleteThis() {
    timeList.deleteTime(this);
  }

  public SignalTimeDAO getTime() {
    return signalTime;
  }

  private Date getFixedTimeAsDate() {
    Integer times = signalTime.getFixedTimeMillisFromMidnight();
    Date date = new Date();
    int hours = times / (60 * 60 * 1000);
    date.setHours(hours);
    date.setMinutes((times - (hours * 60 * 60 * 1000)) / (60 * 1000));
    date.setSeconds(0);
    return date;
  }

  public void setFirst(boolean first) {
    if (firstTimePanel && !first) {
      typeChooser.setEnabled(true);
      missedBehaviorPanel.setVisible(true);
      firstTimePanel = false;
    } else if (first) {
      typeChooser.setItemSelected(0,  true);
      typeChooser.setEnabled(false);
      missedBehaviorPanel.setVisible(false);
      firstTimePanel = true;
    }
    updateTypeDetailsPanel();
  }

}
