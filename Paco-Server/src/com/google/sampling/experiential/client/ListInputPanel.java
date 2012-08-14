package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.ListInput;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ListInputPanel extends HorizontalPanel implements ClickHandler {
  private TextBox questionTextBox;
  private CheckBox multiselectCheckBox;
  private VerticalPanel choicePanels;
  private Button addButton;

  /**
   *
   */
  public ListInputPanel() {
    super();

    questionTextBox = new TextBox();
    multiselectCheckBox = new CheckBox();
    choicePanels = new VerticalPanel();
    addButton = new Button("+");
    addButton.addClickHandler(this);

    Panel panel = new HorizontalPanel();

    panel.add(new Label("Question:"));
    panel.add(questionTextBox);
    panel.add(new Label("Multiselect:"));
    panel.add(multiselectCheckBox);

    add(panel);
    add(choicePanels);
  }

  /**
   * @param input the list input
   */
  public void setInput(ListInput input) {
    questionTextBox.setText(input.getQuestion());
    multiselectCheckBox.setValue(input.isMultiselect());

    for (String choice : input.getChoices()) {
      TextBoxPanel choicePanel = new TextBoxPanel();
      choicePanel.setText(choice);
      choicePanel.addClickHandler(this);
      choicePanels.add(choicePanel);
    }
  }

  /**
   * @return the list input
   */
  public ListInput getInput() {
    ListInput input = new ListInput();

    input.setQuestion(questionTextBox.getText());
    input.setMultiselect(multiselectCheckBox.getValue());

    for (int i = 0; i < choicePanels.getWidgetCount(); i++) {
      input.addChoice(((TextBoxPanel) choicePanels.getWidget(i)).getText());
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
      choicePanels.remove((TextBoxPanel) event.getSource());
    } else if (event.getSource() == addButton) {
      TextBoxPanel choicePanel = new TextBoxPanel();
      choicePanel.addClickHandler(this);
      choicePanels.add(choicePanel);
    }
  }
}
