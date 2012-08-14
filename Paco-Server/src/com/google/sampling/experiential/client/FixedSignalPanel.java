// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.FixedSignal;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class FixedSignalPanel extends VerticalPanel implements ClickHandler {
  private VerticalPanel timePanels;
  private Button addButton;

  /**
   *
   */
  public FixedSignalPanel() {
    super();

    addTimePanel();
  }

  /**
   * @return the fixed signal
   */
  public FixedSignal getSignal() {
    FixedSignal signal = new FixedSignal();

    retrieveTimePanel(signal);

    return signal;
  }

  /**
   * @param signal the fixed signal
   */
  public void setSignal(FixedSignal signal) {
    updateTimePanel(signal);
  }

  private void addTimePanel() {
    timePanels = new VerticalPanel();
    addButton = new Button("+");
    addButton.addClickHandler(this);

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Times:"));
    panel.add(timePanels);
    panel.add(addButton);

    add(panel);
  }

  private void updateTimePanel(FixedSignal signal) {
    timePanels.clear();

    for (Date time : signal.getTimes()) {
      TimePickerPanel timePanel = new TimePickerPanel();
      timePanel.setTime(time);
      timePanel.addClickHandler(this);
      timePanels.add(timePanel);
    }
  }

  private void retrieveTimePanel(FixedSignal signal) {
    for (int i = 0; i < timePanels.getWidgetCount(); i++) {
      signal.addTime(((TimePickerPanel) timePanels.getWidget(i)).getTime());
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent
   * )
   */
  @Override
  public void onClick(ClickEvent event) {
    if (event.getSource() instanceof TimePickerPanel) {
      timePanels.remove((TimePickerPanel) event.getSource());
    } else if (event.getSource() == addButton) {
      TimePickerPanel timePanel = new TimePickerPanel();
      timePanel.addClickHandler(this);
      timePanels.add(timePanel);
    }
  }
}
