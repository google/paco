package com.google.android.apps.paco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

public class PersistentAutocompleteDictionary extends AutocompleteDictionary {

  private static final String AUTOCOMPLETE_DATA_FILE_NAME = "autocompleteData";

  private Context context;

  public PersistentAutocompleteDictionary(Context context) {
    super();
    this.context = context;
    ensureAutoCompleteDatabase();
  }

  protected void saveAutocompleteDictionary() {
    saveAutocompleteToDisk();
  }

  protected void loadAutocompleteDictionary() {
    autocompleteDatabase = loadAutocompleteDataFromDisk();
  }

  private Context getContext() {
    return context;
  }

  private void saveAutocompleteToDisk() {
    OutputStreamWriter f = null;
    try {
      f = new OutputStreamWriter(getContext().openFileOutput(AUTOCOMPLETE_DATA_FILE_NAME, getContext().MODE_PRIVATE));
      BufferedWriter buf = new BufferedWriter(f);
      f.write("version:2\n");
      for (String word : autocompleteDatabase.keySet()) {
        Integer frequency = autocompleteDatabase.get(word);
        f.write(word);
        f.write(":");
        f.write(Integer.toString(frequency));
        f.write("\n");
      }
      f.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (f != null) {
        try {
          f.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private Map<String, Integer> loadAutocompleteDataFromDisk() {
    Map<String, Integer> wordCounts = new ConcurrentHashMap<String, Integer>();
    BufferedReader buf = null;
    try {
      InputStream in = getContext().openFileInput(AUTOCOMPLETE_DATA_FILE_NAME);
      if (in != null) {
        buf = new BufferedReader(new InputStreamReader(in));
        String line = null;
        boolean firstLine = true;
        boolean v1 = false;
        while ((line = buf.readLine()) != null) {
          if (firstLine) {
            if (!line.startsWith("version")) {
              v1 = true;
            }
            firstLine = false;
          }
          if (v1) {
            wordCounts.put(line, 1);
          } else {
            List<String> splits = splitOnLastColon(line);
            String word = splits.get(0);
            if (word.equals("version")) {
              continue;
            }
            Integer frequency = Integer.parseInt(splits.get(1));
            wordCounts.put(word, frequency);
          }
        }
      }
    } catch (FileNotFoundException e) {
      Log.d(PacoConstants.TAG, "No autocomplete database found yet", e);
    } catch (IOException e) {
      Log.d(PacoConstants.TAG, "Could not talk to autocomplete database", e);
    } finally {
      try {
        if (buf != null) {
          buf.close();
        }
      } catch (IOException e) {
        // Not worth it, there is no recovery.
      }
    }
    return wordCounts;
  }

  @VisibleForTesting
  List<String> splitOnLastColon(String line) {
    int lastIndexOf = line.lastIndexOf(":");
    List<String> splits = Lists.newArrayList();
    if (lastIndexOf != -1) {
      splits.add(line.substring(0,lastIndexOf));
      splits.add(line.substring(lastIndexOf + 1));
    }
    return splits;
  }

}
