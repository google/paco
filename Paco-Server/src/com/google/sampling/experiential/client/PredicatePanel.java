package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.paco.shared.model.InputDAO;

public class PredicatePanel extends Composite {

  private HorizontalPanel mainPanel;
  private ListBox predicateListBox;
  private TextBox predicateTextBox;

  private String responseType;

  public PredicatePanel(MouseDownHandler precedenceMouseDownHandler, ChangeHandler changeHandler) {
    mainPanel = new HorizontalPanel();
    initWidget(mainPanel);

    predicateListBox = new ListBox();
    predicateListBox.addMouseDownHandler(precedenceMouseDownHandler);
    predicateListBox.addChangeHandler(changeHandler);

    predicateTextBox = new TextBox();
    predicateTextBox.addMouseDownHandler(precedenceMouseDownHandler);
    predicateTextBox.addChangeHandler(changeHandler);

    mainPanel.add(predicateListBox);
  }

  public void configureForInput(InputDAO input) {
    mainPanel.clear();
    predicateListBox.clear();
    responseType = input.getResponseType();
    if (responseType.equals(InputDAO.LIKERT)) {
      for (Integer i = 1; i <= input.getLikertSteps(); ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      predicateListBox.setSelectedIndex(0);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIST)) {
      for (Integer i = 1; i <= input.getListChoices().length; ++i) {
        predicateListBox.addItem(input.getListChoices()[i - 1], i.toString());
      }
      predicateListBox.setSelectedIndex(0);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.LIKERT_SMILEYS)) {
      for (Integer i = 1; i <= 5; ++i) {
        predicateListBox.addItem(i.toString(), i.toString());
      }
      predicateListBox.setSelectedIndex(0);
      mainPanel.add(predicateListBox);
    } else if (responseType.equals(InputDAO.NUMBER)) {
      predicateTextBox.setValue("0", true);
      mainPanel.add(predicateTextBox);
    }
  }

  public void setEnabled(boolean isEnabled) {
    predicateListBox.setEnabled(isEnabled);
    predicateTextBox.setEnabled(isEnabled);
  }

  public String getValue() {
    if (responseType.equals(InputDAO.LIKERT) || responseType.equals(InputDAO.LIST)
        || responseType.equals(InputDAO.LIKERT_SMILEYS)) {
      return predicateListBox.getValue(predicateListBox.getSelectedIndex());
    } else if (responseType.equals(InputDAO.NUMBER)) {
      return predicateTextBox.getValue();
    }
    return "";
  }

}
