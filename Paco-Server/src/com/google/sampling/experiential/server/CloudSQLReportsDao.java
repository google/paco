package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public interface CloudSQLReportsDao {
  String bucketName = System.getProperty("com.pacoapp.reportbucketname");
  BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  CloudStorageFileWriter csfw = new CloudStorageFileWriter();
  List<String> getParticipants(Long experimentId, String who) throws SQLException;
  String storeQuickStatusInCloudStorage(String jobId, Long experimentId, String who) throws SQLException, JSONException, FileNotFoundException, IOException;
  String storeCompleteStatusInCloudStorage(String jobId, Long experimentId,
                                           String who) throws SQLException, JSONException, FileNotFoundException,
                                                       IOException;
  String storeQuickStatusBySPInCloudStorage(String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException; 
  String storeCompleteStatusBySPInCloudStorage(String jobId, Long expId, String who) throws SQLException, JSONException, FileNotFoundException, IOException; 
}
