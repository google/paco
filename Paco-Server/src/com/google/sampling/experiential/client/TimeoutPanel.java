package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model2.PacoNotificationAction;

public class TimeoutPanel extends Composite {

  private PacoNotificationAction pacoAction;
  private HorizontalPanel mainPanel;
  private String defaultTimeout;

  public TimeoutPanel(final PacoNotificationAction pacoAction, String defaultTimeout) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.pacoAction = pacoAction;
    this.defaultTimeout = defaultTimeout;

    mainPanel = new HorizontalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);

    Label timeoutLabel = new Label(myConstants.timeout() +":");
    timeoutLabel.setStyleName("gwt-Label-Header");
    mainPanel.add(timeoutLabel);

    final TextBox textBox = new TextBox();
    textBox.setWidth("5em");
    textBox.setMaxLength(5);
    mainPanel.add(textBox);

    textBox.setText(getTimeout());

    Label minutesLabel = new Label("(" + myConstants.minutes() + ")");
    minutesLabel.setStyleName("paco-small");
    mainPanel.add(minutesLabel);

    textBox.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent arg0) {
        String text = textBox.getText();

        try {
          int timeoutMinutes = Integer.parseInt(text);
          pacoAction.setTimeout(timeoutMinutes);
        } catch (NumberFormatException nfe) {

        }

      }
    });

  }

  private String getTimeout() {
    if (pacoAction.getTimeout() != null) {
      return pacoAction.getTimeout().toString();
    } else {
      return defaultTimeout;
//      if (pacoAction instanceof ScheduleTrigger) {
//        if (((ScheduleTrigger)pacoAction).getScheduleType().equals(ScheduleTrigger.ESM)) {
//          return PacoNotificationAction.ESM_SIGNAL_TIMEOUT;
//        } else {
//          return PacoNotificationAction.FIXED_SCHEDULE_TIMEOUT;
//        }
//      } else {
//        return PacoNotificationAction.TRIGGER_SIGNAL_TIMEOUT;
//      }
    }

  }

}
