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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pacoapp.paco.shared.model2.InterruptCue;

/**
 * Trigger Cue configuration.
 *
 */
public class TriggerCuePanel extends Composite {

  private MyConstants myConstants;
  private InterruptCue triggerCueDAO;
  private Widget cueSourceIdentifierPanel;
  private TriggerCueListPanel parent;

  public TriggerCuePanel(TriggerCueListPanel triggerCueListPanel, InterruptCue triggerCueDAO) {
    this.parent = triggerCueListPanel;
    this.triggerCueDAO = triggerCueDAO;
    myConstants = GWT.create(MyConstants.class);

    VerticalPanel verticalPanel = new VerticalPanel();
    initWidget(verticalPanel);

    verticalPanel.add(createCueChooser());

    cueSourceIdentifierPanel = createTriggerCueSourceIdentifierChooser();
    verticalPanel.add(cueSourceIdentifierPanel);
    verticalPanel.add(createListMgmtButtons());
  }

  private HorizontalPanel createListMgmtButtons() {
    HorizontalPanel upperLinePanel = new HorizontalPanel();
    Button deleteButton = new Button("-");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        parent.deleteCue(TriggerCuePanel.this);
      }
    });
    upperLinePanel.add(deleteButton);

    Button addButton = new Button("+");
    upperLinePanel.add(addButton);

    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        parent.addCue(TriggerCuePanel.this);
      }
    });
    return upperLinePanel;
  }



  private Widget createTriggerCueSourceIdentifierChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.chooseTriggerSourceIdentifier()));
    line.add(createSourceIdentifierTextEdit());
    line.setVisible(triggerCueDAO.getCueCode() == InterruptCue.PACO_ACTION_EVENT || triggerCueDAO.getCueCode() == InterruptCue.APP_USAGE);
    return line;
  }

  private Widget createSourceIdentifierTextEdit() {
    final TextBox valueBox = new TextBox();
    if (triggerCueDAO.getCueSource() != null) {
      valueBox.setText(triggerCueDAO.getCueSource());
    }
    valueBox.setEnabled(true);
    valueBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        triggerCueDAO.setCueSource(valueBox.getText());
      }

    });
    return valueBox;
  }

  private Widget createCueChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.chooseTriggerCue()));

    final ListBox listBox = new ListBox();
    listBox.addItem(myConstants.chooseTriggerCue());
    for (int i = 0; i < InterruptCue.CUE_EVENT_NAMES.length; i++) {
      listBox.addItem(InterruptCue.CUE_EVENT_NAMES[i]);
    }

    Integer event = triggerCueDAO.getCueCode();
    listBox.setSelectedIndex(event);

    listBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int index = listBox.getSelectedIndex();
        triggerCueDAO.setCueCode(InterruptCue.CUE_EVENTS[index - 1]);
        cueSourceIdentifierPanel.setVisible(triggerCueDAO.getCueCode() == InterruptCue.PACO_ACTION_EVENT || triggerCueDAO.getCueCode() == InterruptCue.APP_USAGE);
      }

    });
    line.add(listBox);
    return line;
  }

  private Label createLabel(String chooseTrigger) {
    Label label = new Label(chooseTrigger + ": ");
    label.setStyleName("keyLabel");
    return label;
  }

  private static HorizontalPanel createHorizontalContainer() {
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    return line; //qq
  }

  public InterruptCue getCue() {
    return triggerCueDAO;
  }

}
