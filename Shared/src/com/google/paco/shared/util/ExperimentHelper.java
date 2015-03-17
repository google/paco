package com.google.paco.shared.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Input2;

public class ExperimentHelper {

  public static Input2 getInputWithName(ExperimentDAO experiment, String name, String groupName) {
    if (Strings.isNullOrEmpty(name)) {
      return null;
    }
    List<Input2> inputs = null;
    if (groupName == null || groupName.isEmpty()) {
      inputs = getInputs(experiment);
    } else {
      ExperimentGroup group = experiment.getGroupByName(groupName);
      if (group != null) {
        inputs = group.getInputs();
      }
    }
    if (inputs != null) {
      for (Input2 input : inputs) {
        if (name.equals(input.getName())) {
          return input;
        }
      }
    }
    return null;
  }

  //@JsonIgnore
  public static List<Input2> getInputs(ExperimentDAO experiment) {
    List<Input2> inputs = new java.util.ArrayList<Input2>();
    for (ExperimentGroup group : experiment.getGroups()) {
      inputs.addAll(group.getInputs());
    }
    return inputs;
  }

}
