package com.pacoapp.paco.js.bridge;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.provider.CalendarContract.Events;
import android.webkit.JavascriptInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pacoapp.paco.sensors.android.AndroidInstalledApplications;
import com.pacoapp.paco.shared.model2.JsonConverter;

public class JavascriptCalendarManager {

  private static final String[] INSTANCE_PROJECTION = new String[] {
    Events._SYNC_ID,
    Instances.TITLE,
    Instances.BEGIN,
    Instances.END,
    Calendars.CALENDAR_DISPLAY_NAME
  };

  private final Context context;

  public JavascriptCalendarManager(Context context) {
    this.context = context;
  }

  /**
   * get a list of all the calendar event list within the given time range.
   */
  @JavascriptInterface
  public String listEventInstances(String startMillis, String endMillis)
      throws NumberFormatException {
    Cursor cursor = null;
    ContentResolver cr = context.getContentResolver();

    // Create a cursor for the query.
    Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
    ContentUris.appendId(builder, Long.parseLong(startMillis));
    ContentUris.appendId(builder, Long.parseLong(endMillis));
    cursor =  cr.query(builder.build(), INSTANCE_PROJECTION, null, null, null);

    // Add the events from the cursor to a JSON-friendly blob.
    final List<Map<String, Object>> eventList = new ArrayList<Map<String, Object>>();
    while (cursor.moveToNext()) {
        Map<String, Object> event = new HashMap<String, Object>();
        event.put("id", cursor.getString(cursor.getColumnIndexOrThrow(Events._SYNC_ID)));
        event.put("title", cursor.getString(cursor.getColumnIndexOrThrow(Instances.TITLE)));
        event.put("begin", cursor.getString(cursor.getColumnIndexOrThrow(Instances.BEGIN)));
        event.put("end", cursor.getString(cursor.getColumnIndexOrThrow(Instances.END)));
        event.put("calendarDisplayName",
            cursor.getString(cursor.getColumnIndexOrThrow(Calendars.CALENDAR_DISPLAY_NAME)));
        eventList.add(event);
     }

    ObjectMapper mapper = JsonConverter.getObjectMapper();
    String json = null;
    try {
      json = mapper.writeValueAsString(eventList);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return json;
  }

}
