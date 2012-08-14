// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class TextBoxPanel extends Composite {
  private HorizontalPanel mainPanel;
  private TextBox textBox;
  private Button removeButton;

  /**
   *
   */
  public TextBoxPanel() {
    mainPanel = new HorizontalPanel();

    initWidget(mainPanel);

    textBox = new TextBox();
    removeButton = new Button("-");

    mainPanel.add(textBox);
    mainPanel.add(removeButton);
  }

  /**
   * @return the text
   */
  public String getText() {
    return textBox.getText();
  }

  /**
   * @param text the text
   */
  public void setText(String text) {
    textBox.setText(text);
  }

  /**
   * @param handler a click handler
   */
  public void addClickHandler(ClickHandler handler) {
    removeButton.addClickHandler(handler);
  }
}
