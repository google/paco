// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.MonthlySchedule;
import com.google.sampling.experiential.shared.MonthlySchedule.Week;
import com.google.sampling.experiential.shared.WeeklySchedule.Day;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class MonthlyScheduleView extends Composite implements HasValue<MonthlySchedule> {
  private static MonthlyScheduleUiBinder uiBinder = GWT.create(MonthlyScheduleUiBinder.class);

  interface MonthlyScheduleUiBinder extends UiBinder<Widget, MonthlyScheduleView> {
  }

  @UiField
  TextBox every;

  @UiField
  RadioButton byDay;
  @UiField
  ListBox days;

  @UiField
  RadioButton byWeek;
  @UiField
  ListBox weeks;
  @UiField
  CheckBox sunday;
  @UiField
  CheckBox monday;
  @UiField
  CheckBox tuesday;
  @UiField
  CheckBox wednesday;
  @UiField
  CheckBox thursday;
  @UiField
  CheckBox friday;
  @UiField
  CheckBox saturday;

  public MonthlyScheduleView() {
    initWidget(uiBinder.createAndBindUi(this));

    every.getElement().setAttribute("type", "number");

    for (int i = 1; i < 32; i++) {
      days.addItem(Integer.toString(i));
    }

    for (Week week : Week.values()) {
      weeks.insertItem(week.name(), week.ordinal());
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MonthlySchedule> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public MonthlySchedule getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(MonthlySchedule schedule) {
    setValue(schedule, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(MonthlySchedule schedule, boolean fireEvents) {
    every.setValue(Integer.toString(schedule.getEvery()));
    byDay.setValue(schedule.isByDay());

    if (schedule.isByDay()) {
      days.setSelectedIndex(schedule.getDayRepeat());
    } else {
      weeks.setSelectedIndex(schedule.getWeekRepeat());
      sunday.setValue(schedule.onDay(Day.Sunday));
      monday.setValue(schedule.onDay(Day.Monday));
      tuesday.setValue(schedule.onDay(Day.Tuesday));
      wednesday.setValue(schedule.onDay(Day.Wednesday));
      thursday.setValue(schedule.onDay(Day.Thursday));
      friday.setValue(schedule.onDay(Day.Friday));
      saturday.setValue(schedule.onDay(Day.Saturday));
    }
  }
}
