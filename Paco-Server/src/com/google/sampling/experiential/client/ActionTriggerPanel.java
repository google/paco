package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.pacoapp.paco.shared.model2.ActionTrigger;

public abstract class ActionTriggerPanel extends Composite {

  protected ActionTriggerListPanel parent;
  protected MyConstants myConstants;
  private ActionTrigger trigger;
  protected VerticalPanel rootPanel;

  public ActionTriggerPanel(ActionTriggerListPanel parent, ActionTrigger trigger) {
    this.parent = parent;
    this.trigger = trigger;
    myConstants = GWT.create(MyConstants.class);
  }

  public ActionTrigger getActionTrigger() {
    return trigger;
  }

  protected void init() {
    rootPanel = new VerticalPanel();
    initWidget(rootPanel);

    createTriggerTypeSpecificUI(rootPanel);

    rootPanel.add(createActionListPanel());

    rootPanel.add(createDeleteButton());
  }

  protected abstract void createTriggerTypeSpecificUI(VerticalPanel verticalPanel);

  protected Widget createDeleteButton() {
    HorizontalPanel buttonPanel = new HorizontalPanel();
    Button deleteButton = new Button("Delete Action Trigger");
    buttonPanel.add(deleteButton);
    deleteButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        parent.deleteActionTrigger(ActionTriggerPanel.this);
      }

    });
    return buttonPanel;
  }

  private Widget createActionListPanel() {
    return new PacoActionListPanel(trigger);
  }


}
