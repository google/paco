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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoActionAllOthers;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;

/**
 * Container Panel for the trigger cue panels
 *
 * @author Bob Evans
 *
 */
public class PacoActionListPanel extends Composite {
  private VerticalPanel rootPanel;
  private ActionTrigger trigger;
  private LinkedList<PacoActionPanel> actionPanelsList;
  private Long lastUsedActionId = 0l;

  public PacoActionListPanel(ActionTrigger trigger) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.trigger = trigger;
    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);

    Label lblSignalTimes = new Label(myConstants.triggerActions());
    lblSignalTimes.setStyleName("gwt-Label-Header");
    rootPanel.add(lblSignalTimes);

    actionPanelsList = new LinkedList<PacoActionPanel>();

    if (trigger.getActions() == null || trigger.getActions().isEmpty()) {
      List<PacoAction> triggerActions = new ArrayList<PacoAction>();
      PacoAction pacoAction = createBlankAction();
      triggerActions.add(pacoAction);
      PacoActionPanel actionPanel = new PacoActionPanel(this, pacoAction);
      rootPanel.add(actionPanel);
      actionPanelsList.add(actionPanel);
      trigger.setActions(triggerActions);
    } else {
      for (int i = 0; i < trigger.getActions().size(); i++) {
        final PacoAction pacoAction = trigger.getActions().get(i);
        if (pacoAction.getId() != null ) {
          if (lastUsedActionId == null || pacoAction.getId() > lastUsedActionId) {
            lastUsedActionId = pacoAction.getId();
          }
        }
        PacoActionPanel actionPanel = new PacoActionPanel(this, pacoAction);
        rootPanel.add(actionPanel);
        actionPanelsList.add(actionPanel);
      }
    }
  }

  private PacoAction createBlankAction() {
    PacoAction pacoAction = new PacoNotificationAction();
    pacoAction.setActionCode(PacoActionAllOthers.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE);
    pacoAction.setId(++lastUsedActionId);
    return pacoAction;
  }

  public void deleteAction(PacoActionPanel actionPanel) {
    if (actionPanelsList.size() == 1) {
      return;
    }
    PacoAction actionDAO = actionPanel.getAction();
    trigger.getActions().remove(actionDAO);

    actionPanelsList.remove(actionPanel);
    rootPanel.remove(actionPanel);
  }

  public void addAction(PacoActionPanel actionPanel) {
    int predecessorIndex = actionPanelsList.indexOf(actionPanel);


    PacoAction pacoAction = createBlankAction();
    trigger.getActions().add(predecessorIndex + 1, pacoAction);

    PacoActionPanel newActionPanel = new PacoActionPanel(this, pacoAction);
    actionPanelsList.add(predecessorIndex + 1, newActionPanel);

    int predecessorWidgetIndex = rootPanel.getWidgetIndex(actionPanel);
    rootPanel.insert(newActionPanel, predecessorWidgetIndex + 1);

  }

  public ActionTrigger getTrigger() {
    return trigger;
  }

  public void setTrigger(ActionTrigger trigger) {
    this.trigger = trigger;
  }


}
