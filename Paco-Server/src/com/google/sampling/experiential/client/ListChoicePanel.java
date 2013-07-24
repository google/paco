/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Panel to show one individual list choice item. This is part of the 
 * definition of an input whose responsetype is list.
 * 
 * @author Bob Evans
 *
 */
public class ListChoicePanel extends Composite {

  private ListChoicesPanel parent;
  private HorizontalPanel horizontalPanel;
  private TextBox textField;

  /**
   * @param listChoicesPanel
   */
  public ListChoicePanel(ListChoicesPanel listChoicesPanel) {
    this.parent = listChoicesPanel;
    horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(2);
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(horizontalPanel);
    horizontalPanel.setWidth("258px");

    Label lblTime = new Label("Choice: ");
    lblTime.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblTime);
    lblTime.setWidth("45px");

    textField = new TextBox();
    horizontalPanel.add(textField);
    textField.addValueChangeHandler(new ValueChangeHandler() {
      public void onValueChange(ValueChangeEvent event) {
        updateChoice();
      }
    });

    Button btnDelete = new Button("-");
    btnDelete.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        deleteThis();
      }

    });
    horizontalPanel.add(btnDelete);

    Button btnAdd = new Button("+");
    horizontalPanel.add(btnAdd);

    btnAdd.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addChoicePanel();
      }

    });
  }

  protected void updateChoice() {
    parent.updateChoice(this);
  }

  protected void addChoicePanel() {
    parent.addChoice(this);
  }

  private void deleteThis() {
    parent.deleteChoice(this);
  }

  /**
   * @return
   */
  public String getChoice() {
    return textField.getText();
  }

  public void setChoice(String choice) {
    textField.setText(choice);
  }
  
  public TextBox getTextField() {
    return textField;
  }



}
