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

import com.google.sampling.experiential.shared.RandomSignal;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class RandomSignalView extends Composite implements HasValue<RandomSignal> {
  private static RandomSignalUiBinder uiBinder = GWT.create(RandomSignalUiBinder.class);

  interface RandomSignalUiBinder extends UiBinder<Widget, RandomSignalView> {
  }

  @UiField
  TextBox frequency;
  @UiField
  TextBox startTime;
  @UiField
  TextBox endTime;

  public RandomSignalView() {
    initWidget(uiBinder.createAndBindUi(this));

    frequency.getElement().setAttribute("type", "number");
    startTime.getElement().setAttribute("type", "time");
    endTime.getElement().setAttribute("type", "time");
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RandomSignal> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public RandomSignal getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(RandomSignal signal) {
    setValue(signal, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(RandomSignal signal, boolean fireEvents) {
    frequency.setText(Integer.toString(signal.getFrequency()));
    startTime.setText(signal.getStartTime().toString());
    endTime.setText(signal.getEndTime().toString());
  }
}
