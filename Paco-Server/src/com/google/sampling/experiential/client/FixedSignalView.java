// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.FixedSignal;

import java.util.Date;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class FixedSignalView extends Composite implements HasValue<FixedSignal> {
  private static FixedSignalUiBinder uiBinder = GWT.create(FixedSignalUiBinder.class);

  interface FixedSignalUiBinder extends UiBinder<Widget, FixedSignalView> {
  }

  @UiField
  FlowPanel times;

  public FixedSignalView() {
    initWidget(uiBinder.createAndBindUi(this));

    times.add(new RemovableTextBox<Date>(times));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<FixedSignal> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public FixedSignal getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(FixedSignal signal) {
    setValue(signal, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(FixedSignal signal, boolean fireEvents) {
    times.clear();

    for (Date time : signal.getTimes()) {
      times.add(new RemovableTextBox<Date>(times, time));
    }
  }
}
