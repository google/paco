package com.google.android.apps.paco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.google.common.collect.Lists;

public class AutocompleteUsageFilteringArrayAdapter extends ArrayAdapter<String> {
  private AutocompleteDictionary autocompleteDictionary;
  private List<String> currentSuggestions;

  public AutocompleteUsageFilteringArrayAdapter(Context context, int viewResourceId, AutocompleteDictionary autocompleteDatabase) {
    super(context, viewResourceId, Lists.newArrayList(autocompleteDatabase.getWords()));
    this.autocompleteDictionary = autocompleteDatabase;
    this.currentSuggestions = new ArrayList<String>();
  }

  @Override
  public Filter getFilter() {
    return mostFrequentFilter;
  }

  Filter mostFrequentFilter = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      if (constraint != null) {
        currentSuggestions.clear();
        currentSuggestions = autocompleteDictionary.getMostFrequentSuggestionsFor(constraint);
        
        FilterResults filterResults = new FilterResults();
        filterResults.values = currentSuggestions;
        filterResults.count = currentSuggestions.size();
        return filterResults;
      } else {
        return new FilterResults();
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      ArrayList<String> filteredList = (ArrayList<String>) results.values;
      if (results != null && results.count > 0) {
        clear();
        for (String c : filteredList) {
          add(c);
        }
        notifyDataSetChanged();
      }
    }
  };

}