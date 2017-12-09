package com.google.sampling.experiential.dao;

import java.sql.SQLException;

import com.google.sampling.experiential.server.PacoId;

public interface CSExperimentLookupDao {

  PacoId getExperimentLookupIdAndCreate(Long expId, String expName, String groupName, Integer version,
                                        boolean createOption) throws SQLException;

}
