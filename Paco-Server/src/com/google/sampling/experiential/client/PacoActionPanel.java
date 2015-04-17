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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoActionAllOthers;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Trigger Cue configuration.
 *
 */
public class PacoActionPanel extends Composite {

  private MyConstants myConstants;
  private PacoAction pacoAction;


  private PacoActionListPanel parent;
  private VerticalPanel mainPanel;
  private TimeoutPanel timeoutPanel;
  private SnoozePanel snoozePanel;
  private Widget delayChooser;
  private Widget customScriptEditorPanel;

  public PacoActionPanel(PacoActionListPanel actionListPanel, PacoAction pacoAction) {
    this.pacoAction = pacoAction;
    this.parent = actionListPanel;
    myConstants = GWT.create(MyConstants.class);

    mainPanel = new VerticalPanel();
    initWidget(mainPanel);

    mainPanel.add(createActionChooser());

    toggleNotificationPanels();
    mainPanel.add(createListMgmtButtons());
  }

  private void toggleNotificationPanels() {
    Integer actionCode = pacoAction.getActionCode();
    if (isNotificationAction(actionCode)) {
      PacoNotificationAction pacoNotificationAction = null;
      // TODO fix this nonsense! We should manage the pacoAction types better
      if (pacoAction instanceof PacoNotificationAction) {
        pacoNotificationAction = (PacoNotificationAction) pacoAction;
      } else {
        pacoNotificationAction = new PacoNotificationAction();
      }
      if (customScriptEditorPanel != null) {
        mainPanel.remove(customScriptEditorPanel);
        customScriptEditorPanel = null;
      }
      delayChooser = createDelayChooser();
      mainPanel.add(delayChooser);
      delayChooser.setVisible(parent.getTrigger() instanceof InterruptTrigger);

      if (timeoutPanel == null) {
        timeoutPanel = new TimeoutPanel(pacoNotificationAction, Integer.toString(pacoNotificationAction.getTimeout()));
        mainPanel.add(timeoutPanel);
        timeoutPanel.setWidth("286px");
      }

      if (snoozePanel == null) {
        snoozePanel = new SnoozePanel(pacoNotificationAction);
        mainPanel.add(snoozePanel);
        snoozePanel.setWidth("286px");
      }
    } else {
      PacoActionAllOthers pacoActionAllOthers = null;
      if (pacoAction instanceof PacoActionAllOthers) {
        pacoActionAllOthers = (PacoActionAllOthers) pacoAction;
      } else {
        pacoActionAllOthers = new PacoActionAllOthers();
      }
      // TODO replace with ACe Editor widget in disclosure panel
      HorizontalPanel line = createHorizontalContainer();
      line.add(createLabel(myConstants.triggerCustomScript()));

      line.add(createCustomScriptEditor());
      line.setVisible(pacoAction.getActionCode() == PacoAction.EXECUTE_SCRIPT_ACTION_CODE);


      customScriptEditorPanel = createCustomScriptEditorDisclosurePanel();
      mainPanel.add(customScriptEditorPanel);

      if (delayChooser != null) {
        mainPanel.remove(delayChooser);
        delayChooser = null;
      }
      if (timeoutPanel != null) {
        mainPanel.remove(timeoutPanel);
        timeoutPanel = null;
      }
      if (snoozePanel != null) {
        mainPanel.remove(snoozePanel);
        snoozePanel = null;
      }
    }
  }

  private boolean isNotificationAction(Integer actionCode) {
    return actionCode.equals(PacoActionAllOthers.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE)
           || actionCode.equals(PacoActionAllOthers.NOTIFICATION_ACTION_CODE);
  }

  private HorizontalPanel createListMgmtButtons() {
    HorizontalPanel upperLinePanel = new HorizontalPanel();
    Button deleteButton = new Button("Delete Action");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        parent.deleteAction(PacoActionPanel.this);
      }
    });
    upperLinePanel.add(deleteButton);

    Button addButton = new Button("Add Action");
    upperLinePanel.add(addButton);

    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        parent.addAction(PacoActionPanel.this);
      }
    });
    return upperLinePanel;
  }

  private Widget createCustomScriptEditor() {
    final TextBox valueBox = new TextBox();
    if (((PacoActionAllOthers)pacoAction).getCustomScript() != null) {
      valueBox.setText(((PacoActionAllOthers)pacoAction).getCustomScript());
    }
    valueBox.setEnabled(true);
    valueBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        ((PacoActionAllOthers)pacoAction).setCustomScript(valueBox.getText());
      }

    });
    return valueBox;
  }

  private Widget createActionChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.triggerActions()));

    final ListBox listBox = new ListBox();
    listBox.addItem(myConstants.chooseTriggerAction());
    for (int i = 0; i < PacoActionAllOthers.ACTION_NAMES.length; i++) {
      listBox.addItem(PacoActionAllOthers.ACTION_NAMES[i]);
    }

    Integer event = pacoAction.getActionCode();
    if (event != null && event != 0) {
      listBox.setSelectedIndex(event);
    }

    listBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int index = listBox.getSelectedIndex();

        int actionCode = PacoAction.ACTIONS[index - 1];
        if (isNotificationAction(actionCode) && !isNotificationAction(pacoAction.getActionCode())) {
          parent.getTrigger().getActions().remove(pacoAction);
          pacoAction = new PacoNotificationAction(0, 10, 59, actionCode, null);
          parent.getTrigger().getActions().add(pacoAction);
        } else if (!isNotificationAction(actionCode) && isNotificationAction(pacoAction.getActionCode())) {
          parent.getTrigger().getActions().remove(pacoAction);
          pacoAction = new PacoActionAllOthers();
          parent.getTrigger().getActions().add(pacoAction);
        }
        pacoAction.setActionCode(actionCode);
        toggleNotificationPanels();
        customScriptEditorPanel.setVisible(pacoAction.getActionCode() == PacoActionAllOthers.EXECUTE_SCRIPT_ACTION_CODE);
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
    return line;
  }

  public PacoAction getAction() {
    return pacoAction;
  }

  private Widget createDelayChooser() {
    HorizontalPanel line = createHorizontalContainer();
    line.add(createLabel(myConstants.chooseTriggerDelay()));
    line.add(createDelayTextEdit());
    return line;
  }

  private Widget createDelayTextEdit() {
    // create text editor
    final TextBox valueBox = new TextBox();
    if (pacoAction instanceof PacoNotificationAction) {
      final PacoNotificationAction pacoNotificationAction = (PacoNotificationAction) pacoAction;
      if (pacoNotificationAction.getDelay() != 0) {
        valueBox.setText(Long.toString(pacoNotificationAction.getDelay() / 1000));
      }
      valueBox.setEnabled(true);
    }
    valueBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        try {
          if (pacoAction instanceof PacoNotificationAction) {
            final PacoNotificationAction pacoNotificationAction = (PacoNotificationAction) pacoAction;
            pacoNotificationAction.setDelay(Long.parseLong(valueBox.getText()) * 1000);
          }
        } catch (NumberFormatException e) {
          Window.alert("Please enter a valid number in seconds for the trigger delay");
        }
      }

    });
    return valueBox;
  }

  final DisclosurePanelImages images = (DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);

  class DisclosurePanelHeader extends HorizontalPanel {
    public DisclosurePanelHeader(boolean isOpen, String html) {
      add(isOpen ? images.disclosurePanelOpen().createImage()
                 : images.disclosurePanelClosed().createImage());
      add(new HTML(html));
    }
  }
  private Widget createCustomScriptEditorDisclosurePanel() {
    mainPanel.add(makeIOSIncompatibleMessage()); // TODO make a container panel or just get rid of this when iOS is ready

    final DisclosurePanel customScriptPanel = new DisclosurePanel();

    final DisclosurePanelHeader closedHeaderWidget =
            new DisclosurePanelHeader(false, "<b>" + myConstants.clickToEditCustomScript() + "</b>");
    final DisclosurePanelHeader openHeaderWidget =
            new DisclosurePanelHeader(true, "<b>" + myConstants.clickToCloseCustomScriptEditor() + "</b >");

    final PacoActionAllOthers pacoActionScript = (PacoActionAllOthers)pacoAction;
    if (pacoActionScript.getCustomScript() != null) {
      customScriptPanel.setHeader(openHeaderWidget);
      customScriptPanel.setOpen(true);
    } else {
      customScriptPanel.setHeader(closedHeaderWidget);
      customScriptPanel.setOpen(false);
    }
    customScriptPanel.addEventHandler(new DisclosureHandler() {
      public void onClose(DisclosureEvent event) {
        boolean currentlyVisible = customScriptPanel.getHeader().isVisible();
        customScriptPanel.setHeader(closedHeaderWidget);
        closedHeaderWidget.setVisible(currentlyVisible);
      }

      public void onOpen(DisclosureEvent event) {
        boolean currentlyVisible = customScriptPanel.getHeader().isVisible();
        customScriptPanel.setHeader(openHeaderWidget);
        openHeaderWidget.setVisible(currentlyVisible);
      }
    });

    VerticalPanel userContentPanel = new VerticalPanel();
    Label instructionLabel = new Label(myConstants.triggerCustomScript());
    userContentPanel.add(instructionLabel);

    final AceEditor customScriptEditor = new AceEditor();
    customScriptEditor.setWidth("800px");
    customScriptEditor.setHeight("600px");
    customScriptEditor.startEditor();
    customScriptEditor.setMode(AceEditorMode.JAVASCRIPT);
    customScriptEditor.setTheme(AceEditorTheme.ECLIPSE);

    if (pacoActionScript.getCustomScript() != null) {
      customScriptEditor.setText(pacoActionScript.getCustomScript());
    }

    userContentPanel.add(customScriptEditor);
    customScriptPanel.setContent(userContentPanel);
    customScriptEditor.addOnChangeHandler(new AceEditorCallback() {

      @Override
      public void invokeAceCallback(JavaScriptObject obj) {
        pacoActionScript.setCustomScript(customScriptEditor.getText());
      }

    });
    return customScriptPanel;
  }

  private HTML makeIOSIncompatibleMessage() {
    HTML html = new HTML("&nbsp;&nbsp;&nbsp;<font color=\"red\" size=\"smaller\"><i>(" + myConstants.iOSIncompatible() + ")</i></font>");
    return html;
  }


}
