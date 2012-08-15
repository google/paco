// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.LikertInput;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class LikertInputView extends Composite implements HasValue<LikertInput> {

  private static LikertInputUiBinder uiBinder = GWT.create(LikertInputUiBinder.class);

  interface LikertInputUiBinder extends UiBinder<Widget, LikertInputView> {
  }

  @UiField
  TextBox question;
  @UiField
  CheckBox smileys;
  @UiField
  FlowPanel labels;

  public LikertInputView() {
    initWidget(uiBinder.createAndBindUi(this));

    labels.add(new RemovableTextBox<String>(labels));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<LikertInput> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public LikertInput getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(LikertInput input) {
    setValue(input, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(LikertInput input, boolean fireEvents) {
    question.setValue(input.getQuestion());
    smileys.setValue(input.isSmileys());

    for (String label : input.getLabels()) {
      labels.add(new RemovableTextBox<String>(labels, label));
    }
  }
}
