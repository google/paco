package com.google.sampling.experiential.server.stats.usage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

public class UsageStatsBlobWriter {

  private static final Logger log = Logger.getLogger(UsageStatsBlobWriter.class.getName());


  public UsageStatsBlobWriter() {
  }

  public String writeStatsAsJson(String jobId, String timeZone, String requestorEmail) {
    log.info("writing usage stats report");
    UsageStatsCronJob job = new UsageStatsCronJob();
    try {
      job.run();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "it doesn't do that anymore :-)"; 
  }
  
  private BlobKey writeBlobUsingNewApi(String jobId, String timeZone,
                                       String eventPage) throws IOException,
                                       FileNotFoundException {

    GcsService gcsService = GcsServiceFactory.createGcsService();
    String bucketName = System.getProperty("com.pacoapp.reportbucketname");
    String fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder().mimeType("application/json").acl("project-private")
                                                         .addUserMetadata("jobId", jobId).build();

    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
    writer.println(eventPage);
    writer.flush();

    writeChannel.waitForOutstandingWrites();

    writeChannel.close();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey(
        "/gs/" + bucketName + "/" + fileName);
    return blobKey;
  }

}
