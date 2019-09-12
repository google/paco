package com.google.sampling.experiential.gcs;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.codec.binary.Base64;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.appengine.tools.cloudstorage.RetryParams.Builder;
import com.google.sampling.experiential.server.BlobAcl;
import com.google.sampling.experiential.server.BlobAclStore;
import com.google.sampling.experiential.server.JSONBlobWriter;
import com.google.sampling.experiential.shared.WhatDAO;

public class GCSFetcher {
  
  public static int BUFFER_SIZE = 2 * 1024 * 1024;

  private GCSFetcher() {}

  public static void fillInResponsesWithEncodedBlobDataFromGCS(List<WhatDAO> whatMap) {
    final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
                                                                     .initialRetryDelayMillis(10)
                                                                     .retryMaxAttempts(10)
                                                                     .totalRetryPeriodMillis(15000)
                                                                     .build());
    BlobAclStore bas = BlobAclStore.getInstance();
    
    for(WhatDAO currentWhat : whatMap) {
      String currentWhatValue = currentWhat.getValue();
      if (currentWhatValue != null && currentWhatValue.startsWith("/eventblobs")) {
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String blobKey = GCSFetcher.getBlobKey(currentWhatValue);
        BlobAcl blobAcl = bas.getAcl(blobKey);
        if (blobAcl != null && blobAcl.getBucketName() != null && blobAcl.getObjectName() != null) {
          GcsFilename gcsFileName = GCSFetcher.getFileName(blobAcl.getBucketName(), blobAcl.getObjectName());
          GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(gcsFileName, 0, GCSFetcher.BUFFER_SIZE);
          try {
            GCSFetcher.copy(Channels.newInputStream(readChannel), byteArrayOutputStream);
            String photoBlobString = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
            currentWhat.setValue(photoBlobString);
          } catch (IOException e) {
            JSONBlobWriter.log.log(Level.WARNING, "failed to copy blob from GCS", e);
          }
        } else {
          JSONBlobWriter.log.warning("Blob key for writing blob was null: " + currentWhatValue);
        }
      }
    }
  }

  public static String fillInResponseForKeyWithEncodedBlobDataFromGCS(String currentWhatValue) {
    final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
                                                                     .initialRetryDelayMillis(10)
                                                                     .retryMaxAttempts(10)
                                                                     .totalRetryPeriodMillis(15000)
                                                                     .build());
    BlobAclStore bas = BlobAclStore.getInstance();
    
      if (currentWhatValue != null && currentWhatValue.startsWith("/eventblobs")) {        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String blobKey = GCSFetcher.getBlobKey(currentWhatValue);
        BlobAcl blobAcl = bas.getAcl(blobKey);
        if (blobAcl != null && blobAcl.getBucketName() != null && blobAcl.getObjectName() != null) {
          GcsFilename gcsFileName = GCSFetcher.getFileName(blobAcl.getBucketName(), blobAcl.getObjectName());
          GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(gcsFileName, 0, GCSFetcher.BUFFER_SIZE);
          try {
            GCSFetcher.copy(Channels.newInputStream(readChannel), byteArrayOutputStream);
            String photoBlobString = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
            return photoBlobString;
          } catch (IOException e) {
            JSONBlobWriter.log.log(Level.WARNING, "failed to copy blob from GCS", e);
          }
        } else {
          JSONBlobWriter.log.warning("Blob key for writing blob was null: " + currentWhatValue);
        }
      }
      return null;
    }
  

  
  public static String getBlobKey(String value) {
    String[] parts = value.split("&");
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].startsWith("blob-key=")) {
        return parts[i].substring(9);
      }
    }
    return null;
  }

  public static GcsFilename getFileName(String bucketName, String objectName) {
    return new GcsFilename(bucketName, objectName);
  }

  /**
   * Transfer the data from the inputStream to the outputStream. Then close both streams.
   */
  public static void copy(InputStream input, OutputStream output) throws IOException {
    try {
      byte[] buffer = new byte[GCSFetcher.BUFFER_SIZE];
      int bytesRead = input.read(buffer);
      while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
      }
    } finally {
      input.close();
      output.close();
    }
  }

  public static BlobKey writeJsonBlobToGCS(String jobId, String json) throws IOException,
                                                                     FileNotFoundException {
    GcsService gcsService = GcsServiceFactory.createGcsService();
    String bucketName = System.getProperty("com.pacoapp.reportbucketname");
    String fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder()
        .mimeType("application/json")
        .acl("project-private")
        .addUserMetadata("jobId", jobId)
        .build();
  
    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
  
    writer.println(json);
    writer.flush();
    writeChannel.waitForOutstandingWrites();
    writeChannel.close();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + fileName);
    return blobKey;
  };

}
