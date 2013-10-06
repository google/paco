package com.google.sampling.experiential.client;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.InputDAO;

public class MultiselectList extends Composite {

  private InputDAO input;
  private VerticalPanel mainPanel;
  List<CheckBox> checkboxes; 
  
  public MultiselectList(InputDAO input) {
    super();
    this.input = input;
    mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    createLayout();
  }

  private void createLayout() {
    checkboxes = Lists.newArrayList();
    for (String choice : input.getListChoices()) {
      CheckBox option = new CheckBox(choice);
      checkboxes.add(option);
      mainPanel.add(option);
    }
  }
  
  public String readSelection() {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < checkboxes.size(); i++) {
      CheckBox option = checkboxes.get(i);
      if (option.getValue()) {
        if (first) {
          first = false;
        } else {
          buf.append(",");
        }
        buf.append(Integer.toString(i + 1));        
      }
    }
    return buf.toString();
  }

}
