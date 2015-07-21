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

import java.util.Collection;
import java.util.List;

import javax.jdo.Query;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;



/**
 *
 * Factored out baseclass for JDOqueries.
 * Reused by Expeirment and Event queries.
 *
 * @author Bob Evans
 *
 */
public class BaseJDOQuery {

  private static final String OR_JUNCTION = " || ";
  private static final String AND_JUNCTION = " && ";
  protected Query query;
  protected List<String> parameterDecls;
  protected List<Object> parameterObjects;
  protected List<String> filters;
  private boolean conjunctive = true;

  /**
   *
   */
  public BaseJDOQuery(Query newQuery) {
    super();
    this.query = newQuery;

    parameterDecls = Lists.newArrayList();
    parameterObjects = Lists.newArrayList();
    filters = Lists.newArrayList();
  }

  public void addParameterObjects(Object... objects) {
    for (Object object : objects) {
      if (object instanceof Collection) {
        parameterObjects.addAll((Collection<? extends Object>) object);
      } else {
        parameterObjects.add(object);
      }
    }
  }

  public void declareParameters(String... params) {
    for (String param : params) {
      parameterDecls.add(param);
    }

  }

  public void addFilters(String... string) {
    for (String string2 : string) {
      filters.add(string2);
    }

  }

  public Query getQuery() {
    if (parameterDecls.size() > 0) {
      String params = Joiner.on(", ").join(parameterDecls);
      query.declareParameters(params);
    }
    if (filters.size() > 0) {
      String filter = Joiner.on(getJunctionString()).join(filters);
      query.setFilter(filter);
    }
    return query;
  }

  protected String getJunctionString() {
    if (conjunctive) {
      return defaultJunction();
    }
    return OR_JUNCTION;
  }

  public void setConjunctive(boolean conjunctive) {
    this.conjunctive = conjunctive;
  }

  private String defaultJunction() {
    return AND_JUNCTION;
  }

  public List<Object> getParameters() {
    return parameterObjects;
  }

}
