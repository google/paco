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

import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.InputDAO;

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

//  private MapWidget map;
//  private Map<EventDAO, Marker> markers = com.google.common.collect.Maps.newHashMap();
  private DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy/MM/dd HH:mm:ssZ");
//  private static final LatLng google = LatLng.newInstance(37.420769, -122.085854);

  public ChartPanel(InputDAO input, List<EventDAO> eventList) {
    this.input = input;
    this.data = eventList;

    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    initWidget(verticalPanel);

    String questionText = input.getText();
    Label inputTextLabel = new Label(questionText);
    inputTextLabel.setStyleName("paco-HTML");
    verticalPanel.add(inputTextLabel);

    ChartOMundo cm = new ChartOMundo();
    Class dataTypeOf = getSampleDataType(cm);
    if (input.getResponseType().equals(InputDAO.OPEN_TEXT) &&
        dataTypeOf.equals(DEFAULT_DATA_CLASS)) {
      verticalPanel.add(cm.createWordCloud("", eventList, input.getName()));
    } else if (input.getResponseType().equals(InputDAO.PHOTO)) {
      verticalPanel.add(createPhotoSlider());
    } else if (input.getResponseType().equals(InputDAO.LOCATION)) {
      verticalPanel.add(renderEventsOnMap());
    } else if (input.getResponseType().equals(InputDAO.LIST)) {
      verticalPanel.add(cm.createBarChartForList(eventList, "", input.getName(),
          input.getListChoices(), input.getMultiselect()));
    } else if (input.getResponseType().equals(InputDAO.LIKERT)) {
      verticalPanel.add(cm.createBarChartForList(eventList, "", input.getName(),
          getLikertCategories(), false));
    } else if (input.getResponseType().equals(InputDAO.LIKERT_SMILEYS)) {
      verticalPanel.add(cm.createBarChartForList(eventList, "", input.getName(),
      getLikertSmileyCategories(), false));
    } else {
      verticalPanel.add(cm.createLineChart(eventList, "", input.getName()));
    }
  }

  /**
   * Retrieve the labels for a likert scale.
   *
   * @return List of labels for choices in likert scale.
   */
  private String[] getLikertCategories() {
    String[] choices = new String[input.getLikertSteps()];
    if (input.getLeftSideLabel() != null) {
      choices[0] = input.getLeftSideLabel();
    } 
    choices[0] = choices[0] + " (1)";
    if (input.getRightSideLabel() != null) {
      choices[input.getLikertSteps() - 1] = input.getRightSideLabel();
    }
    choices[input.getLikertSteps() - 1] = choices[input.getLikertSteps() - 1] 
                                                   + " (" + (input.getLikertSteps()) + ")"; 
    for (int i=1;i < (input.getLikertSteps() - 1); i++) {
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

        HTML picture = new HTML("<div style=\"text-align:center;margin-left:2;margin-right:2;\">"
            + "<img height=\"375\" src=\"data:image/jpg;base64," 
            + blobData 
            + "\"><br><b>" + event.getWho() + "</b><br><b>" + formatter.format(event.getResponseTime()) + "</b>"
            + "<br><b>" + formatter.format(event.getScheduledTime()) + "</b>"
            +"</div>");
        horizontalPanel.add(picture);
      }
    }
    return photosPanel;
  }

  /**
   * Create a Map Widget of Lat/Lon data.
   */
  private void createMap() {
//    
//    map = new MapWidget(google, 11);
//    map.setSize("800px", "600px");
//    
//    // Add some controls for the zoom level
//    map.addControl(new LargeMapControl());
//    map.addControl(new MapTypeControl());
//    map.addControl(new ScaleControl());
  }

  private Widget renderEventsOnMap() {
	  return null;
//    markers.clear();
//    createMap();
//    LatLngBounds bounds = LatLngBounds.newInstance();
//    map.setCenter(bounds.getCenter());
//    map.setZoomLevel(map.getBoundsZoomLevel(bounds));
//    for (final EventDAO eventRating : data) {
//      String latLon = eventRating.getWhatByKey(input.getName());
//      if (latLon == null || latLon.length() == 0) {
//        continue;
//      }
//      String[] splits = latLon.split(",");
//      if (splits == null || splits.length != 2) {
//        continue;
//      }
//      try {
//        double latitude = Double.parseDouble(splits[0]);
//        double longitude = Double.parseDouble(splits[1]);
//
//        MarkerOptions markerOptions = MarkerOptions.newInstance();
//        markerOptions.setTitle(eventRating.getWhatString());
//        final Marker marker = new Marker(LatLng.newInstance(latitude, longitude), markerOptions);
//        bounds.extend(marker.getPoint());
//        marker.addMarkerClickHandler(new MarkerClickHandler() {
//
//          @Override
//          public void onClick(MarkerClickEvent event) {
//            openInfoWindowForMarker(eventRating, marker);
//          }
//
//        });
//        markers.put(eventRating, marker);
//        map.addOverlay(marker);
//      } catch (NumberFormatException nfe) {
//      }
//    }
//    
//    
//    map.setCenter(bounds.getCenter());
//    map.setZoomLevel(map.getBoundsZoomLevel(bounds));
//    map.checkResizeAndCenter();
//    return map;
  }

//  private void openInfoWindowForMarker(final EventDAO eventRating, final Marker marker) {
//    map.getInfoWindow().open(marker.getPoint(), createInfoWindowForEventRating(eventRating));
//  }


//  private InfoWindowContent createInfoWindowForEventRating(final EventDAO eventRating) {
//    return new InfoWindowContent(
//        "What: " + eventRating.getWhatString() + "<br/>Who: " + eventRating.getWho() + "<br/>When: "
//            + formatter.format(eventRating.getWhen()));
//  }

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

}
