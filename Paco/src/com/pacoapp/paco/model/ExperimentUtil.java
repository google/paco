package com.pacoapp.paco.model;

import java.util.Arrays;
import java.util.List;

public class ExperimentUtil {
 
  /**
   * 
   * @param inputString
   *          contains all the columns names and with some sql key words
   *          depending upon the user query
   * @return the list of column names
   */
  public static List<String> aggregateExtractedColNames(String inputString) {
    // any non word character or the words 'and' 'or' 'is' 'not' get replaced
    // with blank
    inputString = inputString.replaceAll("\\W+", " ");
    // replace logical operators and other key words
    inputString = inputString.replaceAll(" and | or | is | not | asc | desc | asc| desc", " ");
    // multiple blank spaces get truncated to single blank space
    inputString = inputString.replaceAll("( )+", " ").trim();
    String[] out = inputString.split(" ");
    return Arrays.asList(out);
  }
}
