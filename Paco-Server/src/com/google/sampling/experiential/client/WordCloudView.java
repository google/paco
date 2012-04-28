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

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.sampling.experiential.shared.WordCloud;
import com.google.sampling.experiential.shared.WordCloud.WordWeight;

public class WordCloudView extends Composite {

  private AbsolutePanel absPanel;

  public WordCloudView(String title, List<String> entries) {
    absPanel = new AbsolutePanel();
    initWidget(absPanel);

    absPanel.setStyleName("word-cloud");
    absPanel.setHeight("300");
    absPanel.setWidth("500");
    draw(entries);
  }

  private void draw(List<String> entries) {
    List<WordWeight> wordWeights = new WordCloud(entries).getWordsWithWeights();

    StringBuilder buf = new StringBuilder();
    for (WordWeight wordWeight : wordWeights) {
      buf.append("<span class=\"");
      buf.append("word-cloud-" + wordWeight.weight);
      buf.append("\">");
      buf.append(wordWeight.word);
      buf.append("</span> \n");

    }
    HTML wordpanel = new HTML(buf.toString());
    absPanel.add(wordpanel);
  }


}
