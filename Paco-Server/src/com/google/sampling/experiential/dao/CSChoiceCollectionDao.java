package com.google.sampling.experiential.dao;

import java.math.BigInteger;
import java.sql.SQLException;

import com.google.sampling.experiential.dao.dataaccess.ChoiceCollection;

public interface CSChoiceCollectionDao {
  void createChoiceCollectionId(Long experimentId, BigInteger inputCollectionId, Integer inputOrder, ChoiceCollection choiceCollection) throws SQLException;
}
