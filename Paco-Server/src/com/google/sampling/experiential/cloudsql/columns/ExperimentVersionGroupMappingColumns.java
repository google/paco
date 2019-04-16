package com.google.sampling.experiential.cloudsql.columns;

public class ExperimentVersionGroupMappingColumns {
  public static final String TABLE_NAME = "experiment_version_group_mapping";
  
  public static final String EXPERIMENT_VERSION_GROUP_MAPPING_ID = "experiment_version_group_mapping_id";
  public static final String EXPERIMENT_ID = "experiment_id";
  public static final String EXPERIMENT_VERSION = "experiment_version";
  public static final String EXPERIMENT_DETAIL_ID = ExperimentDetailColumns.EXPERIMENT_DETAIL_ID;
  public static final String GROUP_DETAIL_ID = GroupDetailColumns.GROUP_DETAIL_ID;
  public static final String INPUT_COLLECTION_ID = InputCollectionColumns.INPUT_COLLECTION_ID;
  public static final String EVENTS_POSTED = "events_posted";
  public static final String SOURCE = "source";
}
