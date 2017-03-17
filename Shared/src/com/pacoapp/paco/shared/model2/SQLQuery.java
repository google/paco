package com.pacoapp.paco.shared.model2;

//POJO to hold the different parts of the SQL query
public class SQLQuery {
  private static final String DESC = " desc";
  private static final String ID = "_id";

  String[] projection;
  String criteriaQuery;
  String[] criteriaValue;
  String groupBy;
  String having;
  String sortOrder;
  String limit;

  public SQLQuery(Builder b) {
    this.projection = b.projection;
    this.criteriaQuery = b.criteriaQuery;
    this.criteriaValue = b.criteriaValue;
    this.groupBy = b.groupBy;
    this.having = b.having;
    this.limit = b.limit;
    this.sortOrder = b.sortOrder;
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

  public static class Builder {
    private String[] projection;
    private String criteriaQuery;
    private String[] criteriaValue;
    private String groupBy;
    private String having;
    private String sortOrder;
    private String limit;

    public Builder(String[] projection) {

      this.projection = projection;
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

    private Builder addDefaultValues(SQLQuery obj) {
      // provide default sort order which is Event._Id desc
      if (obj.sortOrder == null) {
        obj.sortOrder = ID.concat(DESC);
      }

      if (obj.getProjection() == null) {
        obj.projection = new String[] { "*" };
      }

      // adding a default projection of event table primary key column
      int crtLength = obj.getProjection().length;

      String[] modifiedProjection = new String[crtLength + 1];
      System.arraycopy(obj.getProjection(), 0, modifiedProjection, 0, crtLength);
      // adding the following columns in the projection list to help in
      // coalescing
      modifiedProjection[crtLength] = ID;
      obj.projection = modifiedProjection;

      return this;
    }

    public SQLQuery buildWithDefaultValues() {
      SQLQuery obj = new SQLQuery(this);
      addDefaultValues(obj);
      return obj;
    }

  }

}