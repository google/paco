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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.paco.shared.model.SignalScheduleDAO;

/**
 * Panel to show choice of "Repeat Every N days" scheduling option.
 * 
 * @author Bob Evans
 *
 */

public class RepeatEveryNPanel extends Composite {

  private String periodLabel;
  private SignalScheduleDAO schedule;

  public RepeatEveryNPanel(String period, final SignalScheduleDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.schedule = schedule;
    this.periodLabel = period;
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    horizontalPanel.setSpacing(2);
    horizontalPanel.setSize("265px", "25px");
    initWidget(horizontalPanel);

    Label lblRepeatEvery = new Label(myConstants.repeatEvery() + ":");
    lblRepeatEvery.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblRepeatEvery);
    horizontalPanel.setCellVerticalAlignment(lblRepeatEvery, HasVerticalAlignment.ALIGN_MIDDLE);
    lblRepeatEvery.setSize("93px", "16px");

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
    horizontalPanel.add(listBox);
    listBox.setVisibleItemCount(1);
    listBox.setSelectedIndex(schedule.getRepeatRate() - 1);
    listBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        schedule.setRepeatRate(listBox.getSelectedIndex() + 1);
      }
    });
    if (periodLabel == null) {
      periodLabel = "Days";
    }
    Label lblDays = new Label(periodLabel);
    lblDays.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblDays);
  }

}
