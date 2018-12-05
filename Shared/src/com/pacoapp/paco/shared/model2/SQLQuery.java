package com.pacoapp.paco.shared.model2;

import com.pacoapp.paco.shared.util.Constants;

//POJO to hold the different parts of the SQL query
public class SQLQuery {

  String[] projection;
  String criteriaQuery;
  String[] criteriaValue;
  String groupBy;
  String having;
  String sortOrder;
  String limit;
  Boolean fullEventAndOutputs;

  public SQLQuery(Builder b) {
    this.projection = b.projection;
    this.criteriaQuery = b.criteriaQuery;
    this.criteriaValue = b.criteriaValue;
    this.groupBy = b.groupBy;
    this.having = b.having;
    this.limit = b.limit;
    this.sortOrder = b.sortOrder;
    this.fullEventAndOutputs = b.fullEventAndOutputs;
  }

  public String[] getProjection() {
    return projection;
  }

  public String getCriteriaQuery() {
    return criteriaQuery;
  }

  public String[] getCriteriaValue() {
    return criteriaValue;
  }

  public String getGroupBy() {
    return groupBy;
  }

  public String getHaving() {
    return having;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public String getLimit() {
    return limit;
  }

  public Boolean isFullEventAndOutputs() {
    return fullEventAndOutputs;
  }

  public void addClientTzToProjection(){
    String[] modArr = new String[projection.length+1];
    System.arraycopy(projection, 0, modArr, 0, projection.length);
    modArr[projection.length] = Constants.CLIENT_TIMEZONE;
    this.projection = modArr;
  }

  public static class Builder {
    private String[] projection;
    private String criteriaQuery;
    private String[] criteriaValue;
    private String groupBy;
    private String having;
    private String sortOrder;
    private String limit;
    private Boolean fullEventAndOutputs;

    public Builder projection(String[] projection) {
      this.projection = projection;
      return this;
    }

    public Builder criteriaQuery(String criQuery) {
      this.criteriaQuery = criQuery;
      return this;
    }

    public Builder criteriaValues(String[] criValues) {
      this.criteriaValue = criValues;
      return this;
    }

    public Builder groupBy(String groupBy) {
      this.groupBy = groupBy;
      return this;
    }

    public Builder having(String having) {
      this.having = having;
      return this;
    }

    public Builder sortBy(String sortBy) {
      this.sortOrder = sortBy;
      return this;
    }

    public Builder limit(String limit) {
      this.limit = limit;
      return this;
    }

    public Builder fullEventAndOutputs(Boolean fullEventAndOutputs) {
      this.fullEventAndOutputs = fullEventAndOutputs;
      return this;
    }

    private Builder addDefaultValues(SQLQuery obj) {

      // default projection is *
      if (obj.getProjection() == null) {
        obj.projection = new String[] { Constants.STAR };
        obj.fullEventAndOutputs = true;
      }
      // find if there is a distinct clause
      boolean isDistinct = false;
      for(String s : obj.getProjection()) {
        if (s.startsWith(Constants.DISTINCT)) {
          isDistinct = true;
          break;
        }
      }

      // provide default sort order which is Event._Id desc
      // but do not provide default ordering under the following conditions
      // 1.when the user specifies a group by value, in this case when we add default ordering on _id, sql complains order by column _id should be part of group by
      // 2.distinct query, in this case when we add default ordering on _id, sql complains order by column _id should be part of select list
      if (obj.getGroupBy() == null && !isDistinct && obj.sortOrder == null) {
        obj.sortOrder = EventBaseColumns.TABLE_NAME + "." + Constants.UNDERSCORE_ID.concat(Constants.BLANK).concat(Constants.DESC);
      }
      return this;
    }

    public SQLQuery buildWithDefaultValues() {
      SQLQuery obj = new SQLQuery(this);
      addDefaultValues(obj);
      return obj;
    }

  }

}