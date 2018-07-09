package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

public class CloudStorageFileWriter {
  public static final Logger log = Logger.getLogger(CloudStorageFileWriter.class.getName());
  static GcsService gcsService = GcsServiceFactory.createGcsService();;
  static String bucketName = System.getProperty("com.pacoapp.reportbucketname");
  private static final String JOB_ID = "jobId";
  private static final String SLASH = "/";
  private static final String GS = "/gs/";
  

  public GcsOutputChannel getCSWriterChannel(String fileName, String mimeType, String acl, String jobId) throws IOException {
    fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder()
        .mimeType(mimeType)
        .acl(acl)
        .addUserMetadata(JOB_ID, jobId)
        .build();
  
    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    return writeChannel;
  }
  public BlobKey getBlobKey(BlobstoreService blobstoreService, String jobId) {
    return blobstoreService.createGsBlobKey(GS + bucketName + SLASH + jobId);
  }
}
  

