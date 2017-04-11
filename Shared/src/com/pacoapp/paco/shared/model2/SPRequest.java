package com.pacoapp.paco.shared.model2;

import com.pacoapp.paco.shared.model2.EventBaseColumns;

public class SPRequest {
  private static final String DESC = " desc";
  private static final String ID = "_id";
  StoredProcEnum spName;
  String expId;
  String fromDate;
  String toDate;
  String who;
  String limit;
  String sortOrder;

  public SPRequest(Builder b) {
    this.spName = b.spName;
    this.expId = b.expId;
    this.fromDate = b.fromDate;
    this.toDate = b.toDate;
    this.who = b.who;
    this.limit = b.limit;
    this.sortOrder = b.sortOrder;
  }

  public StoredProcEnum getSpName() {
    return spName;
  }

  public String getExpId() {
    return expId;
  }

  public String getFromDate() {
    return fromDate;
  }

  public String getToDate() {
    return toDate;
  }

  public String getWho() {
    return who;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public String getLimit() {
    return limit;
  }

  public static class Builder {
    StoredProcEnum spName;
    String expId;
    String fromDate;
    String toDate;
    String who;
    String limit;
    String sortOrder;

    public Builder(String expId) {

      this.expId = expId;
    }

    public Builder spName(StoredProcEnum spName) {
      this.spName = spName;
      return this;
    }

    public Builder fromDate(String fromDate) {
      this.fromDate = fromDate;
      return this;
    }

    public Builder toDate(String toDate) {
      this.toDate = toDate;
      return this;
    }

    public Builder who(String who) {
      this.who = who;
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

    private Builder addDefaultValues(SPRequest obj) {
      // provide default sort order which is Event._Id desc
      if (obj.sortOrder == null) {
        obj.sortOrder = EventBaseColumns.TABLE_NAME + "." + ID.concat(DESC);
      }
      return this;
    }

    public SPRequest buildWithDefaultValues() {
      SPRequest obj = new SPRequest(this);
      addDefaultValues(obj);
      return obj;
    }

  }

}
