package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.LikertInput;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class LikertInputPanel extends VerticalPanel implements ClickHandler {
  private TextBox questionTextBox;
  private CheckBox smileysCheckBox;
  private VerticalPanel labelPanels;
  private Button addButton;

  /**
   *
   */
  public LikertInputPanel() {
    super();

    questionTextBox = new TextBox();
    smileysCheckBox = new CheckBox();
    labelPanels = new VerticalPanel();

    Panel panel = new HorizontalPanel();

    panel.add(questionTextBox);
    panel.add(smileysCheckBox);

    add(panel);
    add(labelPanels);
  }

  /**
   * @param input the likert input
   */
  public void setInput(LikertInput input) {
    questionTextBox.setText(input.getQuestion());
    smileysCheckBox.setValue(input.isSmileys());

    for (String label : input.getLabels()) {
      TextBoxPanel labelPanel = new TextBoxPanel();
      labelPanel.setText(label);
      labelPanel.addClickHandler(this);
      labelPanels.add(labelPanel);
    }
  }

  /**
   * @return the likert input
   */
  public LikertInput getInput() {
    LikertInput input = new LikertInput();

    input.setQuestion(questionTextBox.getText());
    input.setSmileys(smileysCheckBox.getValue());

    for (int i = 0; i < labelPanels.getWidgetCount(); i++) {
      input.addLabel(((TextBoxPanel) labelPanels.getWidget(i)).getText());
    }

    return input;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
   */
  @Override
  public void onClick(ClickEvent event) {
    if (event.getSource() instanceof TextBoxPanel) {
      labelPanels.remove((TextBoxPanel) event.getSource());
    } else if (event.getSource() == addButton) {
      TextBoxPanel labelPanel = new TextBoxPanel();
      labelPanel.addClickHandler(this);
      labelPanels.add(labelPanel);
    }
  }
}
