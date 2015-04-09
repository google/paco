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
import com.google.paco.shared.model2.InterruptCue;
import com.google.paco.shared.model2.InterruptTrigger;

/**
 * Container Panel for the trigger cue panels
 *
 * @author Bob Evans
 *
 */
public class TriggerCueListPanel extends Composite {
  private VerticalPanel rootPanel;
  private InterruptTrigger trigger;
  private LinkedList<TriggerCuePanel> cuePanelsList;

  public TriggerCueListPanel(InterruptTrigger trigger) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.trigger = trigger;
    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);

    Label lblSignalTimes = new Label(myConstants.triggerCues());
    lblSignalTimes.setStyleName("gwt-Label-Header");
    rootPanel.add(lblSignalTimes);

    cuePanelsList = new LinkedList<TriggerCuePanel>();

    if (trigger.getCues() == null || trigger.getCues().isEmpty()) {
      List<InterruptCue> triggerCues = new ArrayList<InterruptCue>();
      InterruptCue triggerCueDAO = createBlankDAO();
      triggerCues.add(triggerCueDAO);
      TriggerCuePanel cuePanel = new TriggerCuePanel(this, triggerCueDAO);
      rootPanel.add(cuePanel);
      cuePanelsList.add(cuePanel);
      trigger.setCues(triggerCues);
    } else {
      for (int i = 0; i < trigger.getCues().size(); i++) {
        TriggerCuePanel time = new TriggerCuePanel(this, trigger.getCues().get(i));
        rootPanel.add(time);
        cuePanelsList.add(time);
      }
    }
  }

  private InterruptCue createBlankDAO() {
    InterruptCue signalTimeDAO = new InterruptCue();
    return signalTimeDAO;
  }

  public void deleteCue(TriggerCuePanel cuePanel) {
    if (cuePanelsList.size() == 1) {
      return;
    }
    InterruptCue signalTimeDAO = cuePanel.getCue();
    trigger.getCues().remove(signalTimeDAO);

    cuePanelsList.remove(cuePanel);
    rootPanel.remove(cuePanel);
  }

  public void addCue(TriggerCuePanel cuePanel) {
    int predecessorIndex = cuePanelsList.indexOf(cuePanel);


    InterruptCue triggerCueDAO = createBlankDAO();
    trigger.getCues().add(predecessorIndex + 1, triggerCueDAO);

    TriggerCuePanel newTimePanel = new TriggerCuePanel(this, triggerCueDAO);
    cuePanelsList.add(predecessorIndex + 1, newTimePanel);

    int predecessorWidgetIndex = rootPanel.getWidgetIndex(cuePanel);
    rootPanel.insert(newTimePanel, predecessorWidgetIndex + 1);

  }

}
