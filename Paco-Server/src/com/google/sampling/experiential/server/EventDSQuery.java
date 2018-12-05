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


import java.util.List;

import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.common.collect.Lists;

public class EventDSQuery  {

  protected com.google.appengine.api.datastore.Query query;
  protected List<Filter> filters;
  private boolean conjunctive = true;


  private boolean hasAWho;
  private String who;

  public boolean hasAWho() {
    return hasAWho;
  }

  public void setHasWho(String who) {
    hasAWho = true;
    this.who = who;
  }

  public EventDSQuery(com.google.appengine.api.datastore.Query newQuery) {
    super();
    this.query = newQuery;
    this.filters = Lists.newArrayList();
  }

  public String who() {
    return who;
  }

  public void applyFiltersToQuery(com.google.appengine.api.datastore.Query q) {
    if (filters.size() == 1) {
      q.setFilter(filters.get(0));
    } else if (filters.size() > 1) {
      q.setFilter(createCompoundFilter());
    }
  }

  private Filter createCompoundFilter() {
    if (conjunctive) {
      return CompositeFilterOperator.and(filters);
    } else {
      return CompositeFilterOperator.or(filters);
    }
  }

  public void addFilter(Filter filter) {
      filters.add(filter);
  }

  
  
  
}
