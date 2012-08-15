// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.DailySchedule;
import com.google.sampling.experiential.shared.MonthlySchedule;
import com.google.sampling.experiential.shared.Schedule;
import com.google.sampling.experiential.shared.Schedule.Type;
import com.google.sampling.experiential.shared.WeeklySchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ScheduleView extends Composite implements HasValue<Schedule> {
  private static ScheduleUiBinder uiBinder = GWT.create(ScheduleUiBinder.class);

  interface ScheduleUiBinder extends UiBinder<Widget, ScheduleView> {
  }

  @UiField
  TextBox startDate;
  @UiField
  TextBox endDate;
  @UiField
  ListBox types;
  @UiField
  DailyScheduleView dailySchedule;
  @UiField
  WeeklyScheduleView weeklySchedule;
  @UiField
  MonthlyScheduleView monthlySchedule;

  public ScheduleView() {
    initWidget(uiBinder.createAndBindUi(this));

    startDate.getElement().setAttribute("type", "date");
    endDate.getElement().setAttribute("type", "date");

    for (Type type : Type.values()) {
      types.insertItem(type.name(), type.ordinal());
    }

    onChange(null);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Schedule> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public Schedule getValue() {
    Schedule schedule = null;

    if (types.getSelectedIndex() == Type.Daily.ordinal()) {
      schedule = dailySchedule.getValue();
    }

    if (types.getSelectedIndex() == Type.Weekly.ordinal()) {
      schedule = weeklySchedule.getValue();
    }

    if (types.getSelectedIndex() == Type.Monthly.ordinal()) {
      schedule = monthlySchedule.getValue();
    }

    if (schedule != null) {
      // FIXME: Set startDate and endDate
      // schedule.setStartDate(startDate.getValue());
      // schedule.setEndDate(startDate.getValue());
    }

    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Schedule schedule) {
    setValue(schedule, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(Schedule schedule, boolean fireEvents) {
    if (schedule.getType() == Type.Daily) {
      types.setSelectedIndex(Type.Daily.ordinal());
      dailySchedule.setValue((DailySchedule) schedule);
    }

    if (schedule.getType() == Type.Weekly) {
      types.setSelectedIndex(Type.Weekly.ordinal());
      weeklySchedule.setValue((WeeklySchedule) schedule);
    }

    if (schedule.getType() == Type.Monthly) {
      types.setSelectedIndex(Type.Monthly.ordinal());
      monthlySchedule.setValue((MonthlySchedule) schedule);
    }

    onChange(null);
  }

  @UiHandler("types")
  protected void onChange(ChangeEvent event) {
    if (types.getSelectedIndex() == Type.Daily.ordinal()) {
      dailySchedule.setVisible(true);
    } else {
      dailySchedule.setVisible(false);
    }

    if (types.getSelectedIndex() == Type.Weekly.ordinal()) {
      weeklySchedule.setVisible(true);
    } else {
      weeklySchedule.setVisible(false);
    }

    if (types.getSelectedIndex() == Type.Monthly.ordinal()) {
      monthlySchedule.setVisible(true);
    } else {
      monthlySchedule.setVisible(false);
    }
  }
}
