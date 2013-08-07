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
  private SignalMechanismChooserPanel ancestorSignalPanel;
  private HorizontalPanel mainPanel;
  
  // Visible for testing
  protected MouseOverTextBoxBase textBox;

  public TimeoutPanel(final SignalingMechanismDAO schedule, SignalMechanismChooserPanel ancestor) {
    MyConstants myConstants = GWT.create(MyConstants.class);
    this.signalingMechanism = schedule;
    this.ancestorSignalPanel = ancestor;
    mainPanel = new HorizontalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
        
    Label timeoutLabel = new Label(myConstants.timeout() +":");
    timeoutLabel.setStyleName("gwt-Label-Header");
    mainPanel.add(timeoutLabel);
    
    textBox = new MouseOverTextBoxBase(MouseOverTextBoxBase.TEXT_BOX,
                                       myConstants.timeoutMustBeValid());
    textBox.setWidth("5em");
    textBox.setMaxLength(5);
    mainPanel.add(textBox);
    
    textBox.setText(getTimeout());
    
    Label minutesLabel = new Label("(" + myConstants.minutes() + ")");
    minutesLabel.setStyleName("paco-small");
    mainPanel.add(minutesLabel);
    
    textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> arg0) {
        String text = textBox.getText();
        try {
          int timeoutMinutes = Integer.parseInt(text);
          schedule.setTimeout(timeoutMinutes);
          ExperimentCreationPanel.setPanelHighlight(textBox, true);
          textBox.disableMouseOver();
          removeTimeoutError();
        } catch (NumberFormatException nfe) {
          handleTimeoutError();
        } catch (IllegalArgumentException e) {
          handleTimeoutError();
        }
      }
      
      private void handleTimeoutError() {
        ExperimentCreationPanel.setPanelHighlight(textBox, false);
        addTimeoutError();
        textBox.enableMouseOver();
      }
    });
    
  }

  private String getTimeout() {
    if (signalingMechanism.getTimeout() != null) {
      return signalingMechanism.getTimeout().toString();
    } else {
      if (signalingMechanism instanceof SignalScheduleDAO && ((SignalScheduleDAO)signalingMechanism).getScheduleType().equals(SignalScheduleDAO.ESM)) {
        return "59";
      } else {
        return "479";
      }
    } 
  }
  
  public void removeTimeoutError() {
    ancestorSignalPanel.removeTimeoutErrorMessage(textBox.getMessage());
  }
  
  public void addTimeoutError() {
    ancestorSignalPanel.addTimeoutErrorMessage(textBox.getMessage());
  }

}
