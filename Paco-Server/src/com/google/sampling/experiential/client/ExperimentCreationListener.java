package com.google.sampling.experiential.client;

import com.google.paco.shared.model.ExperimentDAO;

/**
 * A narrow interface for the actions available to navigate
 * through the experiment creation process.
 * 
 * @author Priya Donti
 *
 */
public interface ExperimentCreationListener {
  public static final int SHOW_DESCRIPTION_CODE = 1;
  public static final int SHOW_SCHEDULE_CODE = 2;
  public static final int SHOW_INPUTS_CODE = 3;
  public static final int SHOW_PUBLISHING_CODE = 4;
  public static final int NEXT = 5;

  void eventFired(int creationCode, ExperimentDAO experiment, Integer inputGroupNumber);
}
