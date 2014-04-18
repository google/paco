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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.TriggerDAO;

/**
 * View trigger configuration.
 *
 */
public class TriggerPanel extends Composite {

  private MyConstants myConstants;
  private TriggerDAO trigger;
  private Widget sourceIdentifierPanel;

  public TriggerPanel(TriggerDAO triggerDAO) {
    this.trigger = triggerDAO;
    myConstants = GWT.create(MyConstants.class);

    VerticalPanel verticalPanel = new VerticalPanel();
    initWidget(verticalPanel);
    verticalPanel.add(createIosIncompatibleLabel());
    verticalPanel.add(createTriggerChooser());
    sourceIdentifierPanel = createTriggerSourceIdentifierChooser();
    verticalPanel.add(sourceIdentifierPanel);
    verticalPanel.add(createDelayChooser());

    TimeoutPanel timeoutPanel = new TimeoutPanel(trigger);
    verticalPanel.add(timeoutPanel);
    timeoutPanel.setWidth("286px");

    MinimumBufferPanel minimumBufferPanel = new MinimumBufferPanel(trigger);
    verticalPanel.add(minimumBufferPanel);
    minimumBufferPanel.setWidth("286px");

    SnoozePanel snoozePanel = new SnoozePanel(trigger);
    verticalPanel.add(snoozePanel);
    snoozePanel.setWidth("286px");

  }

  private Widget createIosIncompatibleLabel() {
    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\" weight=\"bold\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");

    return html;
  }

  private Widget createTriggerSourceIdentifierChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.chooseTriggerSourceIdentifier()));
    line.add(createSourceIdentifierTextEdit());
    line.setVisible(trigger.getEventCode() == TriggerDAO.PACO_ACTION_EVENT || trigger.getEventCode() == TriggerDAO.APP_USAGE);
    return line;
  }

  private Widget createSourceIdentifierTextEdit() {
    //create text editor
    final TextBox valueBox = new TextBox();
    if (trigger.getSourceIdentifier() != null) {
      valueBox.setText(trigger.getSourceIdentifier());
    }
    valueBox.setEnabled(true);
    valueBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        trigger.setSourceIdentifier(valueBox.getText());
      }

    });
    return valueBox;
  }

  private Widget createDelayChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.chooseTriggerDelay()));
    line.add(createDelayTextEdit());
    return line;
  }

  private Widget createDelayTextEdit() {
    //create text editor
    final TextBox valueBox = new TextBox();
    if (trigger.getDelay() != 0) {
      valueBox.setText(Long.toString(trigger.getDelay() / 1000));
    }
    valueBox.setEnabled(true);
    valueBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        try {
          trigger.setDelay(Long.parseLong(valueBox.getText()) * 1000);
        } catch (NumberFormatException e) {
          Window.alert("Please enter a valid number in seconds for the trigger delay");
        }
      }

    });
    return valueBox;
  }

  private Widget createTriggerChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.chooseTrigger()));

    ListBox widgetWithValue = new ListBox();
    widgetWithValue.addItem("Choose Trigger Event");
    for (int i = 0; i < TriggerDAO.EVENT_NAMES.length; i++) {
      widgetWithValue.addItem(TriggerDAO.EVENT_NAMES[i]);
    }
    int event = trigger.getEventCode();
    if (event != 0) {
      widgetWithValue.setSelectedIndex(event);
    }
    addListSelectionListener(widgetWithValue);
    line.add(widgetWithValue);
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

  private void addListSelectionListener(final ListBox listBox) {
    listBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int index = listBox.getSelectedIndex();
        respondToListSelection(index);
      }

    });
  }

  private void respondToListSelection(int index) {
    trigger.setEventCode(TriggerDAO.EVENTS[index - 1]);
    sourceIdentifierPanel.setVisible(trigger.getEventCode() == TriggerDAO.PACO_ACTION_EVENT || trigger.getEventCode() == TriggerDAO.APP_USAGE);
  }


}
