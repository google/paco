package com.google.sampling.experiential.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.sampling.experiential.shared.TextInput;

public class TextInputPanel extends HorizontalPanel {
  private TextBox questionTextBox;
  private CheckBox multilineCheckBox;

  /**
   *
   */
  public TextInputPanel() {
    super();

    questionTextBox = new TextBox();
    multilineCheckBox = new CheckBox();

    add(new Label("Question:"));
    add(questionTextBox);
    add(new Label("Multiline:"));
    add(multilineCheckBox);
  }

  /**
   * @param input the text input
   */
  public void setInput(TextInput input) {
    questionTextBox.setText(input.getQuestion());
    multilineCheckBox.setValue(input.isMultiline());
  }

  /**
   * @return the text input
   */
  public TextInput getInput() {
    TextInput input = new TextInput();

    input.setQuestion(questionTextBox.getText());
    input.setMultiline(multilineCheckBox.getValue());

    return input;
  }
}
