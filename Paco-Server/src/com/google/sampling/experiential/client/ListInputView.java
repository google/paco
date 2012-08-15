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

import com.google.sampling.experiential.shared.ListInput;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ListInputView extends Composite implements HasValue<ListInput> {
  private static ListInputUiBinder uiBinder = GWT.create(ListInputUiBinder.class);

  interface ListInputUiBinder extends UiBinder<Widget, ListInputView> {
  }

  @UiField
  TextBox question;
  @UiField
  CheckBox multiselect;
  @UiField
  FlowPanel choices;

  public ListInputView() {
    initWidget(uiBinder.createAndBindUi(this));

    choices.add(new RemovableTextBox<String>(choices));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ListInput> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public ListInput getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(ListInput input) {
    setValue(input, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(ListInput input, boolean fireEvents) {
    question.setValue(input.getQuestion());
    multiselect.setValue(input.isMultiselect());

    for (String choice : input.getChoices()) {
      choices.add(new RemovableTextBox<String>(choices, choice));
    }
  }
}
