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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.WeeklySchedule;
import com.google.sampling.experiential.shared.WeeklySchedule.Day;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class WeeklyScheduleView extends Composite implements HasValue<WeeklySchedule> {
  private static WeeklyScheduleUiBinder uiBinder = GWT.create(WeeklyScheduleUiBinder.class);

  interface WeeklyScheduleUiBinder extends UiBinder<Widget, WeeklyScheduleView> {
  }

  @UiField
  TextBox every;
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

  public WeeklyScheduleView() {
    initWidget(uiBinder.createAndBindUi(this));

    every.getElement().setAttribute("type", "number");
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<WeeklySchedule> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public WeeklySchedule getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(WeeklySchedule schedule) {
    setValue(schedule, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(WeeklySchedule schedule, boolean fireEvents) {
    every.setValue(Integer.toString(schedule.getEvery()));
    sunday.setValue(schedule.onDay(Day.Sunday));
    monday.setValue(schedule.onDay(Day.Monday));
    tuesday.setValue(schedule.onDay(Day.Tuesday));
    wednesday.setValue(schedule.onDay(Day.Wednesday));
    thursday.setValue(schedule.onDay(Day.Thursday));
    friday.setValue(schedule.onDay(Day.Friday));
    saturday.setValue(schedule.onDay(Day.Saturday));
  }
}
