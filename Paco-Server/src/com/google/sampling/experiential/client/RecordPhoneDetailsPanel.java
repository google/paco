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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;

/**
 * Record Phone Details configuration panel.
 *
 */
public class RecordPhoneDetailsPanel extends Composite {

  private final ExperimentDAO experiment;

  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel recordPhoneDetailsCheckboxPanel;

  public RecordPhoneDetailsPanel(ExperimentDAO experiment) {
    this.experiment = experiment;
    myConstants = GWT.create(MyConstants.class);

    rootPanel = new VerticalPanel();
    rootPanel.setStyleName("bordered");
    initWidget(rootPanel);

    HTML titleLabel = new HTML("<h2>" + myConstants.recordPhoneDetailsTitle() + ": </h2>");
    rootPanel.add(titleLabel);

    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    rootPanel.add(html);

    recordPhoneDetailsCheckboxPanel = new HorizontalPanel();
    rootPanel.add(recordPhoneDetailsCheckboxPanel);

    CheckBox shouldRecordCheckbox = new CheckBox();
    recordPhoneDetailsCheckboxPanel.add(shouldRecordCheckbox);

    Label checkBoxLabel = new Label(myConstants.shouldRecordPhoneDetailsOnJoin());
    checkBoxLabel.setStyleName("gwt-Label-Header");
    recordPhoneDetailsCheckboxPanel.add(checkBoxLabel);


    shouldRecordCheckbox.setValue(experiment.isRecordPhoneDetails() != null && experiment.isRecordPhoneDetails());

    shouldRecordCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        RecordPhoneDetailsPanel.this.experiment.setRecordPhoneDetails(event.getValue());

      }
    });
  }


}
