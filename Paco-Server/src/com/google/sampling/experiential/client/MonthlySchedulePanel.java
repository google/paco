// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.MonthlySchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class MonthlySchedulePanel extends VerticalPanel {
  private enum Weeks {
    First, Second, Third, Fourth, Fifth
  }

  private enum Days {
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
  }

  private TextBox everyTextBox;

  private RadioButton dayOfMonthRadioButton;
  private ListBox dayListBox;

  private RadioButton dayOfWeekRadioButton;
  private ListBox weekListBox;
  private CheckBox[] dayCheckBoxes;

  /**
   *
   */
  public MonthlySchedulePanel() {
    super();

    addRepeatPanel();
    addDayOfMonthPanel();
    addDayOfWeekPanel();
  }

  /**
   * @return
   */
  public MonthlySchedule getSchedule() {
    MonthlySchedule schedule = new MonthlySchedule();

    retrieveRepeatPanel(schedule);
    retrieveDayOfMonthPanel(schedule);
    retrieveDayOfWeekPanel(schedule);

    return schedule;
  }

  /**
   * @param schedule
   */
  public void setSchedule(MonthlySchedule schedule) {
    updateRepeatPanel(schedule);
    updateDayOfMonthPanel(schedule);
    updateDayOfWeekPanel(schedule);
  }

  private void addRepeatPanel() {
    everyTextBox = new TextBox();

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Every"));
    panel.add(everyTextBox);
    panel.add(new Label("weeks on:"));

    add(panel);
  }

  private void updateRepeatPanel(MonthlySchedule schedule) {
    everyTextBox.setText(Integer.toString(schedule.getEvery()));
  }

  private void retrieveRepeatPanel(MonthlySchedule schedule) {
    schedule.setEvery(Integer.valueOf(everyTextBox.getText()));
  }

  private void addDayOfWeekPanel() {
    dayOfWeekRadioButton = new RadioButton("by", "Day of week");

    weekListBox = new ListBox();

    for (int i = 0; i < Weeks.values().length; i++) {
      weekListBox.addItem(Weeks.values()[i].name());
    }

    dayCheckBoxes = new CheckBox[Days.values().length];

    Grid grid = new Grid(2, dayCheckBoxes.length);

    for (int i = 0; i < dayCheckBoxes.length; i++) {
      grid.setWidget(0, i, dayCheckBoxes[i]);
      grid.setText(1, i, Days.values()[i].name().substring(0, 1));
    }

    Panel dayOfWeekPanel = new HorizontalPanel();

    dayOfWeekPanel.add(weekListBox);
    dayOfWeekPanel.add(grid);

    Panel panel = new VerticalPanel();

    panel.add(dayOfWeekRadioButton);
    panel.add(dayOfWeekPanel);

    add(panel);
  }

  private void updateDayOfWeekPanel(MonthlySchedule schedule) {
    if (schedule.isByDayOfWeek() == false) {
      return;
    }

    for (int i = 0; i < dayCheckBoxes.length; i++) {
      int value = 1 << i;
      dayCheckBoxes[i].setValue((schedule.getDayRepeat() & value) == value);
    }
  }

  private void retrieveDayOfWeekPanel(MonthlySchedule schedule) {
    if (dayOfWeekRadioButton.getValue() == false) {
      return;
    }

    int dayRepeat = 0;

    for (int i = 0; i < dayCheckBoxes.length; i++) {
      int value = 1 << i;
      dayRepeat |= (dayCheckBoxes[i].getValue() ? value : 0);
    }

    schedule.setByDayOfWeek(true);
    schedule.setDayRepeat(dayRepeat);
  }

  private void addDayOfMonthPanel() {
    dayOfMonthRadioButton = new RadioButton("by", "Day of month");

    dayListBox = new ListBox();

    for (int i = 1; i < 32; i++) {
      dayListBox.addItem(Integer.toString(i));
    }

    Panel panel = new HorizontalPanel();

    panel.add(dayOfMonthRadioButton);
    panel.add(dayListBox);

    add(panel);
  }

  private void updateDayOfMonthPanel(MonthlySchedule schedule) {
    if (schedule.isByDayOfWeek()) {
      return;
    }

    dayListBox.setSelectedIndex(schedule.getDayRepeat() - 1);
  }

  private void retrieveDayOfMonthPanel(MonthlySchedule schedule) {
    if (dayOfMonthRadioButton.getValue() == false) {
      return;
    }

    schedule.setByDayOfWeek(false);
    schedule.setDayRepeat(dayListBox.getSelectedIndex() + 1);
  }
}
