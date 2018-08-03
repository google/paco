package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.dao.dataaccess.InformedConsent;

public interface CSInformedConsentDao {
  void insertInformedConsent(InformedConsent informedConsent, Integer experimentVersion) throws SQLException;
}
