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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentDAOCore;

/**
 * Panel to show one individual list choice item. This is part of the
 * definition of an input whose responsetype is list.
 *
 * @author Bob Evans
 *
 */
public class ExtraDataCollectionDeclPanel extends Composite {

  private HorizontalPanel horizontalPanel;
  private CheckBox checkBox;
  private ExperimentDAO experiment;
  private Integer decl;
  private MyConstants myConstants;

  /**
   */
  public ExtraDataCollectionDeclPanel(Integer decl, ExperimentDAO experiment) {
    this.experiment = experiment;
    this.decl = decl;
    myConstants = GWT.create(MyConstants.class);
    horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSpacing(2);
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    initWidget(horizontalPanel);
    horizontalPanel.setWidth("258px");

    checkBox = new CheckBox();
    horizontalPanel.add(checkBox);
    checkBox.setValue(experiment.getExtraDataCollectionDeclarations().contains(decl));

    Label lblTime = new Label(getDeclLabel());
    lblTime.setStyleName("gwt-Label-Header");
    horizontalPanel.add(lblTime);
    lblTime.setWidth("400px");


    checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        updateExperiment(event.getValue());
      }

    });

  }

  private String getDeclLabel() {
    switch (decl) {
    case ExperimentDAOCore.APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION:
      return myConstants.appUsageAndBrowserHistoryDataCollection();
    case ExperimentDAOCore.LOCATION_DATA_COLLECTION:
      return myConstants.locationDataCollection();
    case ExperimentDAOCore.PHONE_DETAILS:
      return myConstants.phoneDetailsDataCollection();
    default:
      return myConstants.unknownDataCollectionLabel();
    }
  }

  private void updateExperiment(Boolean checked) {
    if (checked) {
      if (!experiment.getExtraDataCollectionDeclarations().contains(decl)) {
        experiment.getExtraDataCollectionDeclarations().add(decl);
      }
    } else {
      if (experiment.getExtraDataCollectionDeclarations().contains(decl)) {
        experiment.getExtraDataCollectionDeclarations().remove(decl);
      }
    }


  }


}
