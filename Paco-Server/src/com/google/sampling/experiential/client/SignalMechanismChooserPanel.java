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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;

/**
 * Container for all scheduling configuration panels.
 *
 * @author Bob Evans
 *
 */
public class SignalMechanismChooserPanel extends Composite {

  private ExperimentDAO experiment;

  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel choicePanel;
  private VerticalPanel mainPanel;

  public SignalMechanismChooserPanel(ExperimentDAO experiment) {
    this.experiment = experiment;
    myConstants = GWT.create(MyConstants.class);

    rootPanel = new VerticalPanel();
    rootPanel.setStyleName("bordered");
    initWidget(rootPanel);

    choicePanel = new HorizontalPanel();
    rootPanel.add(choicePanel);

    mainPanel = new VerticalPanel();
    rootPanel.add(mainPanel);

    HTML signalingMechanismLabel = new HTML("<h2>" + myConstants.signalMechanism() + ": </h2>");
    choicePanel.add(signalingMechanismLabel);
    final ListBox signalingMechanismChoices = new ListBox();
    signalingMechanismChoices.addItem("Scheduled Signaling");
    signalingMechanismChoices.addItem("Triggered Signaling");
    //signalingMechanismChoices.addItem("Self Report");

    choicePanel.add(signalingMechanismChoices);

    if (experiment.getSignalingMechanisms() == null || experiment.getSignalingMechanisms().length == 0) {
      SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];
      signalingMechanisms[0] = new SignalScheduleDAO();
      experiment.setSignalingMechanisms(signalingMechanisms);
    } else {
      SignalingMechanismDAO signalingMechanism = experiment.getSignalingMechanisms()[0];
      if (signalingMechanism instanceof SignalScheduleDAO) {
        signalingMechanismChoices.setItemSelected(0, true);
      } else if (signalingMechanism instanceof TriggerDAO) {
        signalingMechanismChoices.setItemSelected(1, true);
      }
    }

    updatePanel();

    signalingMechanismChoices.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int index = signalingMechanismChoices.getSelectedIndex();
        respondToListSelection(index);
      }

    });
  }

  private void respondToListSelection(int index) {
    SignalingMechanismDAO signalingMechanism = null;
    if (index == 0) {
      signalingMechanism = new SignalScheduleDAO();
    } else {
      signalingMechanism = new TriggerDAO();
    }

    SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[] {signalingMechanism};
    experiment.setSignalingMechanisms(signalingMechanisms);
    updatePanel();
  }

  private void updatePanel() {
    SignalingMechanismDAO signalingMechanism = experiment.getSignalingMechanisms()[0];
    mainPanel.clear();
    if (signalingMechanism instanceof SignalScheduleDAO) {
      mainPanel.add(createSchedulePanel());
    } else {
      mainPanel.add(createTriggerPanel());
    }
  }

  private SchedulePanel createSchedulePanel() {
    return new SchedulePanel((SignalScheduleDAO) experiment.getSignalingMechanisms()[0]);
  }

  private TriggerPanel createTriggerPanel() {
    return new TriggerPanel((TriggerDAO) experiment.getSignalingMechanisms()[0]);
  }


}
