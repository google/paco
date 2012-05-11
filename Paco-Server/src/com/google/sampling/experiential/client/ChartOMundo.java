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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.ColumnChart;
import com.google.gwt.visualization.client.visualizations.ColumnChart.Options;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.ScatterChart;
import com.google.sampling.experiential.shared.DateStat;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

public class ChartOMundo {

  private DateTimeFormat formatter = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);
  private static final int CHART_HEIGHT = 300;
  private static final int CHART_WIDTH = 500;
  private List<Widget> charts = Lists.newArrayList();

  /**
   * Produce a set of charts, in most-interesting -> least-interesting order
   * otherwise there may be way too many charts for all variable combinations.
   * 
   * So, first, do Time-series line charts for all numbers, ordered by variance
   * (most -> least) Next, do bar charts for all categorical strings, ordered by
   * variance Next do scatterplot of all numerical types pair-wise. Next do
   * scatterplot with numericals and strings as labels on dots.
   * 
   * 
   * @param query
   * @param eventList
   */
  public List<Widget> autoChart(String query, List<EventDAO> eventList) {

    String chartTitle = "";

    Map<String, List<EventDAO>> eventsByWho = getWhosFrom(eventList);
    // do whole population charts
    createChartsForEvents(eventList, chartTitle + "whole population");
    // do charts for each who
    if (eventsByWho.keySet().size() > 1) {
      for (String who : eventsByWho.keySet()) {
        List<EventDAO> whosEvents = eventsByWho.get(who);
        createChartsForEvents(whosEvents, chartTitle + " who = " + who);
      }
    }
    // plot scatterPlotsForNumericPairWiseWithVaryingKeysAsValue
    // createTable for NonVaryingKeyValues if cluster factor == 1
    // or
    // create tag cloud for words of any string type key if the cluster factor
    // is not 0.

    return charts;

  }

  private static class Correlation implements Comparable<Correlation> {
    Pair pair;
    double correlation;

    public Correlation(Pair pair, double correlation) {
      this.pair = pair;
      this.correlation = correlation;
    }

    @Override
    public int compareTo(Correlation o2) {
      // either positive or negative correlations are interesting, so
      // sort them by the absolute value
      double abs1 = Math.abs(correlation);
      double abs2 = Math.abs(o2.correlation);
      if (abs1 > abs2) {
        return 1;
      } else if (abs2 > abs1) {
        return -1;
      } else {
        // if they are equal, then show positive correlations
        // before negative correlations, so compare the real correlations.
        if (correlation > o2.correlation) {
          return 1;
        } else if (o2.correlation > correlation) {
          return -1;
        } else {
          return 0;
        }
      }
    }

  }

  private void createChartsForEvents(List<EventDAO> eventList, String chartTitle) {
    Map<String, List<String>> mapOfAllValuesByKey = mapOfAllValuesForKey(eventList);
    Map<String, Class> typesOfValuesByKey = getTypesOfValues(mapOfAllValuesByKey);

    Map<String, List<? extends Number>> numericTypes =
        getNumericTypes(typesOfValuesByKey, mapOfAllValuesByKey);
    Map<String, List<? extends Date>> dateTypes =
        getDateTypes(typesOfValuesByKey, mapOfAllValuesByKey);
    List<String> numericKeysByVarianceDescending = sortKeysByVariance(numericTypes);
    plotTimeSeriesOfValuesForKeys(numericKeysByVarianceDescending, eventList, chartTitle);

    // plotScatterPlotsForNumericsPairWise(numericKeysByVarianceDescending,
    // eventList, chartTitle);
    List<Correlation> correlations = getKeyCorrelations(numericTypes);
    plotScatterPlotForPairsByCorrelation(correlations, eventList, chartTitle);

    Map<String, List<String>> stringTypes =
        getStringTypes(mapOfAllValuesByKey, numericTypes, dateTypes);
    List<KeyValuePair> countsByKey = organizeAndSortStringTypesByClusterFactor(stringTypes);
    List<String> stringKeysByVarianceDescending = collectKeysFrom(countsByKey);
    List<String> varyingKeys = getKeysWithVarianceGT1(countsByKey);

    plotBarChartsForCategoricalStringKeys(varyingKeys, eventList, chartTitle);

    plotWordCloudsForCategoricals(chartTitle, getKeysWithMoreThanOneValue(stringTypes), eventList);
  }

  private void plotWordCloudsForCategoricals(String chartTitle,
      List<String> stringKeysByVarianceDescending, List<EventDAO> eventList) {
    for (String key : stringKeysByVarianceDescending) {
      plotWordCloudForCategoricals(chartTitle, key, eventList);
    }
  }

  private void plotScatterPlotForPairsByCorrelation(List<Correlation> correlations,
      List<EventDAO> eventList, String chartTitle) {
    List<Pair> pairWiseKeys = Lists.newArrayList();
    for (Correlation correlation : correlations) {
      pairWiseKeys.add(correlation.pair);
    }
    plotScatterPlotForPairs(eventList, chartTitle, pairWiseKeys);
  }

  private List<Correlation> getKeyCorrelations(Map<String, List<? extends Number>> numericTypes) {
    List<Correlation> correlationsByKeyPair = Lists.newArrayList();
    Map<String, List<Double>> stdUnitsbyKey = convertValuesToStandardUnits(numericTypes);

    List<Pair> keyPairs = getPairWiseKeysFor(Lists.newArrayList(numericTypes.keySet()));

    for (Pair pair : keyPairs) {
      List<Double> key1Values = stdUnitsbyKey.get(pair.key1);
      List<Double> key2Values = stdUnitsbyKey.get(pair.key2);
      double correlation;
      if (key1Values.size() < key2Values.size()) {
        correlation = computeAverage(multiplyEachValue(key1Values, key2Values));
      } else {
        correlation = computeAverage(multiplyEachValue(key2Values, key1Values));
      }

      correlationsByKeyPair.add(new Correlation(pair, correlation));
    }

    Collections.sort(correlationsByKeyPair);
    return correlationsByKeyPair;
  }

  private Map<String, List<Double>> convertValuesToStandardUnits(
      Map<String, List<? extends Number>> numericTypes) {
    Map<String, List<Double>> stdUnitsbyKey = Maps.newHashMap();

    for (String key : numericTypes.keySet()) {
      List<? extends Number> numbersForKey = numericTypes.get(key);
      Double average = computeAverage(numbersForKey);
      Double standardDeviation = computeStdDev(numericTypes.get(key));
      List<Double> stdUnits = Lists.newArrayList();
      for (Number number : numbersForKey) {
        stdUnits.add((number.doubleValue() - average) / standardDeviation);
      }
      stdUnitsbyKey.put(key, stdUnits);
    }
    return stdUnitsbyKey;
  }

  private List<Double> multiplyEachValue(List<Double> key1Values, List<Double> key2Values) {
    List<Double> products = Lists.newArrayList();
    for (int i = 0; i < key1Values.size(); i++) {
      products.add(key1Values.get(i) * key2Values.get(i));
    }
    return products;
  }

  private Map<String, List<EventDAO>> getWhosFrom(List<EventDAO> eventList) {
    Map<String, List<EventDAO>> eventsByWho = Maps.newHashMap();
    for (EventDAO eventDAO : eventList) {
      String who = eventDAO.getWho();
      List<EventDAO> eventListForWho = eventsByWho.get(who);
      if (eventListForWho == null) {
        eventListForWho = Lists.newArrayList(eventDAO);
        eventsByWho.put(who, eventListForWho);
      } else {
        eventListForWho.add(eventDAO);
      }
    }
    return eventsByWho;
  }

  private List<String> getKeysWithVarianceGT1(List<KeyValuePair> countsByKey) {
    List<String> varyingKeys = Lists.newArrayList();
    for (KeyValuePair kvPair : countsByKey) {
      if (kvPair.value < 1 && kvPair.value > 0.000) {
        varyingKeys.add(kvPair.key);
      }
    }
    return varyingKeys;
  }

  private List<String> getKeysWithNoVariance(List<KeyValuePair> countsByKey) {
    List<String> nonVaryingKeys = Lists.newArrayList();
    for (KeyValuePair kvPair : countsByKey) {
      if (kvPair.value == 1 || kvPair.value == 0) {
        nonVaryingKeys.add(kvPair.key);
      }
    }
    return nonVaryingKeys;
  }

  class Pair {
    String key1;
    String key2;

    public Pair(String key1, String key2) {
      this.key1 = key1;
      this.key2 = key2;
    }

    @Override
    public boolean equals(Object obj) {
      Pair other = (Pair) obj;
      return key1.equals(other.key1) && key2.equals(other.key2);
    }

    @Override
    public int hashCode() {
      return key1.hashCode() * key2.hashCode();
    }
  }

  private void plotScatterPlotsForNumericsPairWise(List<String> numericKeysByVarianceDescending,
      List<EventDAO> eventList, String chartTitle) {
    List<Pair> pairWiseKeys = getPairWiseKeysFor(numericKeysByVarianceDescending);
    plotScatterPlotForPairs(eventList, chartTitle, pairWiseKeys);
  }

  private void plotScatterPlotForPairs(List<EventDAO> eventList, String chartTitle,
      List<Pair> pairWiseKeys) {
    for (Pair pair : pairWiseKeys) {
      plotScatterPlotForKeys(pair.key1, pair.key2, eventList, chartTitle + " keys = " + pair.key1
          + " x " + pair.key2);
    }
  }

  private void plotScatterPlotForKeys(String key1, String key2, List<EventDAO> eventList,
      String chartTitle) {
    charts.add(createDisclosurePanel(chartTitle, createScatterChart(eventList, chartTitle, key1,
        key2)));
  }

  private List<Pair> getPairWiseKeysFor(List<String> keys) {
    Set<Pair> allPairs = Sets.newHashSet(); // By virtue of the pair class' eql
                                            // and hashcode, we should not have
                                            // duplicates from the nxn traversal
                                            // here.
    for (String key1 : keys) {
      for (String key2 : keys) {
        if (key1.equals(key2)) {
          continue;
        }
        Pair newPair = new Pair(key1, key2);
        // TODO (bobevans): there is bound to be a better way to get half a
        // matrix so as to
        // prevent redundancies.
        if (!allPairs.contains(newPair) && !allPairs.contains(new Pair(key2, key1))) {
          allPairs.add(newPair);
        }
      }
    }
    return Lists.newArrayList(allPairs);
  }

  private void plotBarChartsForCategoricalStringKeys(List<String> stringKeysByVarianceDescending,
      List<EventDAO> eventList, String chartTitle) {
    for (String string : stringKeysByVarianceDescending) {
      plotBarChartForCategoricalStringKey(string, eventList, chartTitle);
    }
  }

  private void plotBarChartForCategoricalStringKey(String key, List<EventDAO> eventList,
      String chartTitle) {
    String chartTitleFull = chartTitle + " KEY = " + key;
    charts
        .add(createDisclosurePanel(chartTitleFull, createBarChart(eventList, chartTitleFull, key)));
  }

  private void plotWordCloudForCategoricals(String chartTitle, 
      String key, 
      List<EventDAO> eventList) {
    charts.add(createWordCloud(chartTitle, eventList, key));
  }

  private void plotTimeSeriesOfValuesForKeys(List<String> numericKeysByVarianceDescending,
      List<EventDAO> eventList, String chartTitle) {
    for (String key : numericKeysByVarianceDescending) {
      plotTimeSeriesForKey(key, eventList, chartTitle);
    }
  }

  private void plotTimeSeriesForKey(String key, List<EventDAO> eventList, String chartTitle) {
    String chartTitleWithKey = chartTitle + " KEY = " + key;
    charts.add(createDisclosurePanel(chartTitleWithKey, createLineChart(eventList,
        chartTitleWithKey, key)));
  }

  private Widget createDisclosurePanel(String chartTitleWithKey, Widget chart) {
    DisclosurePanel p = new DisclosurePanel();
    p.setHeader(new Label(chartTitleWithKey));
    p.add(chart);
    return p;
  }

  class KeyValuePair implements Comparable<KeyValuePair> {
    public String key;
    public Double value;

    public KeyValuePair(String key, Double value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public int compareTo(KeyValuePair o) {
      return value.compareTo(o.value);
    }
  }

  private List<String> collectKeysFrom(List<KeyValuePair> countsByKey) {
    List<String> keys = Lists.newArrayList();
    for (KeyValuePair keyValuePair : countsByKey) {
      keys.add(keyValuePair.key);
    }
    return keys;
  }

  private List<KeyValuePair> organizeAndSortStringTypesByClusterFactor(
      Map<String, List<String>> stringTypes) {
    List<KeyValuePair> countsByKey = Lists.newArrayList();
    for (String key : stringTypes.keySet()) {
      List<String> values = stringTypes.get(key);
      Set<String> uniqueValues = Sets.newHashSet(values);
      double clusterFactor;
      if (uniqueValues.size() == 1 && values.size() > 1) { // all values are the
                                                           // same
        clusterFactor = 0;
      } else {
        clusterFactor = (double) uniqueValues.size() / (double) values.size();
      }
      countsByKey.add(new KeyValuePair(key, clusterFactor));
    }
    Collections.sort(countsByKey);
    return countsByKey;
  }

  private List<String> getKeysWithMoreThanOneValue(Map<String, List<String>> stringTypes) {
    List<String> keys = Lists.newArrayList();
    for (String key : stringTypes.keySet()) {
      List<String> values = stringTypes.get(key);
      Set<String> uniqueValues = Sets.newHashSet(values);
      if (uniqueValues.size() > 1) {
        keys.add(key);
      }
    }
    return keys;
  }

  private List<String> sortKeysByVariance(Map<String, List<? extends Number>> numericTypes) {
    List<KeyValuePair> stdDevs = Lists.newArrayList();
    for (String key : numericTypes.keySet()) {
      List<? extends Number> value = numericTypes.get(key);
      stdDevs.add(new KeyValuePair(key, computeStdDev(value)));
    }

    Collections.sort(stdDevs);
    List<String> keys = Lists.newArrayList();
    for (KeyValuePair stdDev : stdDevs) {
      keys.add(stdDev.key);
    }
    return keys;
  }

  private Double computeAverage(List<? extends Number> value) {
    double sum = 0;
    for (Number number : value) {
      sum += number.doubleValue();
    }
    return sum / value.size();
  }

  private Double computeStdDev(List<? extends Number> value) {
    double sum = 0;
    for (Number number : value) {
      sum += number.doubleValue();
    }
    double avg = sum / value.size();
    double variance = 0;
    for (Number number : value) {
      double difference = number.doubleValue() - avg;
      variance += (difference * difference);
    }
    return Math.sqrt(variance);
  }

  private Map<String, List<String>> getStringTypes(Map<String, List<String>> mapOfAllValuesByKey,
      Map<String, List<? extends Number>> numericTypes, 
      Map<String, List<? extends Date>> dateTypes) {
    Map<String, List<String>> stringTypes = Maps.newHashMap(mapOfAllValuesByKey);
    for (String key : numericTypes.keySet()) {
      stringTypes.remove(key);
    }
    for (String key : dateTypes.keySet()) {
      stringTypes.remove(key);
    }
    return stringTypes;
  }

  private Map<String, List<? extends Number>> getNumericTypes(
      Map<String, Class> typesOfValuesByKey, Map<String, List<String>> mapOfAllValuesByKey) {
    Map<String, List<? extends Number>> numberValues = Maps.newHashMap();
    for (String key : typesOfValuesByKey.keySet()) {
      // TODO (bobevans): Deal with the case where there are multiple types in a
      // key
      // (due to multiple appIds, or inconsistent type usage by an app), and
      // either
      // split by appId, or take the type of the mode - discarding bad types, or
      // throw an error
      // NOTE: this problem also means that if the first value is not the mode
      // type,
      // then we mistype the key altogether.
      Class type = typesOfValuesByKey.get(key);
      if (type.equals(Long.class) || type.equals(Double.class)) {
        numberValues.put(key, convertToNumberType(type, mapOfAllValuesByKey.get(key)));
      }
    }
    return numberValues;
  }

  private Map<String, List<? extends Date>> getDateTypes(Map<String, Class> typesOfValuesByKey,
      Map<String, List<String>> mapOfAllValuesByKey) {

    Map<String, List<? extends Date>> numberValues = Maps.newHashMap();
    for (String key : typesOfValuesByKey.keySet()) {
      // TODO (bobevans): Deal with the case where there are multiple types in a
      // key
      // (due to multiple appIds, or inconsistent type usage by an app), and
      // either
      // split by appId, or take the type of the mode - discarding bad types, or
      // throw an error
      // NOTE: this problem also means that if the first value is not the mode
      // type,
      // then we mistype the key altogether.
      Class type = typesOfValuesByKey.get(key);
      if (type.equals(Date.class)) {
        try {
          numberValues.put(key, convertToDateType(mapOfAllValuesByKey.get(key)));
        } catch (IllegalArgumentException ia) {
        }
      }
    }
    return numberValues;
  }


  private List<? extends Date> convertToDateType(List<String> list) {
    List<Date> dateList = Lists.newArrayList();
    for (String dateStr : list) {
      Date date = formatter.parse(dateStr);
      dateList.add(date);
    }
    return dateList;
  }

  private List<? extends Number> convertToNumberType(Class type, List<String> list) {
    if (type.equals(Long.class)) {
      List<Long> resultLong = Lists.newArrayList();
      for (String numberString : list) {
        try {
          resultLong.add(Long.valueOf(numberString));
        } catch (NumberFormatException e) {
          // Skip broken entry
          // TODO(bobevans):Log it
        }
      }
      return resultLong;
    } else if (type.equals(Double.class)) {
      List<Double> resultDouble = Lists.newArrayList();
      for (String numberString : list) {
        try {
          resultDouble.add(Double.valueOf(numberString));
        } catch (NumberFormatException e) {
          // Skip broken entry
          // TODO(bobevans):Log it
        }
      }
      return resultDouble;
    } else {
      throw new IllegalArgumentException("Unknown Number type!");
    }
  }

  private Map<String, Class> getTypesOfValues(Map<String, List<String>> mapOfAllValuesByKey) {
    Map<String, Class> typeMap = Maps.newHashMap();
    for (String key : mapOfAllValuesByKey.keySet()) {
      String value = mapOfAllValuesByKey.get(key).get(0);
      typeMap.put(key, getDataTypeOf(value));
    }
    return typeMap;
  }

  /**
   * Produce a set of charts, in most-interesting -> least-interesting order
   * otherwise there may be way too many charts for all variable combinations.
   * 
   * @param query
   * @param eventList
   */
  public void autoChartByVariance(String query, List<EventDAO> eventList) {
    String chartTitle = "Query: " + query;
    // List<List<String>> queryKeyValuePairs = parseKeyValuePairsFromQuery();
    Map<String, List<String>> mapOfAllValuesByKey = mapOfAllValuesForKey(eventList);
    TreeMap<String, Integer> variancesOfKeys = getVariancesOfKeys(mapOfAllValuesByKey);
    List<String> keysWithVariance = getKeysThatVary(variancesOfKeys);

    if (mapOfAllValuesByKey.keySet().size() == 1 || keysWithVariance.size() == 1) {
      charts.add(createUnivariateChart(mapOfAllValuesByKey.keySet().iterator().next(), eventList,
          chartTitle));
    } else if (keysWithVariance.size() == 2) {
      charts.add(createBiVariateChart(keysWithVariance, eventList, chartTitle));
    } else /* if (keysWithVariance.size() > 2) */{
      charts.add(createMultiVariateChart(variancesOfKeys, eventList, chartTitle));
      // pick the most varying for some reason.
      // String mostDynamicKey = getMostVaryingKey(mapOfAllValuesByKey);
      // return createUnivariateChart(mostDynamicKey, eventList, chartTitle);
    }
  }

  private Widget createMultiVariateChart(TreeMap<String, Integer> variancesOfKeys,
      List<EventDAO> eventList, String chartTitle) {
    Widget chart = null;
    // putMostVaryingDimensionOnYAxis
    // putNextMostVaryingDimensionOnXAxis
    // getNextMostVaryingAsSizeofDot_ColorOfDot_Or_MouseOverForDot
    // these all depend on whether the varying parameter is a number or a
    // string.
    // TODO(bobevans):What happens if the most varying is a string type, not a
    // number?
    // createXYPlot(yAxis, xAxis,
    return chart;
  }

  private Widget createBiVariateChart(List<String> keysWithVariance, List<EventDAO> eventList,
      String chartTitle) {
    Widget chart = null;
    // putMostVaryingDimensionOnYAxis
    // putNextMostVaryingDimensionOnXAxis
    return chart;
  }

  private List<String> getKeysThatVary(TreeMap<String, Integer> variancesOfKeys) {
    List<String> varyingKeys = Lists.newArrayList();
    for (String key : variancesOfKeys.keySet()) {
      Integer currValue = variancesOfKeys.get(key);
      if (currValue > 1) {
        varyingKeys.add(key);
      }
    }
    return varyingKeys;
  }

  private String getMostVaryingKey(Map<String, List<String>> mapOfAllValuesByKey) {
    TreeMap<String, Integer> variances = getVariancesOfKeys(mapOfAllValuesByKey);
    // Set<String> keys = variances.keySet();
    // String mostVaryingKey = null;
    // Integer mostVariance = -1;
    // for (String key : keys) {
    // Integer varianceForKey = variances.get(key);
    // if (mostVaryingKey == null || varianceForKey > mostVariance) {
    // mostVariance = varianceForKey;
    // mostVaryingKey = key;
    // }
    // }
    return variances.firstKey();
  }

  private TreeMap<String, Integer> getVariancesOfKeys(Map<String, 
      List<String>> mapOfAllValuesByKey) {
    TreeMap<String, Integer> mapOfVariances = Maps.newTreeMap();
    for (String key : mapOfAllValuesByKey.keySet()) {
      mapOfVariances.put(key, mapOfAllValuesByKey.get(key).size());
    }
    return mapOfVariances;
  }

  private Widget createUnivariateChart(String key, List<EventDAO> eventList, String title) {
    Widget chart = null;
    Class typeOfData = getDataTypeOf(eventList.get(0).getWhatByKey(key));
    if (typeOfData.equals(String.class)) {
      createHistogram(eventList, title, key);
    }
    return chart;
  }

  Class getDataTypeOf(String value) {
    // TODO (bobevans): this is a bad way to do this. Very expensive.
    try {
      Double.parseDouble(value);
      return Double.class;
    } catch (NumberFormatException e) {
    }

    try {
      Long.parseLong(value);
      return Long.class;
    } catch (NumberFormatException e) {
    }
    try {
      formatter.parse(value);
      return Date.class;
    } catch (IllegalArgumentException e) {
    }

    return String.class;
  }

  private Map<String, List<String>> mapOfAllValuesForKey(List<EventDAO> eventList) {
    Map<String, List<String>> allValuesByKey = Maps.newHashMap();
    for (EventDAO eventDAO : eventList) {
      Map<String, String> whatMap = eventDAO.getWhat();
      for (String key : whatMap.keySet()) {
        String value = whatMap.get(key);
        List<String> valuesForKey = allValuesByKey.get(key);
        if (valuesForKey == null) {
          valuesForKey = Lists.newArrayList();
          allValuesByKey.put(key, valuesForKey);
        }
        valuesForKey.add(value);
      }
    }
    return allValuesByKey;
  }

  public ColumnChart createBarChart(List<EventDAO> eventList, String chartTitle,
      String changingParameterKey) {
    String xAxis = changingParameterKey;
    String yAxis = "Count";


    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING, xAxis);
    data.addColumn(ColumnType.NUMBER, yAxis);

    Map<String, Integer> counts = Maps.newHashMap();
    for (EventDAO event : eventList) {
      String activity = event.getWhatByKey(changingParameterKey);
      if (activity == null) {
        continue;
      }
      Integer activityCount = counts.get(activity);
      if (activityCount == null) {
        activityCount = 0;
      }
      counts.put(activity, activityCount + 1);
    }

    data.addRows(counts.keySet().size());

    int row = 0;
    for (String key : counts.keySet()) {
      data.setValue(row, 0, key);
      data.setValue(row, 1, counts.get(key));
      row++;
    }

    ColumnChart bar = new ColumnChart(data, createOptions(chartTitle, xAxis, yAxis, "#ffcc00"));
    return bar;
  }

  public ColumnChart createBarChartForList(List<EventDAO> eventList, String chartTitle,
      String changingParameterKey, String[] listChoices, Boolean multiselectList) {
    String xAxis = changingParameterKey;
    String yAxis = "Count";


    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING, xAxis);
    data.addColumn(ColumnType.NUMBER, yAxis);

    Map<String, Integer> counts = Maps.newHashMap();
    for (EventDAO event : eventList) {
      String activity = event.getWhatByKey(changingParameterKey);
      if (activity == null) {
        continue;
      }
      List<String> activities = Lists.newArrayList();
      if (multiselectList != null && multiselectList) {
        for (String currentActivity : Splitter.on(',').split(activity)) {
          activities.add(currentActivity);
        }
      } else {
        activities.add(activity);
      }
      for (String currentActivity : activities) {
        Integer activityCount = counts.get(currentActivity);
        if (activityCount == null) {
          activityCount = 0;
        }
        counts.put(currentActivity, activityCount + 1);  
      }
      
    }

    data.addRows(listChoices.length);

    int row = 0;
//    for (String key : counts.keySet()) {
    for (int i=0; i < listChoices.length; i++) {
      data.setValue(row, 0, listChoices[i]);
      int countForChoice = 0;
      String iStr = Integer.toString(i + 1); 
      //everything is offset by 1 because listchoices are 1-n, not zero based
      if (counts.containsKey(iStr)) {
        countForChoice = counts.get(iStr);
      }
      data.setValue(row, 1, countForChoice);
      row++;
    }
    
//    log.info("Keys in the dataset: ");
//    for (String key : counts.keySet()) {
//      log.info("key: " + key);
//    }
//    

    ColumnChart bar = new ColumnChart(data, createOptions(chartTitle, xAxis, yAxis, "#ffcc00"));
    return bar;
  }

  /**
   * @param key
   * @param strings
   * @return
   */
  private String getListChoiceFor(String key, String[] strings) {
    int keyInt = Integer.parseInt(key);
    if (keyInt < strings.length) {
      return strings[keyInt - 1];
    }
    return key;
  }

  WordCloudView createWordCloud(String chartTitle, List<EventDAO> eventList, String key) {
    List<String> entries = Lists.newArrayList();
    for (EventDAO event : eventList) {
      String value = event.getWhatByKey(key);
      if (value == null || value.isEmpty()) {
        continue;
      }
      entries.add(value);
    }

    return new WordCloudView(chartTitle + " key = " + key, entries);
  }

  private ColumnChart createHistogram(List<EventDAO> eventList, String chartTitle,
      String changingParameterKey) {
    String xAxis = changingParameterKey;
    String yAxis = "Count";


    DataTable data = DataTable.create();
    data.addRows(5);
    data.addColumn(ColumnType.NUMBER, xAxis);
    data.addColumn(ColumnType.NUMBER, yAxis);

    int[] counts = new int[] {0, 0, 0, 0, 0};
    for (EventDAO event : eventList) {
      try {
        Integer rating = Integer.parseInt(event.getWhatByKey(changingParameterKey).trim());
        counts[rating + 2] = counts[rating + 2] + 1;
      } catch (NumberFormatException nfe) {
      }
    }

    data.setValue(0, 0, counts[0]);
    data.setValue(1, 0, counts[1]);
    data.setValue(2, 0, counts[2]);
    data.setValue(3, 0, counts[3]);
    data.setValue(4, 0, counts[4]);


    ColumnChart bar = new ColumnChart(data, createOptions(chartTitle, xAxis, yAxis, "#ffcc00"));
    return bar;
  }

  private ScatterChart createScatterChart(List<EventDAO> eventList, String chartTitle, String key1,
      String key2) {
    String xAxis = key1;
    String yAxis = key2;

    DataTable data = DataTable.create();
    data.addRows(eventList.size());
    data.addColumn(ColumnType.NUMBER, xAxis);
    data.addColumn(ColumnType.NUMBER, yAxis);


    int row = 0;
    // String debugPoints = "";
    for (EventDAO event : eventList) {
      String key1ValStr = event.getWhatByKey(key1);
      String key2ValStr = event.getWhatByKey(key2);
      // debugPoints += key1ValStr + "," + key2ValStr +"<br/>";
      if (key1ValStr == null || key2ValStr == null) {
        continue;
      }
      Class key1ValueClass = getDataTypeOf(key1ValStr);
      Class key2ValueClass = getDataTypeOf(key2ValStr);
      if (key1ValueClass.equals(Double.class)) {
        data.setValue(row, 0, Double.valueOf(key1ValStr));
      } else {
        data.setValue(row, 0, Long.valueOf(key1ValStr));
      }
      if (key2ValueClass.equals(Double.class)) {
        data.setValue(row, 1, Double.valueOf(key2ValStr));
      } else {
        data.setValue(row, 1, Long.valueOf(key2ValStr));
      }

      row++;
    }
    // Window.alert("Points:<br/>" + debugPoints);
    ScatterChart scatterChart =
        new ScatterChart(data, 
            createScatterChartOptions(chartTitle, xAxis, yAxis, "#000033", 0, 0));
    return scatterChart;
  }

  static LineChart createLineChart(List<EventDAO> eventList, String chartTitle,
      String changingParameterKey) {
    String yAxis = changingParameterKey;
    String xAxis = "Date";

    List<DateStat> statsByDate =
      DateStat.calculateParameterDailyStats(changingParameterKey, eventList);
  
    DataTable data = DataTable.create();
    data.addRows(statsByDate.size());
    data.addColumn(ColumnType.DATE, xAxis);
    // data.addColumn(ColumnType.NUMBER, "min");
    // data.addColumn(ColumnType.NUMBER, "med");
    data.addColumn(ColumnType.NUMBER, "value");
    data.addColumn(ColumnType.NUMBER, "trend");
    // data.addColumn(ColumnType.NUMBER, "max");


    Double min = null;
    Double max = null;
    Double trend = null;
    int row = 0;
    for (DateStat stat : statsByDate) {
      Date date = stat.getWhen();
       Double dataValue = stat.getAverage();
//      List<Double> values = stat.getValues();
//      for (Double dataValue : values) {


        if (min == null || stat.getMin() < min) {
          min = stat.getMin();
        }
        if (max == null || stat.getMax() > max) {
          max = stat.getMax();
        }
        if (row == 1) {
          trend = dataValue;
        } else if (row > 1) {
          trend = recomputeTrend(trend, dataValue);
        }
        data.setValue(row, 0, date);
        // data.setValue(row, 1, stat.getMin());
        // data.setValue(row, 2, stat.getMed());
        data.setValue(row, 1, dataValue);
        if (trend != null) {
          data.setValue(row, 2, trend);
        }
        row++;
//      }
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

  private com.google.gwt.visualization.client.visualizations.ScatterChart.Options 
    createScatterChartOptions(String title, String xTitle, String yTitle, String barColor, 
        double min, double max) {
    ScatterChart.Options options = ScatterChart.Options.create();
    options.setWidth(CHART_WIDTH);
    options.setHeight(CHART_HEIGHT);
    options.setEnableTooltip(true);

    options.setMin(min);
    options.setMax(max);
    options.setTitle(title);
    options.setLegend(LegendPosition.BOTTOM);
    options.setShowCategories(false);
    options.setTitleX(xTitle);
    options.setTitleY(yTitle);
    options.setColors(barColor, "#660000");
    options.setBorderColor("black");
    options.setPointSize(3);
    return options;

  }

  private static com.google.gwt.visualization.client.visualizations.LineChart.Options 
      createLineChartOptions(String title, String xTitle, String yTitle, String barColor, 
          double min, double max) {
    LineChart.Options options = LineChart.Options.create();
    options.setWidth(CHART_WIDTH);
    options.setHeight(CHART_HEIGHT);
    options.setEnableTooltip(true);

    options.setMin(min);
    options.setMax(max);
    options.setPointSize(3);
//    options.setLineSize(2);
    options.setTitle(title);
    options.setLegend(LegendPosition.BOTTOM);
    options.setShowCategories(true);
    options.setTitleX(xTitle);
    options.setTitleY(yTitle);
    options.setColors(barColor, "#660000");
    options.setBorderColor("black");
    return options;

  }

  private Options createOptions(String title, String xTitle, String yTitle, String barColor) {
    Options options = Options.create();
    options.setWidth(CHART_WIDTH);
    options.setHeight(CHART_HEIGHT);
    options.setEnableTooltip(true);

    options.setMin(0.0);
    options.setTitle(title);
    options.setLegend(LegendPosition.NONE);
    options.setShowCategories(true);
    options.setTitleX(xTitle);
    options.setTitleY(yTitle);
    options.setColors(barColor);
    options.setBorderColor("black");
    return options;
  }

  public List<Widget> getCharts() {
    return charts;
  }
}
