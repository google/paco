package com.google.sampling.experiential.server.migration.jobs;

// Imports the Google Cloud client library
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;

public class QuickstartSample {
  public static void main(String... args) throws Exception {
    // Instantiates a client
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

    // The name for the new dataset
    String datasetName = "my_new_dataset";

    // Prepares a new dataset
    Dataset dataset = null;
    DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();

    // Creates the dataset
    dataset = bigquery.create(datasetInfo);

    System.out.printf("Dataset %s created.%n", dataset.getDatasetId().getDataset());
  }
}