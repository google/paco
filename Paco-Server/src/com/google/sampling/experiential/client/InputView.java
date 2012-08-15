// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.Input.Type;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.TextInput;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class InputView extends Composite implements HasValue<Input> {
  private static InputUiBinder uiBinder = GWT.create(InputUiBinder.class);

  interface InputUiBinder extends UiBinder<Widget, InputView> {
  }

  private FlowPanel parent;

  @UiField
  ListBox types;
  @UiField
  TextBox name;
  @UiField
  CheckBox required;
  @UiField
  TextBox conditionalExpression;
  @UiField
  Button add;
  @UiField
  Button remove;
  @UiField
  TextInputView textInput;
  @UiField
  LikertInputView likertInput;
  @UiField
  ListInputView listInput;


  public InputView() {
    initWidget(uiBinder.createAndBindUi(this));

    for (Type type : Type.values()) {
      types.insertItem(type.name(), type.ordinal());
    }

    onChange(null);
  }

  public InputView(FlowPanel parent) {
    this();

    this.parent = parent;
  }

  public InputView(FlowPanel parent, Input input) {
    this(parent);

    setValue(input);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google
   * .gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Input> handler) {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public Input getValue() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Input input) {
    setValue(input, false);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(Input input, boolean fireEvents) {
    if (input.getType() == Type.Text) {
      types.setSelectedIndex(Type.Text.ordinal());
      textInput.setValue((TextInput) input);
    }

    if (input.getType() == Type.Likert) {
      types.setSelectedIndex(Type.Likert.ordinal());
      likertInput.setValue((LikertInput) input);
    }

    if (input.getType() == Type.List) {
      types.setSelectedIndex(Type.List.ordinal());
      listInput.setValue((ListInput) input);
    }

    name.setValue(input.getName());
    required.setValue(input.isRequired());
    conditionalExpression.setValue(input.getConditionalExpression());

    onChange(null);
  }

  @UiHandler("add")
  protected void onAdd(ClickEvent event) {
    int beforeIndex = parent.getWidgetIndex(this) + 1;
    InputView input = new InputView(parent);
    parent.insert(input, beforeIndex);
  }

  @UiHandler("remove")
  protected void onRemove(ClickEvent event) {
    if (parent.getWidgetCount() == 1) {
      // FIXME: What should we do here?
    } else {
      parent.remove(this);
    }
  }

  @UiHandler("types")
  protected void onChange(ChangeEvent event) {
    if (types.getSelectedIndex() == Type.Text.ordinal()) {
      textInput.setVisible(true);
    } else {
      textInput.setVisible(false);
    }

    if (types.getSelectedIndex() == Type.Likert.ordinal()) {
      likertInput.setVisible(true);
    } else {
      likertInput.setVisible(false);
    }

    if (types.getSelectedIndex() == Type.List.ordinal()) {
      listInput.setVisible(true);
    } else {
      listInput.setVisible(false);
    }
  }
}
