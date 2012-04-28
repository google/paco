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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class WordCloud {

  public static final List<String> DEFAULT_STOP_WORDS = ImmutableList.of("a", "an", "and", "is", 
      "or", "the", "with", "on", "for", "about", "of", "to");
  
  public static final int MIN_UNIT_SIZE = 1;
  public static final int MAX_UNIT_SIZE = 7;
  public static final int RANGE_UNIT_SIZE = MAX_UNIT_SIZE - MIN_UNIT_SIZE;

  private List<String> entries;
  
  public WordCloud(List<String> entries) {
    this.entries = entries;
  }
  
  public static class WordWeight {
    public String word;
    public int weight;

    public WordWeight(String word, int weight) {
      this.word = word;
      this.weight = weight;
    }
  }
  
  
  public List<WordWeight> getWordsWithWeights() {
    int minFreq = 999999;
    int maxFreq = 0;
    Map<String, Integer> wordFrequencies = getFrequenciesOfWords();
    for (String word : wordFrequencies.keySet()) {
      int freq = wordFrequencies.get(word);
      minFreq = Math.min(minFreq, freq);
      maxFreq = Math.max(maxFreq, freq);
    }
    
    int range = maxFreq - minFreq;
    float bucketSize = range / 7;
    bucketSize = Math.max(1, bucketSize);
    
    List<WordWeight> wordWeights = Lists.newArrayList();
    for (String word : wordFrequencies.keySet()) {
      int freq = wordFrequencies.get(word);
      
//      int size = MIN_UNIT_SIZE + Math.round( (float)freq / (float)buckets);
      int size = MIN_UNIT_SIZE + Math.round( freq / bucketSize);
      wordWeights.add(new WordWeight(word, size));
    }
    return wordWeights;
  }


  private Map<String, Integer> getFrequenciesOfWords() {
    Map<String, Integer> freqs = new HashMap<String, Integer>();
    for (String entry : entries) {
      entry= entry.trim();
      String[] words = entry.split(" ");
      for(int i=0; i < words.length; i++) {
        String word = words[i].toLowerCase();
        if (DEFAULT_STOP_WORDS.contains(word)) {
          continue;
        }
        Integer tokenCount = freqs.get(word);
        if (tokenCount == null) {
          freqs.put(word, 1);
        } else {
          freqs.put(word, tokenCount + 1);
        }
      }          
    }
    return freqs;
  }
}
