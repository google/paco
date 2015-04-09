package com.pacoapp.paco.os;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.UserPreferences;

public class RingtoneUtil {

  private static final String RINGTONE_TITLE_COLUMN_NAME = "title";
  private static final String PACO_BARK_RINGTONE_TITLE = "Paco Bark";
  private static final String BARK_RINGTONE_FILENAME = "deepbark_trial.mp3";
  private Context context;
  private UserPreferences userPreferences;


  public RingtoneUtil(Context context) {
    super();
    this.context = context.getApplicationContext();
  }

  public void XXXinstallPacoBarkRingtone() {
    userPreferences = new UserPreferences(context);
    if (userPreferences.hasInstalledPacoBarkRingtone()) {
      return;
    }

    File f = copyRingtoneFromAssetsToSdCard();
    if (f == null) {
      return;
    }
    insertRingtoneFile(f);
  }

  public void installPacoBarkRingtone() {
    UserPreferences userPreferences = new UserPreferences(context);
    if (userPreferences.hasInstalledPacoBarkRingtone()) {
      return;
    }

    File f = copyRingtoneFromAssetsToSdCard();
    if (f == null) {
      return;
    }
    ContentValues values = createBarkRingtoneDatabaseEntry(f);
    Uri uri = MediaStore.Audio.Media.getContentUriForPath(f.getAbsolutePath());
    ContentResolver mediaStoreContentProvider = context.getContentResolver();
    Cursor existingRingtoneCursor = mediaStoreContentProvider.query(uri, null, null, null, null); // Note: i want to just retrieve MediaStore.MediaColumns.TITLE and to search on the match, but it is returning null for the TITLE value!!!
    Cursor c = mediaStoreContentProvider.query(uri, null, null, null, null);
    boolean alreadyInstalled = false;
    while (c.moveToNext()) {
      int titleColumnIndex = c.getColumnIndex(RINGTONE_TITLE_COLUMN_NAME);
      String ringtoneTitle = c.getString(titleColumnIndex);
      if (PACO_BARK_RINGTONE_TITLE.equals(ringtoneTitle)) {
        alreadyInstalled = true;
      }
    }
    existingRingtoneCursor.close();

    if (!alreadyInstalled) {
      Uri newUri = mediaStoreContentProvider.insert(uri, values);
      if (newUri != null) {
        userPreferences.setRingtone(newUri.toString());
        userPreferences.setPacoBarkRingtoneInstalled();
      }
    }


  }

  private File copyRingtoneFromAssetsToSdCard()  {
    InputStream fis = null;
    OutputStream fos = null;
    try {
      fis = context.getAssets().open(BARK_RINGTONE_FILENAME);

      if (fis == null) {
        return null;
      }

      File path = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                           + "/Android/data/" + context.getPackageName() + "/");
      if (!path.exists()) {
        path.mkdirs();
      }

      File f = new File(path, BARK_RINGTONE_FILENAME);
      fos = new FileOutputStream(f);
      byte[] buf = new byte[1024];
      int len;
      while ((len = fis.read(buf)) > 0) {
        fos.write(buf, 0, len);
      }
      return f;
    } catch (FileNotFoundException e) {
      Log.e(PacoConstants.TAG, "Could not create ringtone file on sd card. Error = " + e.getMessage());
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, "Either Could not open ringtone from assets. Or could not write to sd card. Error = " + e.getMessage());
      return null;
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          Log.e(PacoConstants.TAG, "could not close sd card file handle. Error = " + e.getMessage());
        }
      }
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          Log.e(PacoConstants.TAG, "could not close asset file handle. Error = " + e.getMessage());
        }
      }
    }
    return null;
  }

  private ContentValues createBarkRingtoneDatabaseEntry(File f) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.DATA, f.getAbsolutePath());
    values.put(MediaStore.MediaColumns.TITLE, PACO_BARK_RINGTONE_TITLE);
    values.put(MediaStore.MediaColumns.SIZE, f.length());
    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
    values.put(MediaStore.Audio.Media.ARTIST, "Paco");
    // values.put(MediaStore.Audio.Media.DURATION, ""); This is not needed
    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
    values.put(MediaStore.Audio.Media.IS_ALARM, false);
    values.put(MediaStore.Audio.Media.IS_MUSIC, false);
    return values;
  }

  /**
   * From Stackoverflow issue:
   * http://stackoverflow.com/questions/22184729/sqliteconstraintexception-thrown-when-trying-to-insert
   * @param filename
   * @return
   */
  Uri insertRingtoneFile(File filename) {
    Uri toneUri = MediaStore.Audio.Media.getContentUriForPath(filename.getAbsolutePath());
    // SDK 11+ has the Files store, which already indexed... everything
    // We need the file's URI though, so we'll be forced to query
    if (Build.VERSION.SDK_INT >= 11) {
        Uri uri = null;

        Uri filesUri = MediaStore.Files.getContentUri("external");
        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.TITLE};
        String selection = MediaStore.MediaColumns.DATA + " = ?";
        String[] args = {filename.getAbsolutePath()};
        Cursor c = context.getContentResolver().query(filesUri, projection, selection, args, null);

        // We expect a single unique record to be returned, since _data is unique
        if (c.getCount() == 1) {
            c.moveToFirst();
            long rowId = c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID));
            String title = c.getString(c.getColumnIndex(MediaStore.MediaColumns.TITLE));
            c.close();
            uri = MediaStore.Files.getContentUri("external", rowId);

            // Since all this stuff was added automatically, it might not have the metadata you want,
            // like Title, or Artist, or IsRingtone
            if (!title.equals(PACO_BARK_RINGTONE_TITLE)) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.TITLE, PACO_BARK_RINGTONE_TITLE);

                if (context.getContentResolver().update(toneUri, values, null, null) < 1) {
                    Log.e(PacoConstants.TAG, "could not update ringtome metadata");
                }

                // Apparently this is best practice, although I have no idea what the Media Scanner
                // does with the new data
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, toneUri));
            }
        }
        else if (c.getCount() == 0) {
            // I suppose the MediaScanner hasn't run yet, we'll insert it
           // ... ommitted
        }
        else {
            throw new UnsupportedOperationException(); // it's expected to be unique!
        }

        return uri;
    }
    // For the legacy way, I'm assuming that the file we're working with is in a .nomedia
    // folder, so we are the ones who created it in the MediaStore. If this isn't the case,
    // consider querying for it and updating the existing record. You should store the URIs
    // you create in case you need to delete them from the MediaStore, otherwise you're a
    // litter bug :P
    else {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, filename.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, filename.length());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename.getName());
        values.put(MediaStore.MediaColumns.TITLE, PACO_BARK_RINGTONE_TITLE);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg3");
        values.put(MediaStore.Audio.Media.ARTIST, "Paco App");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri newToneUri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        userPreferences.setRingtone(newToneUri.toString());
        userPreferences.setPacoBarkRingtoneInstalled();
        // Apparently this is best practice, although I have no idea what the Media Scanner
        // does with the new data
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newToneUri));

        return newToneUri;
    }
}
}
