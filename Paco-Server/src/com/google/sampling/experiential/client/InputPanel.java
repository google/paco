// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.Input.Type;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.TextInput;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class InputPanel extends VerticalPanel implements ChangeHandler {
  private ListBox typeListBox;
  private TextBox nameTextBox;
  private CheckBox requiredCheckBox;
  private TextBox conditionalExpressionTextBox;
  private TextInputPanel textInputPanel;
  private LikertInputPanel likertInputPanel;
  private ListInputPanel listInputPanel;

  /**
   *
   */
  public InputPanel() {
    super();

    addInputPanel();
    addTextInputPanel();
    addLikertInputPanel();
    addListInputPanel();
  }

  /**
   * @return the input
   */
  public Input getInput() {
    Input input = null;

    switch (getType()) {
    case Text:
      input = textInputPanel.getInput();
      break;
    case Likert:
      input = likertInputPanel.getInput();
      break;
    case List:
      input = listInputPanel.getInput();
      break;
    }

    retrieveInputPanel(input);

    return input;
  }

  /**
   * @param input the input
   */
  public void setInput(Input input) {
    updateInputPanel(input);
    updateTextInputPanel(input);
    updateLikertInputPanel(input);
    updateListInputPanel(input);
  }

  public void addInputPanel() {
    Panel panel = new HorizontalPanel();

    panel.add(typeListBox);
    panel.add(new Label("Name:"));
    panel.add(nameTextBox);
    panel.add(new Label("Required:"));
    panel.add(requiredCheckBox);
    panel.add(new Label("Conditional Expression:"));
    panel.add(conditionalExpressionTextBox);

    add(panel);
  }

  private void updateInputPanel(Input input) {
    nameTextBox.setText(input.getName());
    requiredCheckBox.setValue(input.isRequired());
    conditionalExpressionTextBox.setText(input.getConditionalExpression());
  }

  private void retrieveInputPanel(Input input) {
    input.setName(nameTextBox.getText());
    input.setRequired(requiredCheckBox.getValue());
    input.setConditionalExpression(conditionalExpressionTextBox.getText());
  }

  private void addTextInputPanel() {
    textInputPanel = new TextInputPanel();

    add(textInputPanel);
  }

  private void updateTextInputPanel(Input input) {
    if (input.getType().equals(Type.Text) == false) {
      return;
    }

    textInputPanel.setInput((TextInput) input);
  }

  private void addLikertInputPanel() {
    likertInputPanel = new LikertInputPanel();

    add(likertInputPanel);
  }

  private void updateLikertInputPanel(Input input) {
    if (input.getType().equals(Type.Likert) == false) {
      return;
    }

    likertInputPanel.setInput((LikertInput) input);
  }

  private void addListInputPanel() {
    listInputPanel = new ListInputPanel();

    add(listInputPanel);
  }

  private void updateListInputPanel(Input input) {
    if (input.getType().equals(Type.List) == false) {
      return;
    }

    listInputPanel.setInput((ListInput) input);
  }

  private Type getType() {
    return Type.valueOf(typeListBox.getItemText(typeListBox.getSelectedIndex()));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event
   * .dom.client.ChangeEvent)
   */
  @Override
  public void onChange(ChangeEvent event) {
    if (event.getSource() == typeListBox) {
      Type type = getType();

      if (type == Type.Text) {
        textInputPanel.setVisible(true);
      } else {
        textInputPanel.setVisible(false);
      }

      if (type == Type.Likert) {
        likertInputPanel.setVisible(true);
      } else {
        likertInputPanel.setVisible(false);
      }

      if (type == Type.List) {
        listInputPanel.setVisible(true);
      } else {
        listInputPanel.setVisible(false);
      }
    }
  }
}
