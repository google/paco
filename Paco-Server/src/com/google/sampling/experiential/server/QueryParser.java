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

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * param_string :: 'q=\'' keyvalue_pair_list '\'' 
 * keyvalue_pair_list :: EMPTY |keyvalue_pair (':' keyvalue_pair)* 
 * EMPTY :: '' 
 * keyvalue_pair :: date_range | keyword ('=' value)+ 
 * date_range :: 'date_range=' date (-date)+ 
 * date :: [0-0]{4}[0-9]{2}[0-9]{2} 
 * keyword :: builtin_keyword | app_specific_keyword
 * builtin_keyword :: 'who' | 'when' | 'lat' | 'lon' | 'appId' | 'paco_version'
 * app_specific_keyword :: [a-zA-Z_][0-9a-zA-Z_]* 
 * value :: digit | word 
 * digit :: [0-9]+ 
 * word :: quoted_word | plain word 
 * quoted_word :: quote plain_word quote
 * quote :: ' | " 
 * plain_word :: [0-9a-zA-Z_ ]+
 * 
 * 
 * Examples:
 * 
 * q='restaurant' 
 * q='restaurant=' 
 * q='restaurant=CafeMoma'
 * q='who=bobevans@google.com:weight'
 * q='restaurant=CafeMoma:date_range=20090831-20090902'
 * q='restaurant=CafeMoma:date_range=20090831'
 * 
 * @author Bob Evans
 * 
 */
public class QueryParser {

  public List<Query> parse(String q) {
    List<Query> query = Lists.newArrayList();
    if (q == null || q.isEmpty()) {
      return query;
    }

    Iterable<String> kv_pair_list = Splitter.on(":").split(q);
    for (String keyValuePair : kv_pair_list) {
      query.add(parseKeyValuePair(keyValuePair));
    }
    return query;
  }

  private Query parseKeyValuePair(String keyValuePair) {
    Iterable<String> kvsplit = Splitter.on("=").split(keyValuePair);
    Iterator<String> iterator = kvsplit.iterator();
    String key = null;
    String value = null;
    if (iterator.hasNext()) {
      key = iterator.next();
    } else {
      throw new IllegalArgumentException("Illformed query keyvalue pair");
    }
    if (iterator.hasNext()) {
      value = iterator.next();
    }

    return new Query(key, value);
  }

}
