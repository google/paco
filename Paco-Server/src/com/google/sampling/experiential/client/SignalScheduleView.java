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
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.SignalSchedule;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SignalScheduleView extends Composite implements HasValue<SignalSchedule> {
  private static SignalScheduleUiBinder uiBinder = GWT.create(SignalScheduleUiBinder.class);

  interface SignalScheduleUiBinder extends UiBinder<Widget, SignalScheduleView> {
  }

  @UiField
  CheckBox editable;
  @UiField
  SignalView signal;
  @UiField
  ScheduleView schedule;

  public SignalScheduleView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<SignalSchedule> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public SignalSchedule getValue() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setEditable(editable.getValue());
    signalSchedule.setSignal(signal.getValue());
    signalSchedule.setSchedule(schedule.getValue());

    return signalSchedule;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(SignalSchedule signalSchedule) {
    setValue(signalSchedule, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(SignalSchedule signalSchedule, boolean fireEvents) {
    editable.setValue(signalSchedule.isEditable());
    signal.setValue(signalSchedule.getSignal());
    schedule.setValue(signalSchedule.getSchedule());
  }
}
