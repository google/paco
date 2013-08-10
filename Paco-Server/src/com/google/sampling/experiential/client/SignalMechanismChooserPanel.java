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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
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

  public static final int SCHEDULED_SIGNALING_INDEX = 0;
  public static final int TRIGGERED_SIGNALING_INDEX = 1;

  private SignalMechanismChooserListPanel parent;
  private SignalingMechanismDAO signalingMechanism; 

  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel choicePanel;
  private VerticalPanel mainPanel;

  protected ListBox signalingMechanismChoices;

  public SignalMechanismChooserPanel(SignalingMechanismDAO signalingMechanism,
                                     SignalMechanismChooserListPanel parent) {
    myConstants = GWT.create(MyConstants.class);
    this.parent = parent;
    this.signalingMechanism = signalingMechanism;  

    rootPanel = new VerticalPanel();
    initWidget(rootPanel);

    choicePanel = new HorizontalPanel();
    rootPanel.add(choicePanel);

    mainPanel = new VerticalPanel();
    rootPanel.add(mainPanel);

    Label signalingMechanismLabel = new Label(myConstants.signalMechanism() + ": ");
    choicePanel.add(signalingMechanismLabel);
    signalingMechanismChoices = new ListBox();
    signalingMechanismChoices.addItem(myConstants.scheduledSignaling());
    signalingMechanismChoices.addItem(myConstants.triggeredSignaling());

    choicePanel.add(signalingMechanismChoices);

    if (signalingMechanism instanceof SignalScheduleDAO) {
      signalingMechanismChoices.setItemSelected(SCHEDULED_SIGNALING_INDEX, true);
    } else if (signalingMechanism instanceof TriggerDAO) {
      signalingMechanismChoices.setItemSelected(TRIGGERED_SIGNALING_INDEX, true);
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
    if (index == SCHEDULED_SIGNALING_INDEX) {
      signalingMechanism = new SignalScheduleDAO();
    } else {
      signalingMechanism = new TriggerDAO();
    }
    updateExperimentModel();
    updatePanel();
  }

  private void updatePanel() {
    mainPanel.clear();
    if (signalingMechanism instanceof SignalScheduleDAO) {
      mainPanel.add(createSchedulePanel());
    } else {
      mainPanel.add(createTriggerPanel());
    }
  }

  private SchedulePanel createSchedulePanel() {
    return new SchedulePanel((SignalScheduleDAO) signalingMechanism, this);
  }

  private TriggerPanel createTriggerPanel() {
    return new TriggerPanel((TriggerDAO) signalingMechanism, this);
  }

  public void addTimeoutErrorMessage(String message) {
    parent.addTimeoutErrorMessage(this, message);
  }

  public void removeTimeoutErrorMessage(String message) {
    parent.removeTimeoutErrorMessage(this, message);
  }

  private void updateExperimentModel() {
    parent.updateExperimentSignalingMechanism(this, signalingMechanism);
  }

  public SignalingMechanismDAO getSignalingMechanism() {
    return signalingMechanism;
  }

}
