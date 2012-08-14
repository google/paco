// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.DailySchedule;
import com.google.sampling.experiential.shared.MonthlySchedule;
import com.google.sampling.experiential.shared.Schedule;
import com.google.sampling.experiential.shared.Schedule.Type;
import com.google.sampling.experiential.shared.WeeklySchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SchedulePanel extends VerticalPanel implements ChangeHandler {
  private ListBox typeListBox;
  private DailySchedulePanel dailyPanel;
  private WeeklySchedulePanel weeklyPanel;
  private MonthlySchedulePanel monthlyPanel;

  /**
   *
   */
  public SchedulePanel() {
    super();

    addTypePanel();
    addDailyPanel();
    addWeeklyPanel();
    addMonthlyPanel();
  }

  /**
   * @return the schedule
   */
  public Schedule getSchedule() {
    Schedule schedule = null;

    switch (getType()) {
    case Daily:
      schedule = dailyPanel.getSchedule();
      break;
    case Weekly:
      schedule = weeklyPanel.getSchedule();
      break;
    case Monthly:
      schedule = monthlyPanel.getSchedule();
      break;
    }

    return schedule;
  }

  /**
   * @param schedule the schedule
   */
  public void setSchedule(Schedule schedule) {
    updateTypePanel(schedule);
    updateDailyPanel(schedule);
    updateWeeklyPanel(schedule);
    updateMonthlyPanel(schedule);
  }

  private void addTypePanel() {
    Panel panel = new HorizontalPanel();
    Label label = new Label("Schedule: ");

    typeListBox = new ListBox();
    typeListBox.addChangeHandler(this);

    for (int i = 0; i < Type.values().length; i++) {
      typeListBox.addItem(Type.values()[i].name());
    }

    panel.add(label);
    panel.add(typeListBox);
  }

  private void updateTypePanel(Schedule schedule) {
    typeListBox.setSelectedIndex(schedule.getType().ordinal());
  }

  private void addDailyPanel() {
    dailyPanel = new DailySchedulePanel();

    add(dailyPanel);
  }

  private void updateDailyPanel(Schedule schedule) {
    if (schedule.getType().equals(Type.Daily) == false) {
      return;
    }

    dailyPanel.setSchedule((DailySchedule) schedule);
  }

  private void addWeeklyPanel() {
    weeklyPanel = new WeeklySchedulePanel();

    add(weeklyPanel);
  }

  private void updateWeeklyPanel(Schedule schedule) {
    if (schedule.getType().equals(Type.Weekly) == false) {
      return;
    }

    weeklyPanel.setSchedule((WeeklySchedule) schedule);
  }

  private void addMonthlyPanel() {
    monthlyPanel = new MonthlySchedulePanel();

    add(monthlyPanel);
  }

  private void updateMonthlyPanel(Schedule schedule) {
    if (schedule.getType().equals(Type.Monthly) == false) {
      return;
    }

    monthlyPanel.setSchedule((MonthlySchedule) schedule);
  }

  private Type getType() {
    return Type.valueOf(typeListBox.getItemText(typeListBox.getSelectedIndex()));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event
   * .dom.client.ChangeEvent)
   */
  @Override
  public void onChange(ChangeEvent event) {
    if (event.getSource() == typeListBox) {
      Type type = getType();

      if (type == Type.Daily) {
        dailyPanel.setVisible(true);
      } else {
        dailyPanel.setVisible(false);
      }

      if (type == Type.Weekly) {
        weeklyPanel.setVisible(true);
      } else {
        weeklyPanel.setVisible(false);
      }

      if (type == Type.Monthly) {
        monthlyPanel.setVisible(true);
      } else {
        monthlyPanel.setVisible(false);
      }
    }
  }
}
