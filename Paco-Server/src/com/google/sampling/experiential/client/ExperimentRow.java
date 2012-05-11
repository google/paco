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
package com.google.sampling.experiential.client;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.sampling.experiential.shared.ExperimentDAO;

/**
 * 
 * A composite view for one experiment, usually used in a list of experiments.
 *  
 * @author Bob Evans
 *
 */
public class ExperimentRow extends Composite {

  private Images images;
  private ExperimentDAO experiment;
  private ArrayList<ExperimentListener> listeners;
  private boolean joined;

  public ExperimentRow(
      Images resources, ExperimentDAO experiment, ExperimentListener listener, boolean joined) {
    this.images = resources;
    this.experiment = experiment;
    this.joined = joined;
    this.listeners = new ArrayList<ExperimentListener>();
    if (listener != null) {
      listeners.add(listener);
    }
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setStyleName("paco-experimentRow");
    horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    horizontalPanel.setSpacing(1);
    horizontalPanel.setHeight("42px");
    initWidget(horizontalPanel);

    Image experimentIcon = new Image(resources.question());
    experimentIcon.setAltText("Experiment Icon");
    horizontalPanel.add(experimentIcon);
    horizontalPanel.setCellHeight(experimentIcon, "42");
    horizontalPanel.setCellWidth(experimentIcon, "42");
    horizontalPanel.setCellHorizontalAlignment(experimentIcon, HasHorizontalAlignment.ALIGN_CENTER);
    horizontalPanel.setCellVerticalAlignment(experimentIcon, HasVerticalAlignment.ALIGN_BOTTOM);
    experimentIcon.setSize("42px", "42px");

    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setHeight("39px");
    horizontalPanel.add(verticalPanel);

    HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
    horizontalPanel_2.setHeight("19px");
    verticalPanel.add(horizontalPanel_2);

    Label experimentTitleLabel = new Label(experiment.getTitle());
    if (experiment.getDeleted() != null && experiment.getDeleted()) {
      experimentTitleLabel.setStyleName("gwt-Link-underline-strikethrough");
    } else {
      experimentTitleLabel.setStyleName("gwt-Link-underline");
    }
    horizontalPanel_2.add(experimentTitleLabel);
    horizontalPanel_2.setCellWidth(experimentTitleLabel, "22px");
    horizontalPanel_2.setCellHeight(experimentTitleLabel, "18px");
    experimentTitleLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    horizontalPanel.setCellVerticalAlignment(
        experimentTitleLabel, HasVerticalAlignment.ALIGN_MIDDLE);
    experimentTitleLabel.setWidth("180px");
    ClickHandler titleHandler = new ClickHandler() {
      public void onClick(ClickEvent event) {
        showExperimentDetails();
      }
    };
    experimentTitleLabel.addClickHandler(titleHandler);
    experimentIcon.addClickHandler(titleHandler);

    HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
    horizontalPanel_1.setSpacing(1);
    verticalPanel.add(horizontalPanel_1);

    Button statsButton = new Button("Stats");
    statsButton.setStyleName("paco-ExperimentRow-Button");
    statsButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showStats();
      }
    });
    horizontalPanel_1.add(statsButton);
    horizontalPanel.setCellVerticalAlignment(statsButton, HasVerticalAlignment.ALIGN_MIDDLE);

    Button chartButton = new Button("Charts");
    chartButton.setStyleName("paco-ExperimentRow-Button");
    chartButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showCharts();
      }
    });
    horizontalPanel_1.add(chartButton);
    horizontalPanel.setCellVerticalAlignment(chartButton, HasVerticalAlignment.ALIGN_MIDDLE);

    Button csvButton = new Button("CSV");
    csvButton.setStyleName("paco-ExperimentRow-Button");
    csvButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showCSV();
      }
    });
    horizontalPanel_1.add(csvButton);
    horizontalPanel.setCellVerticalAlignment(csvButton, HasVerticalAlignment.ALIGN_MIDDLE);
    if (!joined) {
      Button deleteButton = new Button(
          experiment.getDeleted() != null && experiment.getDeleted() ? "Unhide" : "Hide");
      deleteButton.setStyleName("paco-ExperimentRow-Button");
      deleteButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          softDeleteExperiment();
        }
      });
      horizontalPanel_1.add(deleteButton);
      horizontalPanel.setCellVerticalAlignment(deleteButton, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    if (!joined) {
      Button deleteButton = new Button("Purge");
      deleteButton.setStyleName("paco-ExperimentRow-Button");
      deleteButton.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          deleteExperiment();
        }
      });
      horizontalPanel_1.add(deleteButton);
      horizontalPanel.setCellVerticalAlignment(deleteButton, HasVerticalAlignment.ALIGN_MIDDLE);
    }
  }

  protected void showExperimentDetails() {
    fireExperimentCode(ExperimentListener.EDIT_CODE);
  }

  protected void deleteExperiment() {
    fireExperimentCode(ExperimentListener.DELETE_CODE);
  }

  protected void softDeleteExperiment() {
    fireExperimentCode(ExperimentListener.SOFT_DELETE_CODE);
  }

  protected void showCSV() {
    fireExperimentCode(ExperimentListener.CSV_CODE);
  }

  protected void showCharts() {
    fireExperimentCode(ExperimentListener.CHARTS_CODE);
  }

  protected void showStats() {
    fireExperimentCode(ExperimentListener.STATS_CODE);
  }

  private void fireExperimentCode(int code) {
    for (ExperimentListener listener : listeners) {
      listener.eventFired(code, experiment, joined);
    }
  }


}
