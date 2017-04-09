package com.pacoapp.paco.sensors.android.diagnostics;

import java.util.List;

import android.content.Context;

import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public class PacoJoinedExperimentDiagnostic extends ListDiagnostic {

  public PacoJoinedExperimentDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_joined_experiments_type));
  }

  @Override
  public void run(Context context) {
    List<String> results = Lists.newArrayList();
    ExperimentProviderUtil ep = new ExperimentProviderUtil(context);
    List<Experiment> joined = ep.getJoinedExperiments();
    if (joined != null) {
      results.add(context.getString(R.string.diagnostics_experiment_count_label) + ": " + joined.size());
      for (int i=0; i < joined.size(); i++) {
        Experiment experiment = joined.get(i);
        ExperimentDAO experimentDAO = experiment.getExperimentDAO();
        String title = experimentDAO.getTitle();
        Integer experimentVersion = experimentDAO.getVersion();
        //String joinDate = experimentDAO.getJoinDate();
        results.add((i + 1) + ": " + title + " (" + experimentVersion + ")" /*+ ", " + joinDate*/);
      }
    }
    setValue(results);
  }

}
