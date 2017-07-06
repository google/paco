package com.google.sampling.experiential.server;

import java.io.IOException;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

public class CloudStorageFileWriter {
  static GcsService gcsService = GcsServiceFactory.createGcsService();;
  static String bucketName = System.getProperty("com.pacoapp.reportbucketname");

  public GcsOutputChannel getCSWriterChannel(String fileName, String mimeType, String acl, String jobId) throws IOException {
    fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder()
        .mimeType(mimeType)
        .acl(acl)
        .addUserMetadata("jobId", jobId)
        .build();
  
    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    return writeChannel;
  }
}
  

