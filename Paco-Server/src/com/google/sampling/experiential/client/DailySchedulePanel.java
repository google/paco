// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.DailySchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DailySchedulePanel extends VerticalPanel {
  private TextBox everyTextBox;

  /**
   *
   */
  public DailySchedulePanel() {
    super();

    addRepeatPanel();
  }

  /**
   * @return
   */
  public DailySchedule getSchedule() {
    DailySchedule schedule = new DailySchedule();

    retrieveRepeatPanel(schedule);

    return schedule;
  }

  /**
   * @param schedule
   */
  public void setSchedule(DailySchedule schedule) {
    updateRepeatPanel(schedule);
  }

  private void addRepeatPanel() {
    everyTextBox = new TextBox();

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Every"));
    panel.add(everyTextBox);
    panel.add(new Label("days"));

    add(panel);
  }

  private void updateRepeatPanel(DailySchedule schedule) {
    everyTextBox.setText(Integer.toString(schedule.getEvery()));
  }

  private void retrieveRepeatPanel(DailySchedule schedule) {
    schedule.setEvery(Integer.valueOf(everyTextBox.getText()));
  }
}
