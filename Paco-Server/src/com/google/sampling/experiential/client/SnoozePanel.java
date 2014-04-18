package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.SignalingMechanismDAO;

public class SnoozePanel extends Composite {

  private SignalingMechanismDAO signalingMechanism;
  private VerticalPanel mainPanel;

  public SnoozePanel(final SignalingMechanismDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.signalingMechanism = schedule;
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    mainPanel.setSpacing(2);

    mainPanel.add(createSnoozeCountPanel(schedule, myConstants));
    mainPanel.add(createSnoozeTimePanel(schedule, myConstants));
  }

  private HorizontalPanel createSnoozeCountPanel(final SignalingMechanismDAO schedule, MyConstants myConstants) {
    HorizontalPanel snoozeCountPanel = new HorizontalPanel();

    Label snoozeCountLabel = new Label(myConstants.snoozeCount() +":");
    snoozeCountLabel.setStyleName("gwt-Label-Header");
    snoozeCountPanel.add(snoozeCountLabel);

    final TextBox snoozeCountTextBox = new TextBox();
    snoozeCountTextBox.setWidth("5em");
    snoozeCountTextBox.setMaxLength(5);
    snoozeCountPanel.add(snoozeCountTextBox);

    snoozeCountTextBox.setText(Integer.toString(signalingMechanism.getSnoozeCount()));

    Label repeatOptionsWarningLabel = new Label("(" + myConstants.only1SnoozeRepeat() + ")");
    repeatOptionsWarningLabel.setStyleName("paco-small");
    snoozeCountPanel.add(repeatOptionsWarningLabel);

    snoozeCountTextBox.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        String text = snoozeCountTextBox.getText();

        try {
          int snoozeCount = Integer.parseInt(text);
          schedule.setSnoozeCount(snoozeCount);
        } catch (NumberFormatException nfe) {

        }

      }
    });
    return snoozeCountPanel;
  }

  private HorizontalPanel createSnoozeTimePanel(final SignalingMechanismDAO schedule, MyConstants myConstants) {
    HorizontalPanel snoozeTimePanel = new HorizontalPanel();

    Label snoozeTimeLabel = new Label(myConstants.snoozeTime() +":");
    snoozeTimeLabel.setStyleName("gwt-Label-Header");
    snoozeTimePanel.add(snoozeTimeLabel);

    final TextBox snoozeTimeTextBox = new TextBox();
    snoozeTimeTextBox.setWidth("5em");
    snoozeTimeTextBox.setMaxLength(5);
    snoozeTimePanel.add(snoozeTimeTextBox);

    snoozeTimeTextBox.setText(Integer.toString(signalingMechanism.getSnoozeTimeInMinutes()));

    Label minutesLabel = new Label("(" + myConstants.minutes() + ")");
    minutesLabel.setStyleName("paco-small");
    snoozeTimePanel.add(minutesLabel);

    snoozeTimeTextBox.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        String text = snoozeTimeTextBox.getText();

        try {
          int snoozeCount = Integer.parseInt(text);
          schedule.setSnoozeTimeInMinutes(snoozeCount);
        } catch (NumberFormatException nfe) {

        }

      }
    });
    return snoozeTimePanel;
  }

}
