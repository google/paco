package com.google.sampling.experiential.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.Output;

public class InputExecutorPanel extends Composite {

  protected InputDAO input;
  
  protected VerticalPanel mainPanel;
  private HorizontalPanel upperLinePanel;
  private HorizontalPanel lowerLinePanel;
  private TextBox text;
  private ListBox list;
  private ArrayList<RadioButton> likerts;
  MyConstants myConstants = GWT.create(MyConstants.class);

  private boolean hasBeenSelected;

  protected boolean firstTime=true;

  private MultiselectList multiselectList;

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
    } else if (input.getResponseType().equals(InputDAO.NUMBER)) {
      value = readNumber();
    }
    return new Output(input.getName(), value);
  }

  private String readNumber() {
    return text.getText();
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
    if (input.getMultiselect() != null && input.getMultiselect()) {
      return readMultiselectList();
    } else {
      if (!hasBeenSelected) {
        return null;
      }
      int chosenIndex = list.getSelectedIndex();
      if (chosenIndex == -1) {
        return null;
      }
      return Integer.toString(chosenIndex);
    }
  }

  private String readMultiselectList() {
    return multiselectList.readSelection();
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
    } else if (input.getResponseType().equals(InputDAO.NUMBER)) {
      renderOpenText();
    }
    mainPanel.add(new HTML("<br/>"));
  }

  private void renderLikert() {
    likerts = new ArrayList<RadioButton>();
    String groupName = "likert_choices_"+Long.toString(System.currentTimeMillis());
    if (input.getLikertSteps() == null) {
      // backward compatibility for a short while
      input.setLikertSteps(InputDAO.DEFAULT_LIKERT_STEPS);
    }
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
    boolean isMultipleSelect = input.getMultiselect() != null ? input.getMultiselect() : false;
    if (isMultipleSelect) {
      multiselectList = new MultiselectList(input);
      lowerLinePanel.add(multiselectList);
    } else {
      list = new ListBox(false);
      list.addItem(myConstants.defaultListItem());      // "No selection" list item.
      for (String choice : input.getListChoices()) {
        list.addItem(choice);
      }
      lowerLinePanel.add(list);    
      list.addChangeHandler(new ChangeHandler() {
        public void onChange(ChangeEvent changeEvent) {
          hasBeenSelected = true;
        } 
      });
    }
  }

  private void renderOpenText() {
    VerticalPanel holder = new VerticalPanel();
    lowerLinePanel.add(holder);
    text = new TextBox();
    text.setWidth("40em");
//    text.setCharacterWidth(80);
//    text.setVisibleLines(6);

    text.setMaxLength(500);
    holder.add(text);
        
    Label fivehundredlimit = new Label("(" + myConstants.fiveHundredCharLimit() + ")");
    fivehundredlimit.setStyleName("paco-small");
    holder.add(fivehundredlimit);
  }

  private void createTextPrompt() {
    Label label = new Label(input.getText());
    label.setStyleName("keyLabel");
    upperLinePanel.add(label);    
  }

}