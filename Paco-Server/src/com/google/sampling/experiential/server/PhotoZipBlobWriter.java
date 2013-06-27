package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.TimeUtil;

public class PhotoZipBlobWriter {
  
  private static final Logger log = Logger.getLogger(PhotoZipBlobWriter.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT_FOR_FILENAME).withOffsetParsed();

  
  public PhotoZipBlobWriter() {
  }

  public String writePhotoZipFile(boolean anon, String experimentId, List<Event> events, String jobId) {
    log.info("Inside writePhotoZipFile");
    FileService fileService = FileServiceFactory.getFileService();
    AppEngineFile file = null;
    FileWriteChannel writeChannel  = null;
    ZipOutputStream zip = null;
    try {
      file = fileService.createNewBlobFile("application/zip", "photos_" + experimentId + ".zip");
      log.info("Inside writePhotoZipFile");
      boolean lock = true;
      writeChannel = fileService.openWriteChannel(file, lock);
      log.info("Inside writePhotoZipFile");
      OutputStream blobOutputStream = Channels.newOutputStream(writeChannel);
      log.info("Inside writePhotoZipFile");
      zip = new ZipOutputStream(blobOutputStream);
      addPhotoEventsToZip(getEventsWithPhotos(events), zip, anon);
      log.info("Inside writePhotoZipFile");
      zip.close();
      writeChannel.closeFinally();
      BlobKey blobKey = fileService.getBlobKey(file);
      if (blobKey != null) {
        return blobKey.getKeyString();
      }
      return "Error getting location of report";
    } catch (IOException e) {
      log.log(Level.SEVERE, "IO Thrown writing zip file.", e);
      throw new RuntimeException("Writing photo zip into blobStore", e);
    }
  }

  private void addPhotoEventsToZip(List<Event> eventsWithPhotos, ZipOutputStream zip, boolean anon) throws IOException {
    for (Event event : eventsWithPhotos) {
      createFilesForEventPhotos(zip, event, anon);
    }
  }

  private void createFilesForEventPhotos(ZipOutputStream zip, Event event, boolean anon) throws IOException {
    String filenamePrefix = anonymize(event.getWho(), anon) + "_" + new DateTime(event.getResponseTime()).toString(jodaFormatter) + "_";
    
    List<PhotoBlob> blobs = event.getBlobs();    
    for (int i=0; i < blobs.size(); i++) {
      PhotoBlob photoBlob = blobs.get(i);
      createFileForBytes(zip, filenamePrefix + Integer.toString(i), photoBlob.getValue());
    }
  }

  private String anonymize(String who, boolean anon) {
    if (anon) {
      return Event.getAnonymousId(who + Event.SALT);
    }
    return who;
  }

  private void createFileForBytes(ZipOutputStream zip, String filename, byte[] photoBytes) throws IOException {
    zip.putNextEntry(new ZipEntry(filename + ".jpg"));
    zip.write(photoBytes);
    zip.closeEntry();
  }

  private List<Event> getEventsWithPhotos(List<Event> events) {
    List<Event> eventsWithPhotos = Lists.newArrayList();
    for (Event event : events) {
      if (event.getBlobs().size() > 0) {
        eventsWithPhotos.add(event);
      }
    }
    return eventsWithPhotos;
  }

}
