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

import javax.jdo.Query;

import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class EventDSQuery extends BaseJDOQuery {

  private boolean hasAWho;
  private String who;

  public boolean hasAWho() {
    return hasAWho;
  }

  public void setHasWho(String who) {
    hasAWho = true;
    this.who = who;
  }

  public EventDSQuery(Query newQuery) {
    super(newQuery);
    query.setOrdering("when desc");
  }

  public String who() {
    return who;
  }

  public void getLowLevelDatastoreEntityQuery(com.google.appengine.api.datastore.Query q) {
    if (filters.size() > 0) {
      for (String filter : filters) {
        q.setFilter(createFilterPredicate(filter));
      }
    }
  }

  private Filter createFilterPredicate(String filter) {
    return new FilterPredicate(filter, FilterOperator.EQUAL, filter);
  }

}
