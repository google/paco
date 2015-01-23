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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.paco.shared.model.ExperimentDAO;

/**
 * A panel to contain a collection of extra data collection declaration panels.
 */
public class ListOfExtraDataCollectionDeclsPanel extends Composite {

  private VerticalPanel mainPanel;
  private MyConstants myConstants;

  /**
   * @param experiment
   */
  public ListOfExtraDataCollectionDeclsPanel(final ExperimentDAO experiment) {
    mainPanel = new VerticalPanel();
    mainPanel.setSpacing(2);
    initWidget(mainPanel);
    myConstants = (MyConstants)GWT.create(MyConstants.class);
    Label lblDeclareExtras = new Label(myConstants.getExtraDataCollectionDeclarationTitle());
    lblDeclareExtras.setStyleName("gwt-Label-Header");
    mainPanel.add(lblDeclareExtras);

    for (Integer decl : ExperimentDAO.EXTRA_DATA_COLLECTION_DECLS) {
      ExtraDataCollectionDeclPanel declRow = new ExtraDataCollectionDeclPanel(decl, experiment);
      mainPanel.add(declRow);
    }

  }

}
