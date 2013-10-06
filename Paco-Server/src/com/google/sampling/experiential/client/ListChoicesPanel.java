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
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import java.util.LinkedList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.InputDAO;

/**
 * A collection of all the ListChoicePanels to define the choices for a given
 * Input object whose responsetype is List.
 *
 * @author Bob Evans
 *
 */
public class ListChoicesPanel extends Composite {

  private InputDAO input;
  private VerticalPanel mainPanel;
  private LinkedList<ListChoicePanel> choicePanelsList;

  /**
   * @param input
   */
  public ListChoicesPanel(final InputDAO input) {
    this.input = input;
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
    
    final CheckBox multiselect = new CheckBox("Multiple selections");
    multiselect.setValue(input.getMultiselect());
    multiselect.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        input.setMultiselect(multiselect.getValue());
      }
      
    });
    mainPanel.add(multiselect);
    
    Label lblSignalTimes = new Label("List Choice (s)");
    lblSignalTimes.setStyleName("gwt-Label-Header");
    mainPanel.add(lblSignalTimes);

    choicePanelsList = new LinkedList<ListChoicePanel>();
    String[] choices = input.getListChoices();
    if (choices == null || choices.length == 0) {
      ListChoicePanel choicePanel = new ListChoicePanel(this);
      String choice = choicePanel.getChoice();
      choices = new String[] {choice};
      mainPanel.add(choicePanel);
      choicePanelsList.add(choicePanel);
      input.setListChoices(choices);
    } else {
      for (int i = 0; i < choices.length; i++) {
        ListChoicePanel choicePanel = new ListChoicePanel(this);
        choicePanel.setChoice(choices[i]);
        mainPanel.add(choicePanel);
        choicePanelsList.add(choicePanel);
      }
    }
  }

  public void deleteChoice(ListChoicePanel choicePanel) {
    if (choicePanelsList.size() == 1) {
      return;
    }
    choicePanelsList.remove(choicePanel);
    mainPanel.remove(choicePanel);
    updateChoices();
  }

  public void addChoice(ListChoicePanel choicePanel) {
    int index = choicePanelsList.indexOf(choicePanel);

    ListChoicePanel choicePanel2 = new ListChoicePanel(this);
    choicePanelsList.add(index + 1, choicePanel2);

    int widgetIndex = mainPanel.getWidgetIndex(choicePanel);
    mainPanel.insert(choicePanel2, widgetIndex + 1);

    updateChoices();

  }

  // TODO this is not very efficient.
  private void updateChoices() {
    String[] newTimes = new String[choicePanelsList.size()];
    for (int i = 0; i < choicePanelsList.size(); i++) {
      newTimes[i] = choicePanelsList.get(i).getChoice();
    }
    input.setListChoices(newTimes);
  }

  public void updateChoice(ListChoicePanel choicePanel) {
    int index = choicePanelsList.indexOf(choicePanel);
    input.getListChoices()[index] = choicePanel.getChoice();
  }
  
  public ListChoicePanel getFirstChoicePanel() {
    return getChoicePanel(0);
  }
  
  private ListChoicePanel getChoicePanel(int index) {
    return choicePanelsList.get(index);
  }


}
