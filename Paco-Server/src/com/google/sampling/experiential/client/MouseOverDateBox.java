/**
 * @author Priya Donti
 * A limited wrapper class for a DatePicker, with added mouse-over functionality. 
 * Code adapted from: https://groups.google.com/forum/#!topic/google-web-toolkit/-I3l577JRB8
 *
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
  
  private FlowPanel mainPanel;
  private DateBox dateBox;
  private HTML mouseOverText;
  private boolean shouldShowMouseOver;

  public MouseOverDateBox() {
    FocusPanel container = new FocusPanel();
    container.addMouseOverHandler(this);
    container.addMouseOutHandler(this);
    
    mainPanel = new FlowPanel();
    container.add(mainPanel);
    
    dateBox = new DateBox();
    dateBox.addHandler(this, FocusEvent.getType());
    mainPanel.add(dateBox);
    
    mouseOverText = new HTML();
    mainPanel.add(mouseOverText);
//    mouseOverText.setStyleName("ctb-control");
    mouseOverText.setVisible(false);
    initWidget(container);
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
      mouseOverText.setVisible(false);
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
    mouseOverText.setText(message);
  }
  
  public String getMessage() {
    return mouseOverText.getText();
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
    if (shouldShowMouseOver) {
      mouseOverText.setVisible(true);
    }
  }

  @Override
  public void onMouseOut(MouseOutEvent event) {
    if (shouldShowMouseOver) {
      mouseOverText.setVisible(false);
    }
  }

  @Override
  public void onFocus(FocusEvent event) {
    mouseOverText.setVisible(false);
  }

}
