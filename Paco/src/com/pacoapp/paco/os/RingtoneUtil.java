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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.UserPreferences;

public class RingtoneUtil {

  private static final String RINGTONE_TITLE_COLUMN_NAME = "title";
  private static final String PACO_BARK_RINGTONE_TITLE = "Paco Bark";
  private static final String BARK_RINGTONE_FILENAME = "deepbark_trial.mp3";
  private Context context;


  public RingtoneUtil(Context context) {
    super();
    this.context = context.getApplicationContext();
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


}
