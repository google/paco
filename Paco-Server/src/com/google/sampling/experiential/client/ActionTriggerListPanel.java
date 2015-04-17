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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;

/**
 * Container Panel for the action trigger panels
 *
 */
public class ActionTriggerListPanel extends Composite {
  private VerticalPanel rootPanel;
  private ExperimentGroup experimentGroup;
  private LinkedList<ActionTriggerPanel> actionTriggerPanelsList;
  private MyConstants myConstants;
  private Widget actionTriggerButtonPanel;
  private Long lastIdUsed = 0l;

  public ActionTriggerListPanel(ExperimentGroup group) {
    myConstants = GWT.create(MyConstants.class);
    this.experimentGroup = group;
    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);

    Label lblSignalTimes = new Label(myConstants.chooseActionMoments());
    lblSignalTimes.setStyleName("gwt-Label-Header");
    rootPanel.add(lblSignalTimes);

    actionTriggerPanelsList = new LinkedList<ActionTriggerPanel>();

    if (group.getActionTriggers() == null || group.getActionTriggers().isEmpty()) {
      List<ActionTrigger> actionTriggers = new ArrayList<ActionTrigger>();
      group.setActionTriggers(actionTriggers);
    } else {
      for (int i = 0; i < group.getActionTriggers().size(); i++) {
        ActionTrigger actionTrigger = group.getActionTriggers().get(i);
        if (actionTrigger.getId() != null) {
          if (lastIdUsed == null || actionTrigger.getId() > lastIdUsed) {
            lastIdUsed = actionTrigger.getId();
          }
        }
        ActionTriggerPanel actionTriggerPanel = null;
        if (actionTrigger instanceof ScheduleTrigger) {
          actionTriggerPanel  = new ScheduleTriggerPanel(this, (ScheduleTrigger)actionTrigger);
        } else {
          actionTriggerPanel = new InterruptTriggerPanel(this, (InterruptTrigger)actionTrigger);
        }
        rootPanel.add(actionTriggerPanel);
        actionTriggerPanelsList.add(actionTriggerPanel);
      }
    }
    actionTriggerButtonPanel = createActionTriggerButtonPanel();
    rootPanel.add(actionTriggerButtonPanel);
  }

  private Widget createActionTriggerButtonPanel() {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    Button scheduleButton = new Button(myConstants.scheduleTriggerButton());
    buttonPanel.add(scheduleButton);
    scheduleButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        addScheduleTrigger();
      }

    });

    Button triggerButton = new Button(myConstants.interruptTriggerButton());
    triggerButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        addInterruptTrigger();
      }

    });

    buttonPanel.add(triggerButton);

    return buttonPanel;
  }

  public void deleteActionTrigger(ActionTriggerPanel actionTriggerPanel) {
    if (actionTriggerPanelsList.size() == 1) {
      return;
    }
    ActionTrigger actionTrigger = actionTriggerPanel.getActionTrigger();
    experimentGroup.getActionTriggers().remove(actionTrigger);

    actionTriggerPanelsList.remove(actionTriggerPanel);
    rootPanel.remove(actionTriggerPanel);
  }


  public void addInterruptTrigger() {
    InterruptTrigger newActionTrigger = createBlankInterruptTrigger();
    ActionTriggerPanel newActionTriggerPanel = new InterruptTriggerPanel(this, newActionTrigger);
    addActionTrigger(newActionTriggerPanel, newActionTrigger);
  }

  public void addScheduleTrigger() {
    ScheduleTrigger newActionTrigger = createBlankScheduleTrigger();
    ScheduleTriggerPanel newActionTriggerPanel = new ScheduleTriggerPanel(this, newActionTrigger);
    addActionTrigger(newActionTriggerPanel, newActionTrigger);
  }

  private void addActionTrigger(ActionTriggerPanel newActionTriggerPanel,
                               ActionTrigger actionTrigger) {
    experimentGroup.getActionTriggers().add(actionTrigger);

    rootPanel.remove(actionTriggerButtonPanel);

    rootPanel.add(newActionTriggerPanel);
    actionTriggerPanelsList.add(newActionTriggerPanel);

    actionTriggerButtonPanel = createActionTriggerButtonPanel();

    rootPanel.add(actionTriggerButtonPanel);
  }

  private InterruptTrigger createBlankInterruptTrigger() {
    final InterruptTrigger interruptTrigger = new InterruptTrigger();
    interruptTrigger.setId(++lastIdUsed);
    return interruptTrigger;
  }

  private ScheduleTrigger createBlankScheduleTrigger() {
    final ScheduleTrigger scheduleTrigger = new ScheduleTrigger();
    scheduleTrigger.setId(++lastIdUsed);
    return scheduleTrigger;
  }

}
