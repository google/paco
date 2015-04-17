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
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;

/**
 * Container Panel for the trigger cue panels
 *
 * @author Bob Evans
 *
 */
public class ExperimentGroupListPanel extends Composite {
  private VerticalPanel rootPanel;
  private ExperimentDAO experiment;
  private LinkedList<ExperimentGroupPanel> groupPanelsList;

  public ExperimentGroupListPanel(ExperimentDAO experiment) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.experiment = experiment;
    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);

    Label lblSignalTimes = new Label(myConstants.experimentGroups());
    lblSignalTimes.setStyleName("gwt-Label-Header");
    rootPanel.add(lblSignalTimes);

    groupPanelsList = new LinkedList<ExperimentGroupPanel>();

    if (experiment.getGroups() == null || experiment.getGroups().isEmpty()) {
      List<ExperimentGroup> experimentGroups = new ArrayList<ExperimentGroup>();
      ExperimentGroup experimentGroup = createBlankDAO();
      experimentGroups.add(experimentGroup);
      experiment.setGroups(experimentGroups);

      ExperimentGroupPanel groupPanel = new ExperimentGroupPanel(this, experimentGroup);
      rootPanel.add(groupPanel);
      groupPanelsList.add(groupPanel);

    } else {
      for (int i = 0; i < experiment.getGroups().size(); i++) {
        ExperimentGroupPanel panel = new ExperimentGroupPanel(this, experiment.getGroups().get(i));
        rootPanel.add(panel);
        groupPanelsList.add(panel);
      }
    }
  }

  private ExperimentGroup createBlankDAO() {
    return new ExperimentGroup("New Group");
  }

  public void deleteGroup(ExperimentGroupPanel groupPanel) {
    if (groupPanelsList.size() == 1) {
      return;
    }
    ExperimentGroup group = groupPanel.getExperimentGroup();
    experiment.getGroups().remove(group);

    groupPanelsList.remove(groupPanel);
    rootPanel.remove(groupPanel);
  }

  public void addGroup(ExperimentGroupPanel predecessorPanel) {
    int predecessorIndex = groupPanelsList.indexOf(predecessorPanel);


    ExperimentGroup group = createBlankDAO();
    experiment.getGroups().add(predecessorIndex + 1, group);

    ExperimentGroupPanel groupPanel = new ExperimentGroupPanel(this, group);
    groupPanelsList.add(predecessorIndex + 1, groupPanel);

    int predecessorWidgetIndex = rootPanel.getWidgetIndex(predecessorPanel);
    rootPanel.insert(groupPanel, predecessorWidgetIndex + 1);

  }

  public void recordUIChanges() {
    for (ExperimentGroupPanel groupPanel : groupPanelsList) {
      groupPanel.recordUIChanges();
    }

  }


}
