package com.google.sampling.experiential.cloudsql.columns;

public class ExperimentVersionMappingColumns {
  public static final String TABLE_NAME = "experiment_version_mapping";
  
  public static final String EXPERIMENT_VERSION_MAPPING_ID = "experiment_version_mapping_id";
  public static final String EXPERIMENT_ID = "experiment_id";
  public static final String EXPERIMENT_VERSION = "experiment_version";
  public static final String EXPERIMENT_FACET_ID = ExperimentColumns.EXPERIMENT_FACET_ID;
  public static final String GROUP_ID = GroupColumns.GROUP_ID;
  public static final String INPUT_COLLECTION_ID = InputCollectionColumns.INPUT_COLLECTION_ID;
  public static final String EVENTS_POSTED = "events_posted";
  public static final String SOURCE = "source";
}
