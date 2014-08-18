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
package com.google.sampling.experiential.shared;

import com.google.sampling.experiential.shared.WordCloud;
import com.google.sampling.experiential.shared.WordCloud.WordWeight;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class WordCloudTest extends TestCase {


  public void testWordCloud() throws Exception {
    List<String> entries = new ArrayList<String>();
    entries.add("apple");
    WordCloud wc = new WordCloud(entries);
    List<WordWeight> wordsWithWeights = wc.getWordsWithWeights();
    assertEquals("apple", wordsWithWeights.get(0).word);
    assertEquals(2, wordsWithWeights.get(0).weight);
  }
}
