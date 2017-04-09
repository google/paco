package com.pacoapp.paco.js.bridge;

//POJO to hold the different parts of the SQL query
public class SQLQuery {
  String[] projection;
  String criteriaQuery;
  String[] criteriaValue;
  String groupBy;
  String having;
  String sortOrder;
  String limit;

  public String[] getProjection() {
    return projection;
  }

  public void setProjection(String[] projection) {
    this.projection = projection;
  }

  public String getCriteriaQuery() {
    return criteriaQuery;
  }

  public void setCriteriaQuery(String criteriaQuery) {
    this.criteriaQuery = criteriaQuery;
  }

  public String[] getCriteriaValue() {
    return criteriaValue;
  }

  public void setCriteriaValue(String[] criteriaValue) {
    this.criteriaValue = criteriaValue;
  }

  public String getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }

  public String getHaving() {
    return having;
  }

  public void setHaving(String having) {
    this.having = having;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public String getLimit() {
    return limit;
  }

  public void setLimit(String limit) {
    this.limit = limit;
  }

}
