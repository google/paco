// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.DailySchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DailyScheduleView extends Composite implements HasValue<DailySchedule> {
  private static DailyScheduleUiBinder uiBinder = GWT.create(DailyScheduleUiBinder.class);

  interface DailyScheduleUiBinder extends UiBinder<Widget, DailyScheduleView> {
  }

  @UiField
  TextBox every;

  public DailyScheduleView() {
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
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DailySchedule> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public DailySchedule getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(DailySchedule schedule) {
    setValue(schedule, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(DailySchedule schedule, boolean fireEvents) {
    every.setValue(Integer.toString(schedule.getEvery()));
  }
}
