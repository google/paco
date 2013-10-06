package com.google.android.apps.paco;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public abstract class AutocompleteDictionary {

  protected Map<String, Integer> autocompleteDatabase;

  public AutocompleteDictionary() {
    super();
  }

  public void updateAutoCompleteDatabase(String responseText) {
    ensureAutoCompleteDatabase();
    
    addWordToAutocompleteDatabase(responseText, 2);
    Iterable<String> words = Splitter.on(" ").trimResults().split(responseText);
    for (String word : words) {
      addWordToAutocompleteDatabase(word, 1);
    }
    saveAutocompleteDictionary();
  }

  public List<String> getMostFrequentSuggestionsFor(CharSequence constraint) {
    List<Entry<String, Integer>> suggestionCandidates = Lists.newArrayList();
    for (Entry<String, Integer> wordEntry : autocompleteDatabase.entrySet()) {
      if (wordEntry.getKey().startsWith(constraint.toString().toLowerCase())) {
        suggestionCandidates.add(wordEntry);
      }
    }
    
    Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {
      @Override
      public int compare(Entry<String, Integer> lhs, Entry<String, Integer> rhs) {
        return rhs.getValue().compareTo(lhs.getValue());
      }
    };
    Collections.sort(suggestionCandidates, comparator);
    
    List<String> newSuggestions = Lists.newArrayList();
    for (Entry<String, Integer> entry : suggestionCandidates) {
      newSuggestions.add(entry.getKey());
    }
    return newSuggestions;
  }

  public Set<String> getWords() {
    ensureAutoCompleteDatabase();
    return autocompleteDatabase.keySet();
  }



  protected void ensureAutoCompleteDatabase() {
    if (autocompleteDatabase == null) {
      loadAutocompleteDictionary();
    }
  }

  protected void addWordToAutocompleteDatabase(String word, int weight) {
    word = word.toLowerCase();
    Integer frequency = autocompleteDatabase.get(word);
    if (frequency != null) {
      frequency += weight;
      autocompleteDatabase.put(word, frequency);
    } else {
      autocompleteDatabase.put(word, 1);
    }
  }

  protected abstract void saveAutocompleteDictionary();
  
  protected abstract void loadAutocompleteDictionary();

}