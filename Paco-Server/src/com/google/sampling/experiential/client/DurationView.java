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
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * View for configuring the run length of an experiment.
 * Options are Ongoing, which runs forever.
 * Or, Fixed Length, which runs from a start date to an end date.
 *
 * @author Bob Evans
 *
 */
public class DurationView extends Composite {

  private static DateTimeFormat FORMATTER = DateTimeFormat.getFormat(TimeUtil.DATE_FORMAT);

  HorizontalPanel mainPanel;
  private boolean fixedDuration;
  private String startDate;
  private String endDate;
  private RadioButton radio1;
  private RadioButton radio2;
  private DateBox endBox;
  private DateBox startBox;
  private MyConstants myConstants;

  public DurationView(Boolean fixedDuration, String start, String end) {
    super();
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new HorizontalPanel();
    this.fixedDuration = fixedDuration != null ? fixedDuration : Boolean.FALSE;

    Date today = new Date();
    Date tomorrow = new Date(today.getTime() + TimeUtil.MILLIS_IN_A_DAY
                             + TimeUtil.EXTRA_MILLIS_OFFSET);
    String todayString = FORMATTER.format(today);
    String tomorrowString = FORMATTER.format(tomorrow);
    // TODO (bobevans): Use Calendar or the GWT time manipulation stuff
    this.startDate = start != null ? start : todayString;
    this.endDate = end != null ? end : tomorrowString;
    initWidget(mainPanel);
    init();
  }

  /**
   *
   */
  private void init() {
    mainPanel.setStyleName("bordered");
    VerticalPanel outer = new VerticalPanel();
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    HTML keyLabel = new HTML("<h3>" + myConstants.duration() + ":</h3>");
    //keyLabel.setStyleName("keyLabel");
    outer.add(keyLabel);
    radio1 = new RadioButton("duration", myConstants.ongoingDuration());
    radio2 = new RadioButton("duration", myConstants.fixedDuration());
    radio1.setChecked(!fixedDuration);
    radio2.setChecked(fixedDuration);



    line.add(radio1);
    line.add(radio2);
    outer.add(line);

    final HorizontalPanel datePanel = new HorizontalPanel();
    VerticalPanel startPanel = new VerticalPanel();
    datePanel.add(startPanel);
    startBox = new DateBox();
    startBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
    startBox.setValue(FORMATTER.parse(startDate));

    Label startLabel = new Label(myConstants.startDate()+":");
    keyLabel.setStyleName("keyLabel");

    startPanel.add(startLabel);
    startPanel.add(startBox);

    VerticalPanel endPanel = new VerticalPanel();
    datePanel.add(endPanel);
    endBox = new DateBox();
    endBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
    endBox.setValue(FORMATTER.parse(endDate));

    Label endLabel = new Label(myConstants.endDate() + ":");
    keyLabel.setStyleName("keyLabel");

    endPanel.add(endLabel);
    endPanel.add(endBox);

    datePanel.setVisible(fixedDuration);
    line.add(datePanel);

    ClickListener selectionListener = new ClickListener() {

      @Override
      public void onClick(Widget sender) {
        if (sender.equals(radio1)) {
          datePanel.setVisible(false);
        } else {
          datePanel.setVisible(true);
        }
      }

    };
    radio1.addClickListener(selectionListener);
    radio2.addClickListener(selectionListener);
    mainPanel.add(outer);
  }

  public boolean isFixedDuration() {
    return radio2.isChecked();
  }

  // Visible for testing
  protected void setFixedDuration(boolean isFixedDuration) {
    radio1.setChecked(!isFixedDuration);
    radio2.setChecked(isFixedDuration);
  }

  public String getStartDate() {
    return FORMATTER.format(startBox.getValue());
  }

  // Visible for testing
  protected void setStartDate(String startDate) {
    startBox.setValue(FORMATTER.parse(startDate));
  }

  public String getEndDate() {
    return FORMATTER.format(endBox.getValue());
  }

  // Visible for testing
  protected void setEndDate(String endDate) {
    endBox.setValue(FORMATTER.parse(endDate));
  }

}
