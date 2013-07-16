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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.shared.DateStat;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentStatsDAO;

/**
 * Panel for basic experiment meta statistics, e.g., response rate.
 *
 * @author Bob Evans
 *
 */
public class StatsPanel extends Composite {

  private ExperimentStatsDAO experimentStats;
  private DateTimeFormat df = DateTimeFormat.getFormat("MM/dd/yyyy");
  private ExperimentDAO experiment;
  private boolean isAdmin;
  private List<ExperimentListener> listeners;
  private MyConstants myConstants; 

  public StatsPanel(ExperimentStatsDAO stats, ExperimentDAO experiment, boolean joinView, ExperimentListener listener) {
    myConstants = GWT.create(MyConstants.class);
    this.experimentStats = stats;
    this.experiment = experiment;
    this.listeners = Lists.newArrayList();
    listeners.add(listener);
    this.isAdmin = !joinView; // isAdmin(experiment, loggedInUser);
    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    initWidget(verticalPanel);

    String title = experiment.getTitle();
    if (title == null) {
      title = "Experiment";
    }
    Label lblStatisticsForExperiment = new Label("Statistics for " + title);
    lblStatisticsForExperiment.setStyleName("paco-HTML-Large");
    verticalPanel.add(lblStatisticsForExperiment);

    Grid grid = new Grid(3, 2);
    verticalPanel.add(grid);
    grid.setWidth("450px");

    if (isAdmin) {
      showJoinedStats(grid);
    }

    Label lblResponseRate = new Label("Response Rate:");
    lblResponseRate.setStyleName("gwt-Label-Header");
    grid.setWidget(1, 0, lblResponseRate);

    Label label_1 = new Label(experimentStats.getResponseRate());
    grid.setWidget(1, 1, label_1);

    Label lblMedianResponseTime = new Label("Median Response Time (minutes):");
    lblMedianResponseTime.setStyleName("gwt-Label-Header");
    grid.setWidget(2, 0, lblMedianResponseTime);

    Label label_2 = new Label(experimentStats.getResponseTime());
    grid.setWidget(2, 1, label_2);
    grid.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
    grid.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
    grid.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_LEFT);

    if (isAdmin) {
      show7DayActives(verticalPanel);
      showDailyResponseRate(verticalPanel);
    }
  }

  private void showDailyResponseRate(VerticalPanel verticalPanel) {
    VerticalPanel dailyResponsePanel = new VerticalPanel();
    dailyResponsePanel.setSpacing(2);
    verticalPanel.add(dailyResponsePanel);
    dailyResponsePanel.setSize("450px", "32px");

    Label dailyResponseLabel = new Label("Daily Response Count");
    dailyResponseLabel.setStyleName("paco-HTML");
    dailyResponsePanel.add(dailyResponseLabel);

    LineChart responseChartPanel = createLineChart(experimentStats.getDailyResponseRate(), "", "#");
    dailyResponsePanel.add(responseChartPanel);
  }

  private void show7DayActives(VerticalPanel verticalPanel) {
    VerticalPanel sevenDayActivePanel = new VerticalPanel();
    sevenDayActivePanel.setSpacing(2);
    verticalPanel.add(sevenDayActivePanel);
    sevenDayActivePanel.setSize("450px", "32px");

    Label sevenDayLabel = new Label("7-Day Active Participants");
    sevenDayLabel.setStyleName("paco-HTML");
    sevenDayActivePanel.add(sevenDayLabel);

    LineChart sevenDayChartPanel = createLineChart(experimentStats.getSevenDayDateStats(), "", "#");
    sevenDayActivePanel.add(sevenDayChartPanel);
  }

  /**
   * @param loggedInUser
   * @param experiment2
   * @param experiment2
   * @return
   */
  private boolean isAdmin(ExperimentDAO experiment2, String loggedInUser) {
    String[] admins = experiment.getAdmins();
    if (admins == null) {
      return false;
    }
    for (int i = 0; i < admins.length; i++) {
      if (admins[i].toLowerCase().equals(loggedInUser)) return true;
    }
    return false;
  }

  private void showJoinedStats(Grid grid) {
    Label joinedLabel = new Label("Participants Data (click for detail):");
    joinedLabel.setStyleName("gwt-Label-Header");
    joinedLabel.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        //showParticipantsPopup();
        fireExperimentCode(ExperimentListener.INDIVIDUAL_STATS_CODE);
      }
    });
    grid.setWidget(0, 0, joinedLabel);
    joinedLabel.setWidth("");

    Label label = new Label(Integer.toString(createMapofParticipantsAndJoinTimes().values().size()));
    grid.setWidget(0, 1, label);
  }

  private HashMap<String, String> createMapofParticipantsAndJoinTimes() {
    HashMap<String,String> participants = new HashMap<String,String>();
    for (EventDAO event : experimentStats.getJoinedEventsList()) {
      String who = event.getWho();
      String existingWhoValue = participants.get(who);
      if (existingWhoValue == null) {
        existingWhoValue = who + ": " + df.format(event.getResponseTime());
      } else {
        existingWhoValue += "," + df.format(event.getResponseTime());
      }
      participants.put(who, existingWhoValue);
    }
    return participants;
  }

  private void fireExperimentCode(int code) {
    for (ExperimentListener listener : listeners) {
      listener.eventFired(code, experiment, false, false);
    }
  }


  static LineChart createLineChart(
      DateStat[] dateStats, String chartTitle, String changingParameterKey) {
    String yAxis = changingParameterKey;
    String xAxis = "Date";


    DataTable data = DataTable.create();
    data.addRows(dateStats.length);
    data.addColumn(ColumnType.DATE, xAxis);
    // data.addColumn(ColumnType.NUMBER, "min");
    // data.addColumn(ColumnType.NUMBER, "med");
    data.addColumn(ColumnType.NUMBER, "value");
    // data.addColumn(ColumnType.NUMBER, "trend");
    // data.addColumn(ColumnType.NUMBER, "max");


    // List<DateStat> statsByDate =
    // DateStat.calculateParameterDailyStats(changingParameterKey, eventList);

    Double min = null;
    Double max = null;
    Double trend = null;
    int row = 0;
    for (DateStat stat : dateStats) {
      Date date = stat.getWhen();
      // Double paramAvg = stat.getAverage();
      List<Double> values = stat.getValues();
      for (Double dataValue : values) {


        if (min == null || stat.getMin() < min) {
          min = stat.getMin();
        }
        if (max == null || stat.getMax() > max) {
          max = stat.getMax();
        }
        if (row == 1) {
          trend = dataValue;
        } else if (row > 1) {
          // trend = recomputeTrend(trend, dataValue);
        }
        data.setValue(row, 0, date);
        // data.setValue(row, 1, stat.getMin());
        // data.setValue(row, 2, stat.getMed());
        data.setValue(row, 1, dataValue);
        // if (trend != null) {
        // data.setValue(row, 2, trend);
        // }
        row++;
      }
    }
    if (min == null) {
      min = 0.0;
    }
    if (max == null) {
      max = 0.0;
    }
    LineChart lineChart =
        new LineChart(data, createLineChartOptions(chartTitle, xAxis, yAxis, "#000033", min, max));
    return lineChart;
  }

  private static Double recomputeTrend(Double trend, double average) {
    double smoothingFactor = 0.1;
    return (smoothingFactor * average) + ((1 - smoothingFactor) * trend);
  }

  private static
      com.google.gwt.visualization.client.visualizations.LineChart.Options createLineChartOptions(
          String title, String xTitle, String yTitle, String barColor, double min, double max) {
    LineChart.Options options = LineChart.Options.create();
    options.setWidth(500);
    options.setHeight(300);
    options.setEnableTooltip(true);
    options.setPointSize(3);
    options.setMin(min);
    options.setMax(max);
    options.setTitle(title);
    options.setLegend(LegendPosition.BOTTOM);
    options.setShowCategories(true);
    options.setTitleX(xTitle);
    options.setTitleY(yTitle);
    options.setColors(barColor, "#660000");
    options.setBorderColor("black");
    return options;

  }
}
