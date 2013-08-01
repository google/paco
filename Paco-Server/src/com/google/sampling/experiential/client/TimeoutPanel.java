package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;

public class TimeoutPanel extends Composite {

  private SignalingMechanismDAO signalingMechanism;
  private HorizontalPanel mainPanel;

  public TimeoutPanel(final SignalingMechanismDAO schedule) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.signalingMechanism = schedule;
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
          schedule.setTimeout(timeoutMinutes);
        } catch (NumberFormatException nfe) {
          
        }
        
      }
    });
    
  }

  private String getTimeout() {
    if (signalingMechanism.getTimeout() != null) {
      return signalingMechanism.getTimeout().toString();
    } else {
      if (signalingMechanism instanceof SignalScheduleDAO) {
        if (((SignalScheduleDAO)signalingMechanism).getScheduleType().equals(SignalScheduleDAO.ESM)) {
          return SignalingMechanismDAO.ESM_SIGNAL_TIMEOUT;
        } else {
          return SignalingMechanismDAO.FIXED_SCHEDULE_TIMEOUT;
        }
      } else {
        return SignalingMechanismDAO.TRIGGER_SIGNAL_TIMEOUT;
      }
    }
    
  }

}
