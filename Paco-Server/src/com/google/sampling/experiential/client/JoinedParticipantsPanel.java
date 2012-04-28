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
import java.util.Collection;
import java.util.Collections;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Panel to show all users who have registered a "joined" event with the given
 * experiment.
 *
 * @author Bob Evans
 *
 */
public class JoinedParticipantsPanel extends PopupPanel {

  public JoinedParticipantsPanel(Collection<String> collection) {
    super(true);
    setAutoHideEnabled(true);
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    setWidget(verticalPanel);
    verticalPanel.setSize("300", "400%");

    Label lblParticipants = new Label("Participants");
    lblParticipants.setStyleName("paco-HTML");
    verticalPanel.add(lblParticipants);

    ScrollPanel scrollPanel = new ScrollPanel();
    verticalPanel.add(scrollPanel);

    Grid grid = new Grid(collection.size(), 1);
    scrollPanel.setWidget(grid);
    int row = 0;
    ArrayList<String> participantsAsList = new ArrayList<String>(collection);
    Collections.sort(participantsAsList);
    for (String participant : participantsAsList) {
      Label label = new Label(participant);
      grid.setWidget(row++, 0, label);

    }

    Button closeButton = new Button("Close");

    verticalPanel.add(closeButton);
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        hide();
      }
    });
  }
}
