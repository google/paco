package com.google.android.apps.paco;

import java.util.List;

import android.test.AndroidTestCase;

public class PersistentAutocompleteDictionaryTest extends AndroidTestCase {

  public void testSplitterForStorageEasyCase() {
    assertEquals(2, new PersistentAutocompleteDictionary(getContext()).splitOnLastColon("hello:1").size());
    assertEqualParts("hello", "1", new PersistentAutocompleteDictionary(getContext()).splitOnLastColon("hello:1"));
  }


  public void testSplitterForStorageWithColonInAutocompleteText() {
    assertEquals(2, new PersistentAutocompleteDictionary(getContext()).splitOnLastColon("hello:1").size());
    assertEqualParts("list of stuff:one, two, three", "1", new PersistentAutocompleteDictionary(getContext()).splitOnLastColon("list of stuff:one, two, three:1"));
  }

  private void assertEqualParts(String expectedAutocompleteword, String expectedFrequency, List<String> splitOnLastColon) {
    assertEquals(expectedAutocompleteword, splitOnLastColon.get(0));
    assertEquals(expectedFrequency, splitOnLastColon.get(1));
  }

}
