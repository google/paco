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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * View for configuring experiment's question types:
 * Fixed question sets are usually not updated.
 * QotD questions sets are targeted at specific dates.
 * 
 * @author Bob Evans
 *
 */
public class ContentTypeView extends Composite implements BooleanValueHolder {

  HorizontalPanel mainPanel;
  private boolean questionsChange;
  private RadioButton radio1;
  private RadioButton radio2;

  ContentTypeView(boolean questionsChange) {
    super();
    mainPanel = new HorizontalPanel();
    this.questionsChange = questionsChange;
    initWidget(mainPanel);
    init();
  }

  /**
   * 
   */
  private void init() {
    VerticalPanel outer = new VerticalPanel();
    HorizontalPanel line = new HorizontalPanel();
    line.setStyleName("left");
    Label keyLabel = new Label("Content Type:");
    keyLabel.setStyleName("keyLabel");
    outer.add(keyLabel);  
    radio1 = new RadioButton("contentType", "Fixed Question Set");
    radio2 = new RadioButton("contentType", "Questions are asked on specific dates (QotD)");
    radio1.setChecked(!questionsChange);
    radio2.setChecked(questionsChange);
    
    line.add(radio1);
    line.add(radio2);
    outer.add(line);
    mainPanel.add(outer);
  }

  public boolean getValue() {
    return radio2.isChecked();
  }
  
}
