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
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.MapEventType;
import com.google.gwt.maps.client.events.MapHandlerRegistration;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.resize.ResizeMapEvent;
import com.google.gwt.maps.client.events.resize.ResizeMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Component for holding an individual chart for an Input's responses.
 *
 * @author Bob Evans
 *
 */
public class ChartPanel extends Composite {

  private static final Class<String> DEFAULT_DATA_CLASS = String.class;
  private InputDAO input;
  private List<EventDAO> data;

  private MapWidget map;
  private Map<EventDAO, Marker> markers = com.google.common.collect.Maps.newHashMap();
  private DateTimeFormat formatter = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);
  private int mapWidth;
  private int mapHeight;
  private boolean showLabel;
  private VerticalPanel rootPanel;
  private static final LatLng google = LatLng.newInstance(37.420769, -122.085854);

  public ChartPanel(InputDAO input, List<EventDAO> eventList, int mapWidth, int mapHeight, boolean showLabel) {
    this.input = input;
    this.data = eventList;
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;
    this.showLabel = showLabel;

    rootPanel = new VerticalPanel();
    rootPanel.setSpacing(2);
    initWidget(rootPanel);
    rootPanel.clear();

    if (showLabel) {
      String questionText = input.getText();
      Label inputTextLabel = new Label(questionText);
      inputTextLabel.setStyleName("paco-HTML");
      rootPanel.add(inputTextLabel);
    }

    ChartOMundo cm = new ChartOMundo();
    Class dataTypeOf = getSampleDataType(cm);
    if (input.getResponseType().equals(InputDAO.OPEN_TEXT) &&
        dataTypeOf.equals(DEFAULT_DATA_CLASS)) {
      rootPanel.add(cm.createWordCloud("", eventList, input.getName()));
    } else if (input.getResponseType().equals(InputDAO.PHOTO)) {
      rootPanel.add(createPhotoSlider());
    } else if (input.getResponseType().equals(InputDAO.LOCATION)) {
      renderEventsOnMap();
      //rootPanel.add();
    } else if (input.getResponseType().equals(InputDAO.LIST)) {
      rootPanel.add(cm.createBarChartForList(eventList, "", input.getName(),
          input.getListChoices(), input.getMultiselect()));
    } else if (input.getResponseType().equals(InputDAO.LIKERT)) {
      rootPanel.add(cm.createBarChartForList(eventList, "", input.getName(),
          getLikertCategories(), false));
    } else if (input.getResponseType().equals(InputDAO.LIKERT_SMILEYS)) {
      rootPanel.add(cm.createBarChartForList(eventList, "", input.getName(),
      getLikertSmileyCategories(), false));
    } else {
      rootPanel.add(cm.createLineChart(eventList, "", input.getName()));
    }
  }

  public ChartPanel(InputDAO input, List<EventDAO> events) {
    this(input, events, 800, 600, true);
  }

  /**
   * Retrieve the labels for a likert scale.
   *
   * @return List of labels for choices in likert scale.
   */
  private String[] getLikertCategories() {
    Integer likertSteps = input.getLikertSteps();
    if (likertSteps == null) {
      likertSteps = 0;
    }
    String[] choices = new String[likertSteps];
    if (!Strings.isNullOrEmpty(input.getLeftSideLabel())) {
      choices[0] = input.getLeftSideLabel();
    }
    choices[0] = (choices[0] != null ? choices[0] + " " : "") + "(1)";
    if (!Strings.isNullOrEmpty(input.getRightSideLabel())) {
      choices[likertSteps - 1] = input.getRightSideLabel();
    }
    choices[likertSteps - 1] = (choices[likertSteps - 1] != null ? choices[likertSteps - 1] + " " : "") + " (" + likertSteps + ")";
    for (int i=1;i < (likertSteps - 1); i++) {
      choices[i] = "(" + (i + 1) + ")";
    }
    return choices;
  }

  private String[] getLikertSmileyCategories() {
    final int smiley_count = 5;
    String[] choices = new String[smiley_count];
    for (int i=0;i < 5; i++) {
      choices[i] = Integer.toString(i + 1);
    }
    return choices;
  }

  /**
   * Show photos in a side-sliding gallery.
   *
   * @return The photo gallery widget
   */
  private Widget createPhotoSlider() {
    // I want a horizontally scrolling panel that shows photos in date order
    //
    //      / photo1 | photo2 | photo3 \
    //     (                            )
    //      \  date  |  date  |  date  /
    //
    ScrollPanel photosPanel = new ScrollPanel();
    photosPanel.setHeight("480");
    photosPanel.setWidth("800");

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setHeight("450");
    photosPanel.add(horizontalPanel);
    for (EventDAO event : data) {
      String[] blobs = event.getBlobs();
      if (blobs == null || blobs.length == 0) {
        continue;
      }
      for (int i = 0; i < blobs.length; i++) {
        String blobData = blobs[i];
        if (blobData.length() == 0 || blobData.equals("==")) {
          continue;
        }

        String formattedResponseTime = formatTime(event.getResponseTime());
        String formattedScheduledTime = formatTime(event.getScheduledTime());
        HTML picture = new HTML("<div style=\"text-align:center;margin-left:2;margin-right:2;\">"
            + "<img height=\"375\" src=\"data:image/jpg;base64,"
            + blobData
            + "\"><br><b>" + event.getWho() + "</b><br><b>" + formattedResponseTime + "</b>"
            + "<br><b>" + formattedScheduledTime + "</b>"
            +"</div>");
        horizontalPanel.add(picture);
      }
    }
    return photosPanel;
  }

  private String formatTime(Date time) {
    if (time == null) {
      return "";
    }
    return formatter.format(time);
  }

  /**
   * Create a Map Widget of Lat/Lon data.
   */
  private void createMap() {

    MapOptions mapOptions = MapOptions.newInstance();
    mapOptions.setCenter(google);
    mapOptions.setZoom(4);
    mapOptions.setMapTypeId(MapTypeId.ROADMAP);
    map = new MapWidget(mapOptions);
    rootPanel.add(map);
    map.setSize(mapWidth + "px", mapHeight + "px");
  }

  private Widget renderEventsOnMap() {
    markers.clear();
    createMap();
    final LatLngBounds bounds = LatLngBounds.newInstance(google, google);
    for (final EventDAO event : data) {
      String latLon = event.getWhatByKey(input.getName());
      if (latLon == null || latLon.length() == 0) {
        continue;
      }
      String[] splits = latLon.split(",");
      if (splits == null || splits.length != 2) {
        continue;
      }
      try {
        double latitude = Double.parseDouble(splits[0]);
        double longitude = Double.parseDouble(splits[1]);

        MarkerOptions markerOptions = MarkerOptions.newInstance();
        markerOptions.setMap(map);
        markerOptions.setTitle(event.getWhatString());
        LatLng newInstance = LatLng.newInstance(latitude, longitude);
        final Marker marker = Marker.newInstance(markerOptions);
        marker.setPosition(newInstance);
        bounds.union(LatLngBounds.newInstance(marker.getPosition(), marker.getPosition()));
        marker.addClickHandler(new ClickMapHandler() {
          @Override
          public void onEvent(ClickMapEvent mapEvent) {
            openInfoWindowForMarker(event, marker);
          }
        });
        markers.put(event, marker);
      } catch (NumberFormatException nfe) {
      }
    }


    map.fitBounds(bounds);

    final LatLng oldCenter = map.getCenter();
    final int oldZoom = map.getZoom();
    map.addResizeHandler(new ResizeMapHandler() {

      @Override
      public void onEvent(ResizeMapEvent event) {
        map.setCenter(oldCenter);
        map.setZoom(oldZoom);
        map.fitBounds(bounds);
      }
    });
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
          MapHandlerRegistration.trigger(map, MapEventType.RESIZE);
          GWT.log("Window has been resized!");
      }
  });
    return map;
  }

  private void openInfoWindowForMarker(final EventDAO eventRating, final Marker marker) {
    InfoWindowOptions options = InfoWindowOptions.newInstance();
    options.setContent(createInfoWindowForEvent(eventRating));
    InfoWindow iw = InfoWindow.newInstance(options);
    iw.open(map, marker);
  }


  private HTML createInfoWindowForEvent(final EventDAO event) {
    return new HTML(
        "What: " + event.getWhatString() + "<br/>Who: " + event.getWho() + "<br/>When: "
            + formatter.format(event.getResponseTime()));
  }


  /**
   * Sample the data to figure out what type it is.
   *
   * @param cm The ChartoMundo chart maker object.
   * @return
   */
  private Class getSampleDataType(ChartOMundo cm) {
    String inputName = input.getName();
    if (inputName == null || data.size() == 0) {
      return DEFAULT_DATA_CLASS;
    }

    EventDAO eventDAO = data.get(0);
    String answer = eventDAO.getWhatByKey(inputName);
    if (answer == null) {
      return DEFAULT_DATA_CLASS;
    }

    Class dataTypeOfFirstEntry = cm.getDataTypeOf(answer);

    if (data.size() == 1) {
      return dataTypeOfFirstEntry;
    }
    EventDAO eventDAO2 = data.get(1);
    String answer2 = eventDAO2.getWhatByKey(inputName);
    if (answer2 == null) {
      return DEFAULT_DATA_CLASS;
    }
    if (cm.getDataTypeOf(answer2) == dataTypeOfFirstEntry) {
      return dataTypeOfFirstEntry;
    }
    return DEFAULT_DATA_CLASS;
  }

  @Override
  protected void onLoad() {
    // TODO Auto-generated method stub
    super.onLoad();
    MapHandlerRegistration.trigger(map, MapEventType.RESIZE);
  }



}
