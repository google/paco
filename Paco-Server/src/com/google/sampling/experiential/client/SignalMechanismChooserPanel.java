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
  
  public static final int SCHEDULED_SIGNALING_INDEX = 0;
  public static final int TRIGGERED_SIGNALING_INDEX = 1;

  private ExperimentDAO experiment; 
  private int signalGroupNum;
  private ExperimentCreationListener listener;
  
  private MyConstants myConstants;
  private VerticalPanel rootPanel;
  private HorizontalPanel choicePanel;
  private VerticalPanel mainPanel;
  
  protected ListBox signalingMechanismChoices;

  public SignalMechanismChooserPanel(ExperimentDAO experiment, int signalGroupNum,
                                     ExperimentCreationListener listener) {
    myConstants = GWT.create(MyConstants.class);
    this.experiment = experiment;  
    this.signalGroupNum = signalGroupNum;
    this.listener = listener;

    rootPanel = new VerticalPanel();
    initWidget(rootPanel);
    
    rootPanel.add(createSignalGroupHeader());
    rootPanel.add(createScheduleHeader());

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

    if (signalGroupNum != 0 ||  // TODO: for now high input group numbers have no meaning. Will change with signal groups.
        experiment.getSignalingMechanisms() == null || experiment.getSignalingMechanisms().length == 0) {
      SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];
      signalingMechanisms[0] = new SignalScheduleDAO();
      experiment.setSignalingMechanisms(signalingMechanisms);
    } else {
      SignalingMechanismDAO signalingMechanism = experiment.getSignalingMechanisms()[0];
      if (signalingMechanism instanceof SignalScheduleDAO) {
        signalingMechanismChoices.setItemSelected(SCHEDULED_SIGNALING_INDEX, true);
      } else if (signalingMechanism instanceof TriggerDAO) {
        signalingMechanismChoices.setItemSelected(TRIGGERED_SIGNALING_INDEX, true);
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

  private Label createScheduleHeader() {
    String titleText = myConstants.experimentSchedule();
    Label lblExperimentSchedule = new Label(titleText);
    lblExperimentSchedule.setStyleName("paco-HTML-Large");
    return lblExperimentSchedule;
  }
  
  private Label createSignalGroupHeader() {
    String titleText = myConstants.signalGroup() + " " + signalGroupNum;
    Label lblExperimentSchedule = new Label(titleText);
    lblExperimentSchedule.setStyleName("paco-HTML-Large");
    return lblExperimentSchedule;
  }

  private void respondToListSelection(int index) {
    SignalingMechanismDAO signalingMechanism = null;
    if (index == SCHEDULED_SIGNALING_INDEX) {
      signalingMechanism = new SignalScheduleDAO();
    } else {
      signalingMechanism = new TriggerDAO();
    }

    SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[] { signalingMechanism };
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
    return new SchedulePanel((SignalScheduleDAO) experiment.getSignalingMechanisms()[0], this);
  }

  private TriggerPanel createTriggerPanel() {
    return new TriggerPanel((TriggerDAO) experiment.getSignalingMechanisms()[0], this);
  }

  public void addTimeoutErrorMessage() {
    fireExperimentCode(ExperimentCreationListener.ADD_ERROR, myConstants.timeoutMustBeValid());
  }
  
  public void removeTimeoutErrorMessage() {
    fireExperimentCode(ExperimentCreationListener.REMOVE_ERROR, myConstants.timeoutMustBeValid());
  }
  
  public void fireExperimentCode(int code, String message) {
    listener.eventFired(code, signalGroupNum, message);
  }

}
