/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.Response;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.TextInput;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Component for holding an individual chart for an Input's responses.
 *
 * @author Bob Evans
 *
 */
public class ChartPanel extends Composite {

  private static final Class<String> DEFAULT_DATA_CLASS = String.class;
  private Input input;
  private List<Response> responses;

  // private MapWidget map;
  // private Map<Response, Marker> markers = com.google.common.collect.Maps.newHashMap();
  private DateTimeFormat formatter = DateTimeFormat.getFormat(TimeUtil.DATETIME_FORMAT);

  // private static final LatLng google = LatLng.newInstance(37.420769, -122.085854);

  public ChartPanel(Input input, List<Response> responses) {
    this.input = input;
    this.responses = responses;

    VerticalPanel verticalPanel = new VerticalPanel();
    verticalPanel.setSpacing(2);
    initWidget(verticalPanel);

    ChartOMundo cm = new ChartOMundo();
    Class dataTypeOf = getSampleDataType(cm);

    if (input.getType().equals(Input.TEXT) && dataTypeOf.equals(DEFAULT_DATA_CLASS)) {
      TextInput textInput = (TextInput) input;
      Label inputTextLabel = new Label(textInput.getQuestion());
      inputTextLabel.setStyleName("paco-HTML");
      verticalPanel.add(inputTextLabel);
      verticalPanel.add(cm.createWordCloud("", responses, textInput.getName()));
    } else if (input.getType().equals(Input.PHOTO)) {
      verticalPanel.add(createPhotoSlider());
    } else if (input.getType().equals(Input.LOCATION)) {
      verticalPanel.add(renderResponsesOnMap());
    } else if (input.getType().equals(Input.LIST)) {
      ListInput listInput = (ListInput) input;
      Label inputTextLabel = new Label(listInput.getQuestion());
      inputTextLabel.setStyleName("paco-HTML");
      verticalPanel.add(inputTextLabel);
      verticalPanel.add(cm.createBarChartForList(responses, "", listInput.getName(),
          listInput.getChoices().toArray(new String[listInput.getChoices().size()]),
          listInput.isMultiselect()));
    } else if (input.getType().equals(Input.LIKERT)) {
      LikertInput likertInput = (LikertInput) input;

      verticalPanel.add(cm.createBarChartForList(
          responses, "", likertInput.getName(), getLikertCategories(likertInput), false));
    } else {
      verticalPanel.add(cm.createLineChart(responses, "", input.getName()));
    }
  }

  /**
   * Retrieve the labels for a likert scale.
   *
   * @return List of labels for choices in likert scale.
   */
  private String[] getLikertCategories(LikertInput likertInput) {
    return likertInput.getLabels().toArray(new String[likertInput.getLabels().size()]);
  }

  /**
   * Show photos in a side-sliding gallery.
   *
   * @return The photo gallery widget
   */
  private Widget createPhotoSlider() {
    // I want a horizontally scrolling panel that shows photos in date order
    //
    // / photo1 | photo2 | photo3 \
    // ( )
    // \ date | date | date /
    //
    ScrollPanel photosPanel = new ScrollPanel();
    photosPanel.setHeight("480");
    photosPanel.setWidth("800");

    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setHeight("450");
    photosPanel.add(horizontalPanel);
    for (Response response : responses) {
      for (String key : response.getOutputs().keySet()) {
        String blobData = response.getOutputByKey(key);

        if (blobData == null || blobData.startsWith("http://") == false) {
          continue;
        }

        HTML picture = new HTML("<div style=\"text-align:center;margin-left:2;margin-right:2;\">"
            + "<img height=\"375\" src=\"" + blobData + "\"><br><b>" + response.getSubject()
            + "</b><br><b>" + formatter.format(response.getResponseTime()) + "</b>" + "<br><b>"
            + formatter.format(response.getSignalTime()) + "</b>" + "</div>");
        horizontalPanel.add(picture);
      }
    }
    return photosPanel;
  }

  private Widget renderResponsesOnMap() {
    return null;
  }

  /**
   * Sample the data to figure out what type it is.
   *
   * @param cm The ChartoMundo chart maker object.
   * @return
   */
  private Class getSampleDataType(ChartOMundo cm) {
    String inputName = input.getName();
    if (inputName == null || responses.size() == 0) {
      return DEFAULT_DATA_CLASS;
    }

    Response response = responses.get(0);
    String answer = response.getOutputByKey(inputName);
    if (answer == null) {
      return DEFAULT_DATA_CLASS;
    }

    Class dataTypeOfFirstEntry = cm.getDataTypeOf(answer);

    if (responses.size() == 1) {
      return dataTypeOfFirstEntry;
    }
    Response response2 = responses.get(1);
    String answer2 = response2.getOutputByKey(inputName);
    if (answer2 == null) {
      return DEFAULT_DATA_CLASS;
    }
    if (cm.getDataTypeOf(answer2) == dataTypeOfFirstEntry) {
      return dataTypeOfFirstEntry;
    }
    return DEFAULT_DATA_CLASS;
  }

}
