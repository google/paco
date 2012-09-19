package com.google.sampling.experiential.client;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.Output;

public class InputExecutorPanel extends Composite {

  protected InputDAO input;
  
  protected VerticalPanel mainPanel;
  private HorizontalPanel upperLinePanel;
  private HorizontalPanel lowerLinePanel;
  private TextBox text;
  private ListBox list;
  private ArrayList<RadioButton> likerts;

  public InputExecutorPanel(InputDAO input) {
    super();
    this.input = input;
    createLayout();
  }

  public InputDAO getInput() {
    return input;
  }

  public Output getValue() {
    String value = "";
    if (input.getResponseType().equals(InputDAO.OPEN_TEXT)) {
      value = readOpenText();
    } else if (input.getResponseType().equals(InputDAO.LIST)) {
      value = readList();
    } else if (input.getResponseType().equals(InputDAO.LIKERT)) {
      value = readLikert();
    }
    return new Output(input.getName(), value);
  }

  protected String readLikert() {
    for (int i=0; i < likerts.size(); i++) {
      RadioButton radio = likerts.get(i);
      if (radio.getValue() == true) {
        return Integer.toString(i + 1);
      }
    }
    return null;
  }

  protected String readList() {
    int chosenIndex = list.getSelectedIndex();
    if (chosenIndex == -1) {
      return null;
    }
    return Integer.toString(chosenIndex + 1);
  }

  protected String readOpenText() {
    return text.getText();    
  }

  protected void createLayout() {
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
    
    upperLinePanel = new HorizontalPanel();
    upperLinePanel.setStyleName("left");
    mainPanel.add(upperLinePanel);
  
    lowerLinePanel = new HorizontalPanel();
    mainPanel.add(lowerLinePanel);
  
    createTextPrompt();
    renderInputItem();
  }

  private void renderInputItem() {
    if (input.getResponseType().equals(InputDAO.OPEN_TEXT)) {
      renderOpenText();
    } else if (input.getResponseType().equals(InputDAO.LIST)) {
      renderList();
    } else if (input.getResponseType().equals(InputDAO.LIKERT)) {
      renderLikert();
    }
    
  }

  private void renderLikert() {
    likerts = new ArrayList<RadioButton>();
    String groupName = "likert_choices_"+Long.toString(System.currentTimeMillis());
    for (int i = 0; i < input.getLikertSteps(); i++) {
      String name = "";
      if (i == input.getLikertSteps() - 1 && input.getRightSideLabel() != null) {
        name = input.getRightSideLabel();
      } else if (i == 0 && input.getLeftSideLabel() != null) {
        Label leftLabel = new Label(input.getLeftSideLabel());
        leftLabel.setStyleName("keyLabel");
        lowerLinePanel.add(leftLabel);
      }
      
      RadioButton radio = new RadioButton(groupName, "");     
      likerts.add(radio);
      lowerLinePanel.add(radio);
      
      if (i == input.getLikertSteps() - 1 && input.getRightSideLabel() != null) {
        Label rightLabel = new Label(input.getRightSideLabel());
        rightLabel.setStyleName("keyLabel");
        lowerLinePanel.add(rightLabel);
      }
      
    }
  }

  private void renderList() {
    list = new ListBox(input.getMultiselect() != null ? input.getMultiselect() : false);
    for (String choice : input.getListChoices()) {
      list.addItem(choice);
    }
    lowerLinePanel.add(list);    
  }

  private void renderOpenText() {
    text = new TextBox();
    lowerLinePanel.add(text);    
  }

  private void createTextPrompt() {
    Label label = new Label(input.getText());
    label.setStyleName("keyLabel");
    upperLinePanel.add(label);    
  }

}