/**
 * A limited wrapper class for a DatePicker, with added mouse-over functionality. 
 * Code adapted from: https://groups.google.com/forum/#!topic/google-web-toolkit/-I3l577JRB8
 */
package com.google.sampling.experiential.client;

import java.util.Date;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.Format;

public class MouseOverDateBox extends Composite implements MouseOutHandler, MouseOverHandler, FocusHandler {
  
  private DateBox dateBox;
  private String message;
  private boolean mouseOverIsEnabled;

  public MouseOverDateBox(String message) {
    this.message = message;
    
    FocusPanel container = new FocusPanel();
    container.addMouseOverHandler(this);
    container.addMouseOutHandler(this);
    initWidget(container);
    
    dateBox = new DateBox();
    dateBox.addHandler(this, FocusEvent.getType());
    container.add(dateBox);
  }

  public void enableMouseOver() {
    setShouldShowMouseOver(true);
  }
  
  public void disableMouseOver() {
    setShouldShowMouseOver(false);
  }
  
  private void setShouldShowMouseOver(boolean mouseOverIsEnabled) {
    this.mouseOverIsEnabled = mouseOverIsEnabled;
    if (!mouseOverIsEnabled) {
      disableMouseOverText();
    }
  }
  
  public void setValue(Date value) {
    setValue(value, false);
  }

  public void setValue(Date value, boolean fireEvents) {
    dateBox.setValue(value, fireEvents);
  }

  public Date getValue() {
    return dateBox.getValue();
  }  
  
  public void setFormat(Format format) {
    dateBox.setFormat(format);
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public String getMessage() {
    return message;
  }
  
  public void addStyleName(String style) {
    dateBox.addStyleName(style);
  }
  
  public void removeStyleName(String style) {
    dateBox.removeStyleName(style);
  }

  public void addValueChangeHandler(ValueChangeHandler<Date> handler) {
    dateBox.addValueChangeHandler(handler);
  }
  
  @Override
  public void onMouseOver(MouseOverEvent event) {
    if (mouseOverIsEnabled) {
      enableMouseOverText();
    }
  }

  @Override
  public void onMouseOut(MouseOutEvent event) {
    if (mouseOverIsEnabled) {
      disableMouseOverText();
    }
  }

  @Override
  public void onFocus(FocusEvent event) {
    disableMouseOverText();
  }
  
  private void enableMouseOverText() {
    dateBox.setTitle(message);
  }
  
  private void disableMouseOverText() {
    dateBox.setTitle("");
  }

}
