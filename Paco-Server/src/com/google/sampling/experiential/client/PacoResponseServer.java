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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.ColumnChart;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.ScatterChart;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.LoginInfo;
import com.google.sampling.experiential.shared.LoginService;
import com.google.sampling.experiential.shared.LoginServiceAsync;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.MapServiceAsync;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * An alternate entry point that focuses on a search interface and
 * table output of Events.
 * 
 * @author Bob Evans
 *
 */
public class PacoResponseServer implements EntryPoint {

  static final Images images = (Images) GWT.create(Images.class);

  private final class InputHandler implements ClickHandler, KeyUpHandler {

    @Override
    public void onClick(ClickEvent event) {
      drawResults();
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        drawResults();
      }
    }
  }

  // private static final LatLng google = LatLng.newInstance(37.420769,
  // -122.085854);
  private DateTimeFormat formatter = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);


  private MapServiceAsync mapService = GWT.create(MapService.class);
  // private MapWidget map;
  List<Event> responses;
  // private ScrollPanel visualizationPanel;
  private VerticalPanel responsesPanel;
  // private Map<Response, Marker> markers = Maps.newHashMap();
  TextBox tagList;
  private VerticalPanel logPanel;
  FlowPanel chartPanel;
  private VerticalPanel rootPanel;

  private void log(String msg) {
    logPanel.add(new HTML(msg));
  }

  private LoginInfo loginInfo = null;
  private VerticalPanel loginPanel = new VerticalPanel();
  private Label loginLabel =
      new Label("Please sign in to your Google Account " + "to access the application.");
  private Anchor signInLink = new Anchor("Sign In");
  private Anchor signOutLink = new Anchor("Sign Out");


  public void onModuleLoad() {
    // Check login status using login service.
    LoginServiceAsync loginService = GWT.create(LoginService.class);
    loginService.login(GWT.getHostPageBaseURL() +"PacoResponseServer.html", new AsyncCallback<LoginInfo>() {
      public void onFailure(Throwable error) {
      }

      public void onSuccess(LoginInfo result) {
        loginInfo = result;
        if (loginInfo.isLoggedIn()) {
          createHomePage();
        } else {
          loadLogin();
        }
      }
    });

  }

  private void loadLogin() {
    // Assemble login panel.
    signInLink.setHref(loginInfo.getLoginUrl());
    loginPanel.add(loginLabel);
    loginPanel.add(signInLink);
    RootPanel.get().add(loginPanel);
  }

  private void createHomePage() {
    rootPanel = new VerticalPanel();
    // rootPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    rootPanel.setWidth("100%");
    RootPanel.get().add(rootPanel);

    VerticalPanel searchPanel = new VerticalPanel();
    searchPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    searchPanel.setSpacing(5);
    signOutLink.setHref(loginInfo.getLogoutUrl());
    rootPanel.add(signOutLink);
    Image pacoLogo = new Image(images.pacoFaceLogo());
    searchPanel.add(pacoLogo);
    searchPanel.add(createSearchPanel());

    rootPanel.add(searchPanel);
    rootPanel.add(new HTML("<h3><a href=\"/paco.apk\">Download the Paco Android Client</a></h3>"));

    rootPanel.add(createChartPanel());

    HorizontalPanel mainPanel = createMainpanel();
    rootPanel.add(mainPanel);
    createResponsesPanel(mainPanel);

    logPanel = new VerticalPanel();
    logPanel.setBorderWidth(2);
    rootPanel.add(logPanel);

    createCallbackForGviz();
  }

  private FlowPanel createChartPanel() {
    chartPanel = new FlowPanel();
    chartPanel.setStylePrimaryName("left");
    // chartPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    return chartPanel;
  }

  private VerticalPanel createSearchPanel() {
    final VerticalPanel filterPanel = new VerticalPanel();
    filterPanel.add(new HTMLPanel("<b>Responses (empty for all):</b> "));
    tagList = new TextBox();
    tagList.setWidth("45em");
    filterPanel.add(tagList);
    filterPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);


    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    filterPanel.add(buttonPanel);
    Button cafeButton = new Button("Paco Search");
    buttonPanel.add(cafeButton);
    InputHandler cafeHandler = new InputHandler();
    cafeButton.addClickHandler(cafeHandler);
    tagList.addKeyUpHandler(cafeHandler);

    Button postButton = new Button("Store Data");
    buttonPanel.add(postButton);
    postButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        Window.Location.assign("/PostResponse.html");

      }
    });
    return filterPanel;
  }

  private HorizontalPanel createMainpanel() {
    HorizontalPanel mainPanel = new HorizontalPanel();
    // mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    mainPanel.setSpacing(5);
    mainPanel.setWidth("100%");
    return mainPanel;
  }

  private void createResponsesPanel(HorizontalPanel mainPanel) {
    ScrollPanel responsesScrollPanel = new ScrollPanel();

    responsesScrollPanel.setHeight("75em");
    mainPanel.add(responsesScrollPanel);

    responsesPanel = new VerticalPanel();
    responsesPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    responsesScrollPanel.add(responsesPanel);
  }

  private void createCallbackForGviz() {
    // Create a callback to be called when the visualization API
    // has been loaded.
    Runnable onLoadCallback = new Runnable() {
      public void run() {
      }
    };
    // Load the visualization api, passing the onLoadCallback to be called
    // when loading is done.
    VisualizationUtils.loadVisualizationApi(onLoadCallback, ColumnChart.PACKAGE, Table.PACKAGE,
        LineChart.PACKAGE, ScatterChart.PACKAGE);
  }

  // private void createMapWidget() {
  // map = new MapWidget(google, 11);
  // map.setSize("800px", "600px");
  //
  // // Add some controls for the zoom level
  // map.addControl(new LargeMapControl());
  // map.addControl(new MapTypeControl());
  // map.addControl(new ScaleControl());
  // }

  private void renderResponsesOnList(List<Event> responses) {
    DataTable data = DataTable.create();
    data.addRows(responses.size());
    data.addColumn(ColumnType.DATE, "When");
    data.addColumn(ColumnType.STRING, "Who");
    data.addColumn(ColumnType.STRING, "Experiment");
    data.addColumn(ColumnType.STRING, "What");
    int row = 0;
    for (Event response : responses) {
      data.setValue(row, 0, response.getCreateTime());
      data.setValue(row, 1, response.getSubject());
      data.setValue(row, 2, response.getExperimentId());
      data.setValue(row, 3, response.getOutputsString());
      row++;
    }

    final Table meetingTable = new Table(data, createTableOptions());
    responsesPanel.clear();
    responsesPanel.add(meetingTable);
  }

  private com.google.gwt.visualization.client.visualizations.Table.Options createTableOptions() {
    com.google.gwt.visualization.client.visualizations.Table.Options options =
        com.google.gwt.visualization.client.visualizations.Table.Options.create();
    options.setShowRowNumber(false);
    options.setAllowHtml(true);
    return options;
  }

  public static Set<String> splitTags(String text) {
    String[] tags = text.split(" ");
    return new HashSet<String>(Arrays.asList(tags));
  }

  private void drawResults() {

    chartPanel.clear();
    responsesPanel.clear();

    AsyncCallback<List<Event>> callback = new AsyncCallback<List<Event>>() {

      @Override
      public void onFailure(Throwable caught) {

      }

      @Override
      public void onSuccess(List<Event> responses) {
        // create column chart with responses;
        log("Got results: size: " + responses.size());
        if (responses.size() == 0) {
          chartPanel.add(new HTML("<h1>No results for your query.</h1>"));
          return;
        }
        renderResponsesOnList(responses);
        // Window.alert("Starting ChartOMundo");
        ChartOMundo chartMaker = new ChartOMundo();
        List<Widget> charts = chartMaker.autoChart(tagList.getText(), responses);
        // chartMaker.getCharts();
        // Window.alert("Got back _"+charts.size()+"_ charts!");
        for (Widget chart : charts) {
          chartPanel.add(chart);
        }
      }

      private List<List<String>> parseKeyValuePairsFromQuery() {
        String[] keySplit = tagList.getText().split("\\:");
        List<List<String>> kvPairs = Lists.newArrayList();
        for (int i = 0; i < keySplit.length; i++) {
          String kv = keySplit[i];
          int indexOfEqual = kv.indexOf("=");
          if (indexOfEqual == -1) {
            kvPairs.add(Lists.newArrayList(kv, null));
          } else {
            String key = kv.substring(0, indexOfEqual);
            String value = kv.substring(indexOfEqual + 1);
            kvPairs.add(Lists.newArrayList(key, value));
          }
        }
        return kvPairs;
      }

    };
    log("Calling server");
    String queryText = tagList.getText();
    int indexOfHisto = queryText.indexOf(":histo");
    if (indexOfHisto != -1) {
      queryText = queryText.replace(":histo", "");
    }
    mapService.mapWithTags(queryText, callback);
    log("called server");


  }



}
