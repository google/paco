package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;

public class SnoozePanel extends Composite {

  private PacoNotificationAction pacoNotificationAction;
  private VerticalPanel mainPanel;

  public SnoozePanel(final PacoNotificationAction action) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.pacoNotificationAction = action;
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    mainPanel.setSpacing(2);

    mainPanel.add(createSnoozeCountPanel(action, myConstants));
    mainPanel.add(createSnoozeTimePanel(action, myConstants));
  }

  private HorizontalPanel createSnoozeCountPanel(final PacoNotificationAction action, MyConstants myConstants) {
    HorizontalPanel snoozeCountPanel = new HorizontalPanel();

    Label snoozeCountLabel = new Label(myConstants.snoozeCount() +":");
    snoozeCountLabel.setStyleName("gwt-Label-Header");
    snoozeCountPanel.add(snoozeCountLabel);

    final TextBox snoozeCountTextBox = new TextBox();
    snoozeCountTextBox.setWidth("5em");
    snoozeCountTextBox.setMaxLength(5);
    snoozeCountPanel.add(snoozeCountTextBox);

    snoozeCountTextBox.setText(Integer.toString(pacoNotificationAction.getSnoozeCount()));

    Label repeatOptionsWarningLabel = new Label("(" + myConstants.only1SnoozeRepeat() + ")");
    repeatOptionsWarningLabel.setStyleName("paco-small");
    snoozeCountPanel.add(repeatOptionsWarningLabel);

    snoozeCountTextBox.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        String text = snoozeCountTextBox.getText();

        try {
          int snoozeCount = Integer.parseInt(text);
          action.setSnoozeCount(snoozeCount);
        } catch (NumberFormatException nfe) {

        }

      }
    });
    return snoozeCountPanel;
  }

  private HorizontalPanel createSnoozeTimePanel(final PacoNotificationAction action, MyConstants myConstants) {
    HorizontalPanel snoozeTimePanel = new HorizontalPanel();

    Label snoozeTimeLabel = new Label(myConstants.snoozeTime() +":");
    snoozeTimeLabel.setStyleName("gwt-Label-Header");
    snoozeTimePanel.add(snoozeTimeLabel);

    final TextBox snoozeTimeTextBox = new TextBox();
    snoozeTimeTextBox.setWidth("5em");
    snoozeTimeTextBox.setMaxLength(5);
    snoozeTimePanel.add(snoozeTimeTextBox);

    snoozeTimeTextBox.setText(Integer.toString(pacoNotificationAction.getSnoozeTimeInMinutes()));

    Label minutesLabel = new Label("(" + myConstants.minutes() + ")");
    minutesLabel.setStyleName("paco-small");
    snoozeTimePanel.add(minutesLabel);

    snoozeTimeTextBox.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        String text = snoozeTimeTextBox.getText();

        try {
          int snoozeCount = Integer.parseInt(text);
          action.setSnoozeTimeInMinutes(snoozeCount);
        } catch (NumberFormatException nfe) {

        }

      }
    });
    return snoozeTimePanel;
  }

}
