
/**
 * A limited wrapper class for a TextBoxBase (specifically TextArea or TextBox), 
 * with added mouse-over functionality. 
 * Code adapted from: https://groups.google.com/forum/#!topic/google-web-toolkit/-I3l577JRB8
 */
package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;

public class MouseOverTextBoxBase extends Composite implements MouseOverHandler, MouseOutHandler, FocusHandler {
  
  public static final int TEXT_AREA = 0;
  public static final int TEXT_BOX = 1;
  
  private String message;
  private boolean shouldShowMouseOver;
  
  // Visible for testing
  protected TextBoxBase textBox;
  
  public MouseOverTextBoxBase(int type, String message) {
    this.message = message;
    
    FocusPanel container = new FocusPanel();
    container.addMouseOverHandler(this);
    container.addMouseOutHandler(this);
    initWidget(container);
    
    if (type == TEXT_AREA) {
      textBox = new TextArea();
    } else {
      textBox = new TextBox();
    }
    textBox.addFocusHandler(this);
    container.add(textBox);
  }

  public void enableMouseOver() {
    setShouldShowMouseOver(true);
  }
  
  public void disableMouseOver() {
    setShouldShowMouseOver(false);
  }
  
  private void setShouldShowMouseOver(boolean shouldShowMouseOver) {
    this.shouldShowMouseOver = shouldShowMouseOver;
    if (!shouldShowMouseOver) {
      disableMouseOverText();
    }
  }

  public void setText(String text) {
    textBox.setText(text);
  }

  public String getText() {
    return textBox.getText();
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    textBox.setValue(value, fireEvents);
  }

  public String getValue() {
    return textBox.getValue();
  }

  public void setEnabled(boolean isEnabled) {
    textBox.setEnabled(isEnabled);
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public String getMessage() {
    return message;
  }
  
  public void addStyleName(String style) {
    textBox.addStyleName(style);
  }
  
  public void removeStyleName(String style) {
    textBox.removeStyleName(style);
  }
  
  /**
   * Note: this affects only objects initialized with type TEXT_AREA.
   */
  public void setCharacterWidth(int width) {
    if (textBox instanceof TextArea) {
      ((TextArea) textBox).setCharacterWidth(width);
    }
  }
  
  public void setHeight(String height) {
    textBox.setHeight(height);
  }
  
  public void setWidth(String width) {
    textBox.setWidth(width);
  }
  
  /**
   * Note: this affects only objects initialized with type TEXT_BOX.
   */
  public void setMaxLength(int length) {
    if (textBox instanceof TextBox) {
      ((TextBox) textBox).setMaxLength(length);
    }
  }
  
  /**
   * Note: this affects only objects initialized with type TEXT_BOX.
   */
  public void setVisibleLength(int length) {
    if (textBox instanceof TextBox) {
      ((TextBox) textBox).setVisibleLength(length);
    }
  }
  
  public void addValueChangeHandler(ValueChangeHandler<String> handler) {
    textBox.addValueChangeHandler(handler);
  }
  
  public void addMouseDownHandler(MouseDownHandler handler) {
    textBox.addMouseDownHandler(handler);
  }
  
  public void addChangeHandler(ChangeHandler handler) {
    textBox.addChangeHandler(handler);
  }
  
  @Override
  public void onMouseOver(MouseOverEvent event) {
    if (shouldShowMouseOver) {
      enableMouseOverText();
    }
  }

  @Override
  public void onMouseOut(MouseOutEvent event) {
    if (shouldShowMouseOver) {
      disableMouseOverText();
    }
  }
  
  @Override
  public void onFocus(FocusEvent event) {
    disableMouseOverText();
  }
  
  private void enableMouseOverText() {
    textBox.setTitle(message);
  }
  
  private void disableMouseOverText() {
    textBox.setTitle("");
  }
}
