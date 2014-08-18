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

import com.google.gwt.core.client.GWT;
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
import com.google.paco.shared.model.ExperimentDAO;

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
  private MyConstants myConstants;
  private boolean findView;

  public ExperimentRow(
      Images resources, ExperimentDAO experiment, ExperimentListener listener, boolean joined, boolean findView) {
    this.images = resources;
    this.myConstants = GWT.create(MyConstants.class);
    this.experiment = experiment;
    this.joined = joined;
    this.findView = findView;
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
    experimentIcon.setAltText(myConstants.experimentIcon());
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
    if (!isExperimentPurged()) {
      experimentTitleLabel.addClickHandler(titleHandler);
      experimentIcon.addClickHandler(titleHandler);
    }

    HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
    horizontalPanel_1.setSpacing(1);
    verticalPanel.add(horizontalPanel_1);

    createButtonPanel(experiment, joined, horizontalPanel, horizontalPanel_1, findView);

  }

  private void createButtonPanel(ExperimentDAO experiment, boolean joined, HorizontalPanel horizontalPanel,
                                 HorizontalPanel horizontalPanel_1, boolean findView) {
    if (findView) {
      createFindViewPanel(horizontalPanel, horizontalPanel_1);
    } else if (joined) {
      createJoinedButtonPanel(horizontalPanel, horizontalPanel_1);
    } else {
      createAdminButtonPanel(horizontalPanel, horizontalPanel_1);
    }

  }

  private void createAdminButtonPanel(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    createViewDataButton(horizontalPanel, horizontalPanel_1);
    createChartButton(horizontalPanel, horizontalPanel_1);
    createStatsButton(horizontalPanel, horizontalPanel_1);
    createCSVButton(horizontalPanel, horizontalPanel_1);
    createAnonCSVButton(horizontalPanel, horizontalPanel_1);
    createCopyButton(horizontalPanel, horizontalPanel_1);
    createDeleteButton(experiment, horizontalPanel, horizontalPanel_1);
    createPurgeButton(horizontalPanel, horizontalPanel_1);
    createQRCodeButton(horizontalPanel, horizontalPanel_1);
    createEodRefButton(horizontalPanel, horizontalPanel_1);
  }

  private void createJoinedButtonPanel(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    createRespondButton(horizontalPanel, horizontalPanel_1);
    createViewDataButton(horizontalPanel, horizontalPanel_1);
    createChartButton(horizontalPanel, horizontalPanel_1);
    createStatsButton(horizontalPanel, horizontalPanel_1);
    createCSVButton(horizontalPanel, horizontalPanel_1);
    createCopyButton(horizontalPanel, horizontalPanel_1);
  }

  private void createFindViewPanel(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    createDetailsButton(horizontalPanel, horizontalPanel_1);
    createQRCodeButton(horizontalPanel, horizontalPanel_1);
    createCopyButton(horizontalPanel, horizontalPanel_1);
  }

  private void createDetailsButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button copyButton = new Button(myConstants.copy());
    copyButton.setStyleName("paco-ExperimentRow-Button");
    copyButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showExperimentDetails();
      }
    });
    horizontalPanel_1.add(copyButton);
    horizontalPanel.setCellVerticalAlignment(copyButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createEodRefButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button refButton = new Button(myConstants.eodRef());
    refButton.setStyleName("paco-ExperimentRow-Button");
    refButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showRefDialog();
      }
    });
    horizontalPanel_1.add(refButton);
    horizontalPanel.setCellVerticalAlignment(refButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createQRCodeButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button qrCodeButton = new Button(myConstants.qrCode());
    qrCodeButton.setStyleName("paco-ExperimentRow-Button");
    qrCodeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showQRCode();
      }
    });
    horizontalPanel_1.add(qrCodeButton);
    horizontalPanel.setCellVerticalAlignment(qrCodeButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createPurgeButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button deleteButton = new Button(myConstants.purge());
    deleteButton.setStyleName("paco-ExperimentRow-Button");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        deleteExperiment();
      }
    });
    horizontalPanel_1.add(deleteButton);
    horizontalPanel.setCellVerticalAlignment(deleteButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createDeleteButton(ExperimentDAO experiment, HorizontalPanel horizontalPanel,
                                  HorizontalPanel horizontalPanel_1) {
    Button deleteButton = new Button(
        experiment.getDeleted() != null && experiment.getDeleted() ? myConstants.unHide() : myConstants.hide());
    deleteButton.setStyleName("paco-ExperimentRow-Button");
    deleteButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        softDeleteExperiment();
      }
    });
    horizontalPanel_1.add(deleteButton);
    horizontalPanel.setCellVerticalAlignment(deleteButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createCopyButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button copyButton = new Button(myConstants.copy());
    copyButton.setStyleName("paco-ExperimentRow-Button");
    copyButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showExperimentDetailsAsCopy();
      }
    });
    horizontalPanel_1.add(copyButton);
    horizontalPanel.setCellVerticalAlignment(copyButton, HasVerticalAlignment.ALIGN_MIDDLE);
    copyButton.setEnabled(!isExperimentPurged());
  }

  private boolean isExperimentPurged() {
    return experiment == null || experiment.getSignalingMechanisms() == null;
  }

  private void createAnonCSVButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button csvAnonButton = new Button(myConstants.anonCsv());
    csvAnonButton.setStyleName("paco-ExperimentRow-Button");
    csvAnonButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showCSVAnon();
      }
    });
    horizontalPanel_1.add(csvAnonButton);
    horizontalPanel.setCellVerticalAlignment(csvAnonButton, HasVerticalAlignment.ALIGN_MIDDLE);

    Button anonMappingButton = new Button(myConstants.anonMap());
    anonMappingButton.setStyleName("paco-ExperimentRow-Button");
    anonMappingButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showAnonMapping();
      }
    });
    horizontalPanel_1.add(anonMappingButton);
    horizontalPanel.setCellVerticalAlignment(csvAnonButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createCSVButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button csvButton = new Button(myConstants.csv());
    csvButton.setStyleName("paco-ExperimentRow-Button");
    csvButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showCSV();
      }
    });
    horizontalPanel_1.add(csvButton);
    horizontalPanel.setCellVerticalAlignment(csvButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createStatsButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button statsButton = new Button(myConstants.stats());
    statsButton.setStyleName("paco-ExperimentRow-Button");
    statsButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showStats();
      }
    });
    horizontalPanel_1.add(statsButton);
    horizontalPanel.setCellVerticalAlignment(statsButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createChartButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button chartButton = new Button(myConstants.charts());
    chartButton.setStyleName("paco-ExperimentRow-Button");
    chartButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showCharts();
      }
    });
    horizontalPanel_1.add(chartButton);
    horizontalPanel.setCellVerticalAlignment(chartButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createViewDataButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button dataButton = new Button(myConstants.viewData());
    dataButton.setStyleName("paco-ExperimentRow-Button");
    dataButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showData();
      }
    });
    horizontalPanel_1.add(dataButton);
    horizontalPanel.setCellVerticalAlignment(dataButton, HasVerticalAlignment.ALIGN_MIDDLE);
  }

  private void createRespondButton(HorizontalPanel horizontalPanel, HorizontalPanel horizontalPanel_1) {
    Button enterButton = new Button(myConstants.respond());
    enterButton.setStyleName("paco-ExperimentRow-Button");
    enterButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showExecutorPanel();
      }
    });
    horizontalPanel_1.add(enterButton);
    horizontalPanel.setCellVerticalAlignment(enterButton, HasVerticalAlignment.ALIGN_MIDDLE);
    enterButton.setEnabled(!isExperimentPurged());
  }

  protected void showRefDialog() {
    fireExperimentCode(ExperimentListener.SHOW_REF_CODE);
  }

  protected void showQRCode() {
    fireExperimentCode(ExperimentListener.SHOW_QR_CODE);

  }

  protected void showExecutorPanel() {
    fireExperimentCode(ExperimentListener.SHOW_EXPERIMENT_RESPONSE_CODE);

  }

  protected void showAnonMapping() {
    fireExperimentCode(ExperimentListener.ANON_MAPPING_CODE);
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

  protected void showData() {
    fireExperimentCode(ExperimentListener.DATA_CODE);
  }

  protected void showCSV() {
    fireExperimentCode(ExperimentListener.CSV_CODE);
  }

  protected void showCSVAnon() {
    fireExperimentCode(ExperimentListener.CSV_ANON_CODE);
  }

  protected void showExperimentDetailsAsCopy() {
    fireExperimentCode(ExperimentListener.COPY_EXPERIMENT_CODE);
  }

  protected void showCharts() {
    fireExperimentCode(ExperimentListener.CHARTS_CODE);
  }

  protected void showStats() {
    fireExperimentCode(ExperimentListener.INDIVIDUAL_STATS_CODE);
  }

  private void fireExperimentCode(int code) {
    for (ExperimentListener listener : listeners) {
      listener.eventFired(code, experiment, joined, findView);
    }
  }


}
