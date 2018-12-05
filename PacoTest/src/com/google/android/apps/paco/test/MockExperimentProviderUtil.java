package com.google.android.apps.paco.test;

import java.util.List;

import android.content.Context;
import android.net.Uri;

import com.google.common.collect.Lists;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentColumns;
import com.pacoapp.paco.model.ExperimentProviderUtil;

class MockExperimentProviderUtil extends ExperimentProviderUtil {

  private List<Experiment> experimentList;

  MockExperimentProviderUtil(Context context) {
    super(context);
    experimentList = Lists.newArrayList();
  }

  @Override
  public Uri insertFullJoinedExperiment(Experiment experiment) {
    experimentList.add(experiment);
    // The uri of a joined experiment has this as its first segment.
    return Uri.parse(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI.getPathSegments().get(0));
  }

  @Override
  public int deleteNotificationsForExperiment(Long experimentId) {
    return 0;
  }

  @Override
  public Experiment getExperimentByServerId(long id) {
    for (Experiment experiment : experimentList) {
      if (experiment.getServerId() != null && experiment.getServerId().equals(id))
        return experiment;
    }
    return null;
  }

  @Override
  public void updateJoinedExperiment(Experiment experiment) {
    Experiment experimentToDelete = null;
    for (Experiment e : experimentList) {
      if (e.getId().equals(experiment.getId())) {
        experimentToDelete = e;
      }
    }
    if (experimentToDelete != null) {
      experimentList.remove(experimentToDelete);
      experimentList.add(experiment);
    }
  }

  public Experiment getExperiment(long experimentId) {
    for (Experiment e : experimentList) {
      if (e.getId().equals(experimentId)) {
        return e;
      }
    }
    return null;
  }

  @Override
  public void deleteExperiment(long experimentId) {
    Experiment experimentToDelete = null;
    for (Experiment e : experimentList) {
      if (e.getId().equals(experimentId)) {
        experimentToDelete = e;
      }
    }
    if (experimentToDelete != null) {
      experimentList.remove(experimentToDelete);
    }
  }

}