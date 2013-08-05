package com.google.android.apps.paco;

import java.util.List;

import android.test.AndroidTestCase;

public class PersistentAutocompleteDictionaryTest extends AndroidTestCase {

  public void testSplitterForStorageEasyCase() {
    List<String> autocompleteEntry = new PersistentAutocompleteDictionary(getContext()).splitOnLastColon("hello:1");
    assertEquals(2, autocompleteEntry.size());
    assertEqualParts("hello", "1", autocompleteEntry);
  }


  public void testSplitterForStorageWithColonInAutocompleteText() {
    List<String> autocompleteEntry = new PersistentAutocompleteDictionary(getContext()).splitOnLastColon("list of stuff:one, two, three:1");
    assertEquals(2, autocompleteEntry.size());
    assertEqualParts("list of stuff:one, two, three", "1", autocompleteEntry);
  }

  public void testSplitterForEmptyCase() throws Exception {
    List<String> autocompletEntry = new PersistentAutocompleteDictionary(getContext()).splitOnLastColon(":1");
    assertEquals(2, autocompletEntry.size());
    assertEqualParts("", "1", autocompletEntry);
  }

  private void assertEqualParts(String expectedAutocompleteword, String expectedFrequency, List<String> splitOnLastColon) {
    assertEquals(expectedAutocompleteword, splitOnLastColon.get(0));
    assertEquals(expectedFrequency, splitOnLastColon.get(1));
  }

}
