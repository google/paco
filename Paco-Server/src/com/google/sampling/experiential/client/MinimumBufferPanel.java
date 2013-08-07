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

public class MinimumBufferPanel extends Composite {

  private SignalingMechanismDAO signalingMechanism;
  private HorizontalPanel mainPanel;

  public MinimumBufferPanel(final SignalingMechanismDAO signalingMechanism) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.signalingMechanism = signalingMechanism;
    mainPanel = new HorizontalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
        
    Label minimumBufferLabel = new Label(myConstants.minimumBuffer() +":");
    minimumBufferLabel.setStyleName("gwt-Label-Header");
    mainPanel.add(minimumBufferLabel);
    
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
          int minBufferMinutes = Integer.parseInt(text);
          signalingMechanism.setMinimumBuffer(minBufferMinutes);
        } catch (NumberFormatException nfe) {
          
        }
        
      }
    });
    
  }

  private String getTimeout() {
    if (signalingMechanism.getMinimumBuffer() != null) {
      return signalingMechanism.getMinimumBuffer().toString();
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
