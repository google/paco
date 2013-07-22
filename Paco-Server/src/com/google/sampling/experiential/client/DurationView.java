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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.paco.shared.model.ExperimentDAO;
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
  
  private HorizontalPanel mainPanel;
  private HorizontalPanel datePanel;
  private boolean fixedDuration;
  private String startDate;
  private String endDate;
  private RadioButton radio1;
  private RadioButton radio2;
  private DateBox endBox;
  private DateBox startBox;
  private MyConstants myConstants;
  
  private ExperimentDAO experiment;

  public DurationView(ExperimentDAO experiment) {
    super();
    myConstants = GWT.create(MyConstants.class);
    mainPanel = new HorizontalPanel();
    
    this.experiment = experiment;
    
    this.fixedDuration = experiment.getFixedDuration() != null ? experiment.getFixedDuration() : Boolean.FALSE;
    
    Date today = new Date();
    Date tomorrow = new Date(today.getTime() + 8645000);
    String todayString = FORMATTER.format(today);
    String tomorrowString = FORMATTER.format(tomorrow);
    
    // TODO (bobevans): Use Calendar or the GWT time manipulation stuff
    this.startDate = experiment.getStartDate() != null ? experiment.getStartDate() : todayString;
    this.endDate = experiment.getEndDate() != null ? experiment.getEndDate() : tomorrowString;
    initWidget(mainPanel);
    init();
  }

  /**
   * 
   */
  private void init() {
    VerticalPanel outer = new VerticalPanel();
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label(myConstants.duration() + ":");
    keyLabel.setStyleName("keyLabel");
    outer.add(keyLabel);  
    radio1 = new RadioButton("duration", myConstants.ongoingDuration());
    radio2 = new RadioButton("duration", myConstants.fixedDuration());
    radio1.setValue(!fixedDuration);
    radio2.setValue(fixedDuration);
    
    line.add(radio1);
    line.add(radio2);
    outer.add(line);
    
    datePanel = new HorizontalPanel();
    VerticalPanel startPanel = new VerticalPanel();
    datePanel.add(startPanel);
    startBox = new DateBox();
    startBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT)));
    startBox.setValue(FORMATTER.parse(startDate));
    experiment.setStartDate(startDate);
    startBox.addValueChangeHandler(new ValueChangeHandler<Date>() {  
      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        experiment.setStartDate(FORMATTER.format(event.getValue()));        
      }
    });

    Label startLabel = new Label(myConstants.startDate()+":");
    keyLabel.setStyleName("keyLabel");
    
    startPanel.add(startLabel);
    startPanel.add(startBox);

    VerticalPanel endPanel = new VerticalPanel();
    datePanel.add(endPanel);
    endBox = new DateBox();
    endBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT)));
    endBox.setValue(FORMATTER.parse(endDate));
    experiment.setEndDate(endDate);
    endBox.addValueChangeHandler(new ValueChangeHandler<Date>() {  
      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        experiment.setEndDate(FORMATTER.format(event.getValue()));        
      }
    });
   
    Label endLabel = new Label(myConstants.endDate() + ":");
    keyLabel.setStyleName("keyLabel");
    
    endPanel.add(endLabel);
    endPanel.add(endBox);

    datePanel.setVisible(fixedDuration);
    line.add(datePanel);
    
    ClickHandler selectionListener = new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        Widget sender = (RadioButton) event.getSource();
        if (sender.equals(radio1)) {
          setDatePanelFixedDuration(false);
        } else {
          setDatePanelFixedDuration(true);
        }
      }  
    };
    
    radio1.addClickHandler(selectionListener);
    radio2.addClickHandler(selectionListener);
    mainPanel.add(outer);
  }
  
  private void setDatePanelFixedDuration(boolean isFixedDuration) {
    datePanel.setVisible(isFixedDuration);
    experiment.setFixedDuration(isFixedDuration);
  }
  
  public boolean isFixedDuration() {
    return radio2.getValue();
  }
  
  public String getStartDate() {
    return FORMATTER.format(startBox.getValue());
  }
  
  public String getEndDate() {
    return FORMATTER.format(endBox.getValue());
  }
  
  // Visible for testing
  protected void setFixedDuration(boolean isFixedDuration) {
    radio1.setValue(!isFixedDuration);
    radio2.setValue(isFixedDuration);
    setDatePanelFixedDuration(isFixedDuration);
  }
  
  // Visible for testing
  protected void setStartDate(String startDate) {
    startBox.setValue(FORMATTER.parse(startDate), true);
  }
  
  // Visible for testing
  protected void setEndDate(String endDate) {
    endBox.setValue(FORMATTER.parse(endDate), true);
  }
  
}
