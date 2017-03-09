package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.util.SearchUtil;

public class ACLHelper {
  public static String getModifiedQueryBasedOnACL(String selectSql, String loggedInUser,
                                                  List<Long> adminExperimentsinDB) throws Exception {
    String adminExpIdCSV = adminExperimentsinDB.toString();
    boolean ownData = false;
    boolean ownExpt = false;
    adminExpIdCSV = adminExpIdCSV.replace("[", "");
    adminExpIdCSV = adminExpIdCSV.replace("]", "");

    List<String> userSpecifiedWhoValues = SearchUtil.retrieveUserSpecifiedConditions(selectSql, "who");
    List<String> userSpecifiedExpId = SearchUtil.retrieveUserSpecifiedConditions(selectSql, "experiment_id");
    List<Long> userSpecifiedExpIdValues = convertToLong(userSpecifiedExpId);

    // if user querying own data
    for (String s : userSpecifiedWhoValues) {
      if (s.equalsIgnoreCase(loggedInUser)) {
        ownData = true;
      } else {
        ownData = false;
      }
    }

    // if user querying own experiments
    for (Long s : userSpecifiedExpIdValues) {
      if (adminExperimentsinDB.contains(s)) {
        ownExpt = true;
      } else {
        ownExpt = false;
      }
    }

    // Level 1 filters
    // a->No exp id filter, no processing
    if ((userSpecifiedExpIdValues.size() == 0)) {
      throw new Exception("Unauthorized access: No experiment id filter");
    }

    // b->Mixed ACL in experiment id filter, and who clause is not logged in
    // admin user
    if (!ownExpt) {
      // The user is requesting experiments where he is not an admin. He could
      // be a participant or it could be a random experiment.
      if (!ownData) {
        throw new Exception("Unauthorized access: Mixed ACL in Experiment Id and who clause is not logged in user");
      }
    }

    // c->Mixed ACL in experiment id filter, and who clause is not of a
    // participant or the logged in user
    // TODO participant check

    // d->if logged in user is not admin and who filter contains user other than
    // the logged in user
    if (adminExperimentsinDB.size() == 0 && !ownData) {
      throw new Exception("Unauthorized access: User who is not an admin asking for another user data");
    }

    return selectSql;
  }

  private static List<Long> convertToLong(List<String> inpList) {
    List<Long> outList = Lists.newArrayList();
    for (String s : inpList) {
      outList.add(Long.parseLong(s.trim()));
    }
    return outList;
  }
}
