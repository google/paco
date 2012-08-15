// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class RemovableTextBox<T> extends Composite implements HasValue<T> {
  private static RemovableTextBoxUiBinder uiBinder = GWT.create(RemovableTextBoxUiBinder.class);

  interface RemovableTextBoxUiBinder extends UiBinder<Widget, RemovableTextBox<?>> {
  }

  private FlowPanel parent;

  @UiField
  TextBox textBox;
  @UiField
  Button add;
  @UiField
  Button remove;

  public RemovableTextBox() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public RemovableTextBox(FlowPanel parent) {
    this();

    this.parent = parent;
  }

  public RemovableTextBox(FlowPanel parent, T value) {
    this();

    this.parent = parent;
    setValue(value);
  }

  @UiHandler("add")
  protected void onAdd(ClickEvent event) {
    int beforeIndex = parent.getWidgetIndex(this) + 1;
    RemovableTextBox<T> removableTextBox = new RemovableTextBox<T>(parent);
    parent.insert(removableTextBox, beforeIndex);
  }

  @UiHandler("remove")
  protected void onRemove(ClickEvent event) {
    if (parent.getWidgetCount() == 1) {
      textBox.setText("");
    } else {
      parent.remove(this);
    }
  }

  /* (non-Javadoc)
   * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    // TODO(corycornelius): Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public T getValue() {
    // TODO(corycornelius): Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(T value) {
    // TODO(corycornelius): Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
   */
  @Override
  public void setValue(T value, boolean fireEvents) {
    // TODO(corycornelius): Auto-generated method stub
    
  }
}
