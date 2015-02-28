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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model2.ExperimentGroup;

public class EndOfDayCheckPanel extends Composite {

  private final ExperimentGroup experimentGroup;

  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel eodGroupCheckboxPanel;
  private HorizontalPanel groupIdentifierPanel;

  public EndOfDayCheckPanel(final ExperimentGroup group) {
    this.experimentGroup = group;
    myConstants = GWT.create(MyConstants.class);

    rootPanel = new VerticalPanel();
    rootPanel.setStyleName("bordered");
    initWidget(rootPanel);

//    HTML titleLabel = new HTML("<h2>" + myConstants.endOfDayGroupLabel() + ": </h2>");
//    rootPanel.add(titleLabel);

    // set up the checkbox
    eodGroupCheckboxPanel = new HorizontalPanel();
    rootPanel.add(eodGroupCheckboxPanel);

    CheckBox eodGroupCheckbox = new CheckBox();
    eodGroupCheckboxPanel.add(eodGroupCheckbox);
    Label checkBoxLabel = new Label(myConstants.endOfDayGroupLabel());
    checkBoxLabel.setStyleName("gwt-Label-Header");
    eodGroupCheckboxPanel.add(checkBoxLabel);

    eodGroupCheckbox.setValue(group.getEndOfDayGroup() != null && group.getEndOfDayGroup());


    // set up the sourceIdentifier textbox
    groupIdentifierPanel = new HorizontalPanel();
    rootPanel.add(groupIdentifierPanel);

    Label textBoxLabel = new Label(myConstants.endOfDayReferredGroupIdentifierLabel());
    textBoxLabel.setStyleName("gwt-Label-Header");
    groupIdentifierPanel.add(textBoxLabel);

    final TextBox groupIdentifierTextBox = new TextBox();
    groupIdentifierPanel.add(groupIdentifierTextBox);
    groupIdentifierTextBox.setValue(group.getEndOfDayReferredGroupName());
    groupIdentifierTextBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        EndOfDayCheckPanel.this.experimentGroup.setEndOfDayReferredGroupName(groupIdentifierTextBox.getText());
      }
    });

    groupIdentifierPanel.setVisible(group.getEndOfDayGroup() != null && group.getEndOfDayGroup());

    eodGroupCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        EndOfDayCheckPanel.this.experimentGroup.setEndOfDayGroup(event.getValue());
        boolean isEndOfDayGroup = group.getEndOfDayGroup() != null && group.getEndOfDayGroup();
        groupIdentifierPanel.setVisible(isEndOfDayGroup);
        if (!isEndOfDayGroup) {
          group.setEndOfDayReferredGroupName(null);
        }
      }
    });
  }


}
