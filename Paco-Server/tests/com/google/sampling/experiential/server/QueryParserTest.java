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
package com.google.sampling.experiential.server;


import junit.framework.TestCase;

import java.util.List;

/**
 * param_string :: 'q=\'' keyvalue_pair_list '\''
 * keyvalue_pair_list :: EMPTY | keyvalue_pair (':' keyvalue_pair)*
 * EMPTY :: ''  
 * keyvalue_pair :: date_range | keyword ('=' value)+
 * date_range :: 'date_range=' date (-date)+
 * date :: [0-0]{4}[0-9]{2}[0-9]{2}
 * keyword :: builtin_keyword | app_specific_keyword
 * builtin_keyword :: 'who' | 'when' | 'where' | 'appId' | 'paco_version' 
 * app_specific_keyword :: [a-zA-Z_][0-9a-zA-Z_]*
 * value :: digit | word 
 * digit :: [0-9]+
 * word :: quoted_word | plain word
 * quoted_word :: quote plain_word quote
 * quote :: ' | "
 * plain_word :: [0-9a-zA-Z_ ]+
 *  
 * Examples:
 * 
 * q='restaurant'
 * q='restaurant='
 * q='restaurant=CafeMoma'
 * q='who=bobevans@google.com:weight'
 * q='restaurant=CafeMoma:date_range=20090831-20090902'
 * 
 * @author Bob Evans
 *
 */
public class QueryParserTest extends TestCase {
  
  
  
  private QueryParser queryParser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    queryParser = new QueryParser();
  }

  private void assertResultSize(int expectedSize, List<Query> result) {
    assertEquals(expectedSize, result.size());
  }  
  
  public void testParseEmpty() throws Exception {
    assertResultSize(0, queryParser.parse(""));    
  }

  public void testNullQuery() throws Exception {
    assertResultSize(0, queryParser.parse(null));
  }
  
  public void testWhoQ() throws Exception {
    List<Query> result = queryParser.parse("who");
    assertResultSize(1, result);
    assertEquals("who", result.get(0).getKey());
    assertNull(result.get(0).getValue());
  }
  
  public void testWhoWithRhs() throws Exception {
    List<Query> result = queryParser.parse("who=bobevans@google.com");
    assertResultSize(1, result);
    assertEquals("who", result.get(0).getKey());
    assertEquals("bobevans@google.com", result.get(0).getValue());
  }
  
  public void testWhoWithTwoQueries() throws Exception {
    List<Query> result = queryParser.parse("who=bobevans@google.com:weight");
    assertResultSize(2, result);
    assertEquals("who", result.get(0).getKey());
    assertEquals("bobevans@google.com", result.get(0).getValue());
    assertEquals("weight", result.get(1).getKey());
    assertNull(result.get(1).getValue());
  }

  public void testTwoQueriesNullValueSecondQuery() throws Exception {
    List<Query> result = queryParser.parse("who=bobevans@google.com:rating");
    assertResultSize(2, result);
    assertEquals("who", result.get(0).getKey());
    assertEquals("bobevans@google.com", result.get(0).getValue());
    assertEquals("rating", result.get(1).getKey());
    assertNull(result.get(1).getValue());
  }

  public void testTwoQueriesFull() throws Exception {
    List<Query> result = queryParser.parse("who=bobevans@google.com:rating=2");
    assertResultSize(2, result);
    assertEquals("who", result.get(0).getKey());
    assertEquals("bobevans@google.com", result.get(0).getValue());
    assertEquals("rating", result.get(1).getKey());
    assertEquals("2", result.get(1).getValue());
  }
  
  public void testDateRange() throws Exception {
    List<Query> result = queryParser.parse("date_range=20090831-20090902");
    assertResultSize(1, result);
    assertEquals("date_range", result.get(0).getKey());
    assertEquals("20090831-20090902", result.get(0).getValue());
  }

}
