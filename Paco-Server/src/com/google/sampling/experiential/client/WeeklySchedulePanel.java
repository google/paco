// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.WeeklySchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class WeeklySchedulePanel extends VerticalPanel {
  private enum Days {
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
  }

  private TextBox everyTextBox;
  private CheckBox[] checkBoxes;

  /**
   *
   */
  public WeeklySchedulePanel() {
    super();

    addRepeatPanel();
    addDaysPanel();
  }

  /**
   * @return
   */
  public WeeklySchedule getSchedule() {
    WeeklySchedule schedule = new WeeklySchedule();

    retrieveRepeatPanel(schedule);
    retrieveDaysPanel(schedule);

    return schedule;
  }

  /**
   * @param schedule
   */
  public void setSchedule(WeeklySchedule schedule) {
    updateRepeatPanel(schedule);
    updateDaysPanel(schedule);
  }

  private void addRepeatPanel() {
    everyTextBox = new TextBox();

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Every"));
    panel.add(everyTextBox);
    panel.add(new Label("weeks on:"));

    add(panel);
  }

  private void updateRepeatPanel(WeeklySchedule schedule) {
    everyTextBox.setText(Integer.toString(schedule.getEvery()));
  }

  private void retrieveRepeatPanel(WeeklySchedule schedule) {
    schedule.setEvery(Integer.valueOf(everyTextBox.getText()));
  }

  private void addDaysPanel() {
    checkBoxes = new CheckBox[Days.values().length];

    Grid grid = new Grid(2, checkBoxes.length);

    for (int i = 0; i < checkBoxes.length; i++) {
      grid.setWidget(0, i, checkBoxes[i]);
      grid.setText(1, i, Days.values()[i].name().substring(0, 1));
    }

    add(grid);
  }

  private void updateDaysPanel(WeeklySchedule schedule) {
    for (int i = 0; i < checkBoxes.length; i++) {
      checkBoxes[i].setValue((schedule.getDayRepeat() & (1 << i)) == (1 << i));
    }
  }

  private void retrieveDaysPanel(WeeklySchedule schedule) {
    int dayRepeat = 0;

    for (int i = 0; i < checkBoxes.length; i++) {
      dayRepeat |= (checkBoxes[i].getValue() ? (1 << i) : 0);
    }

    schedule.setDayRepeat(dayRepeat);
  }
}
