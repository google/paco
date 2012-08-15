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
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.FixedSignal;
import com.google.sampling.experiential.shared.RandomSignal;
import com.google.sampling.experiential.shared.Signal;
import com.google.sampling.experiential.shared.Signal.Type;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SignalView extends Composite implements HasValue<Signal> {
  private static SignalUiBinder uiBinder = GWT.create(SignalUiBinder.class);

  interface SignalUiBinder extends UiBinder<Widget, SignalView> {
  }

  @UiField
  ListBox types;
  @UiField
  FixedSignalView fixedSignal;
  @UiField
  RandomSignalView randomSignal;

  public SignalView() {
    initWidget(uiBinder.createAndBindUi(this));

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
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Signal> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public Signal getValue() {
    if (types.getSelectedIndex() == Type.Fixed.ordinal()) {
      return fixedSignal.getValue();
    }

    if (types.getSelectedIndex() == Type.Random.ordinal()) {
      return randomSignal.getValue();
    }

    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Signal signal) {
    setValue(signal, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(Signal signal, boolean fireEvents) {
    if (signal.getType() == Type.Fixed) {
      types.setSelectedIndex(Type.Fixed.ordinal());
      fixedSignal.setValue((FixedSignal) signal);
    }

    if (signal.getType() == Type.Random) {
      types.setSelectedIndex(Type.Random.ordinal());
      randomSignal.setValue((RandomSignal) signal);
    }

    onChange(null);
  }

  @UiHandler("types")
  protected void onChange(ChangeEvent event) {
    if (types.getSelectedIndex() == Type.Fixed.ordinal()) {
      fixedSignal.setVisible(true);
    } else {
      fixedSignal.setVisible(false);
    }

    if (types.getSelectedIndex() == Type.Random.ordinal()) {
      randomSignal.setVisible(true);
    } else {
      randomSignal.setVisible(false);
    }
  }
}
