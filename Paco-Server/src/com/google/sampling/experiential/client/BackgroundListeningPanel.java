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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;

/**
 * Container for all scheduling configuration panels.
 *
 * @author Phil Adams
 *
 */
public class BackgroundListeningPanel extends Composite {

  private final ExperimentDAO experiment;

  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel listeningCheckboxPanel;
  private HorizontalPanel sourceIdentifierPanel;

  public BackgroundListeningPanel(ExperimentDAO experiment) {
    this.experiment = experiment;
    myConstants = GWT.create(MyConstants.class);

    rootPanel = new VerticalPanel();
    rootPanel.setStyleName("bordered");
    initWidget(rootPanel);

    HTML titleLabel = new HTML("<h2>" + myConstants.backgroundListeningTitle() + ": </h2>");
    rootPanel.add(titleLabel);

    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    rootPanel.add(html);
    
    // set up the checkbox
    listeningCheckboxPanel = new HorizontalPanel();
    rootPanel.add(listeningCheckboxPanel);
    CheckBox shouldListenCheckbox = new CheckBox();
    listeningCheckboxPanel.add(shouldListenCheckbox);
    Label checkBoxLabel = new Label(myConstants.shouldBackgroundListen());
    checkBoxLabel.setStyleName("gwt-Label-Header");
    listeningCheckboxPanel.add(checkBoxLabel);
    shouldListenCheckbox.setValue(experiment.isBackgroundListen() != null && experiment.isBackgroundListen());
    shouldListenCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        BackgroundListeningPanel.this.experiment.setBackgroundListen(event.getValue());
      }
    });
    
    // set up the sourceIdentifier textbox
    sourceIdentifierPanel = new HorizontalPanel();
    rootPanel.add(sourceIdentifierPanel);
    Label textBoxLabel = new Label(myConstants.backgroundListenSourceIdentifier());
    textBoxLabel.setStyleName("gwt-Label-Header");
    sourceIdentifierPanel.add(textBoxLabel);
    final TextBox sourceIdentifierTextBox = new TextBox();
    sourceIdentifierPanel.add(sourceIdentifierTextBox);
    sourceIdentifierTextBox.setValue(experiment.getBackgroundListenSourceIdentifier());
    sourceIdentifierTextBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        BackgroundListeningPanel.this.experiment.setBackgroundListenSourceIdentifier(
            sourceIdentifierTextBox.getText());
      }
    });

  }


}
