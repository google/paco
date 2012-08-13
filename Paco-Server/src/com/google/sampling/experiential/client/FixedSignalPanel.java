// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.FixedSignal;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class FixedSignalPanel extends VerticalPanel {
  private VerticalPanel timePanels;

  /**
   *
   */
  public FixedSignalPanel() {
    super();

    addTimePanel();
  }

  /**
   * @return
   */
  public FixedSignal getSignal() {
    FixedSignal signal = new FixedSignal();

    retrieveTimePanel(signal);

    return signal;
  }

  /**
   * @param signal
   */
  public void setSignal(FixedSignal signal) {
    updateTimePanel(signal);
  }

  private void addTimePanel() {
    clear();

    timePanels = new VerticalPanel();
    timePanels.add(new TimePanel(null));

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Times:"));
    panel.add(timePanels);

    add(panel);
  }

  private void updateTimePanel(FixedSignal signal) {
    timePanels.clear();

    for (Date time : signal.getTimes()) {
      TimePanel timePanel = new TimePanel(null);
      timePanel.setDateTime(time);
      timePanels.add(timePanel);
    }
  }

  private void retrieveTimePanel(FixedSignal signal) {
    for (int i = 0; i < timePanels.getWidgetCount(); i++) {
      signal.addTime(((TimePanel) timePanels.getWidget(i)).getDateTime());
    }
  }
}
