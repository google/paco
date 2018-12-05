package com.pacoapp.paco.js.bridge;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Attendees;
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
    Events.ACCOUNT_NAME,
    Events.OWNER_ACCOUNT,
    Instances.TITLE,
    Instances.BEGIN,
    Instances.END,
    Instances.EVENT_LOCATION,
    Instances.RDATE,
    Instances.RRULE,
    Instances.ORIGINAL_ID,
    Instances.START_DAY,
    Instances.START_MINUTE,
    Instances.END_DAY,
    Instances.END_MINUTE,
    Instances.IS_ORGANIZER,
    Instances.SELF_ATTENDEE_STATUS,
    Instances.ACCESS_LEVEL,
    Instances.EVENT_ID,

    // Android seems to return buggy data for HAS_ATTENDEE_DATA :(
    // Otherwise, this would be a great way to determine whether the event has
    // only 0 attendees, or so many that Calendar refuses to list them.
    Instances.HAS_ATTENDEE_DATA
  };

  private final Context context;

  public JavascriptCalendarManager(Context context) {
    this.context = context;
  }

  /**
   * Get a list of all the calendar event list within the given time range.
   *
   * The events returned by this function contain the following fields:
   *
   *     "id" - Event ID in Google Calendar.
   *     "title" - Short event summary.
   *     "begin"/"end" - The event start/end in UTC milliseconds.
   *     "start_day"/"end_day" - The Julian start/end day of the event (local time zone).
   *     "start_minute"/"end_minute" - Number of minutes since midnight (local time zone).
   *     "owner_account" - The id of the calendar owning the event.
   *     "location" - Event location. For multiple locations, they're generally separated by commas.
   *     "is_organizer" - True if PACO user is the organizer for the event.
   *     "is_recurring" - True if the event is part of a recurring series of events.
   *     "self_attendee_status" - The RSVP status of the PACO user for this event.
   *                              Options: "accepted", "declined", "invited", "tentative", "none".
   *     "access_level" - Indicates whether the event is visible to others.
   *                      Options: "confidential", "default", "private", "public".
   *     "attendees_accepted", "attendees_declined", "attendees_tentative" - Number of attendees by RSVP status.
   *     "attendees_rooms" - The number of rooms booked for the event.
   *     "has_attendee_data" - Whether the event has data on event attendees.
   *                           (However, Android's response for this field appears to be unreliable.)
   *
   * Only events on the user's primary calendar will be included.
   *
   * @see <a href="https://developer.android.com/reference/android/provider/CalendarContract.Instances.html">
   * Android calendar docs</a> for additional details on these values.
   */
  @JavascriptInterface
  public String listEventInstances(String startMillis, String endMillis)
      throws NumberFormatException {
    ContentResolver cr = context.getContentResolver();

    // Create a cursor for the query.
    Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
    ContentUris.appendId(builder, Long.parseLong(startMillis));
    ContentUris.appendId(builder, Long.parseLong(endMillis));
    Cursor cursor = cr.query(builder.build(), INSTANCE_PROJECTION, null, null, null);

    // Add the events from the cursor to a JSON-friendly blob.
    final List<Map<String, Object>> eventList = new ArrayList<Map<String, Object>>();
    while (cursor.moveToNext()) {
      boolean isPrimaryAccount =
          cursor.getString(cursor.getColumnIndexOrThrow(Events.ACCOUNT_NAME)).equals(
              cursor.getString(cursor.getColumnIndexOrThrow(Events.OWNER_ACCOUNT)));

      // Don't expose events from non-primary calendar accounts to PACO
      if (!isPrimaryAccount) { continue; }

      // Convert the Android event object to an object that can be serialized as JSON
      Map<String, Object> event = new HashMap<String, Object>();

      event.put("id", cursor.getString(cursor.getColumnIndexOrThrow(Events._SYNC_ID)));
      event.put("title", cursor.getString(cursor.getColumnIndexOrThrow(Instances.TITLE)));
      event.put("begin", cursor.getString(cursor.getColumnIndexOrThrow(Instances.BEGIN)));
      event.put("end", cursor.getString(cursor.getColumnIndexOrThrow(Instances.END)));
      event.put("owner_account", cursor.getString(cursor.getColumnIndexOrThrow(Events.OWNER_ACCOUNT)));
      event.put("location", cursor.getString(cursor.getColumnIndexOrThrow(Instances.EVENT_LOCATION)));
      event.put("start_day", cursor.getString(cursor.getColumnIndexOrThrow(Instances.START_DAY)));
      event.put("end_day", cursor.getString(cursor.getColumnIndexOrThrow(Instances.END_DAY)));
      event.put("start_minute", cursor.getString(cursor.getColumnIndexOrThrow(Instances.START_MINUTE)));
      event.put("end_minute", cursor.getString(cursor.getColumnIndexOrThrow(Instances.END_MINUTE)));
      event.put("has_attendee_data", cursor.getString(cursor.getColumnIndexOrThrow(Instances.HAS_ATTENDEE_DATA)));
      event.put("is_organizer", cursor.getInt(cursor.getColumnIndexOrThrow(Instances.IS_ORGANIZER)) > 0);

      // An event is recurring if it is an "exception" event (ie. it references its original event id),
      // or if it contains a recurrence rule.
      String rdate = cursor.getString(cursor.getColumnIndexOrThrow(Instances.RDATE));
      String rrule = cursor.getString(cursor.getColumnIndexOrThrow(Instances.RRULE));
      int original_id = cursor.getInt(cursor.getColumnIndexOrThrow(Instances.ORIGINAL_ID));
      boolean isRecurring = rdate != null && !rdate.equals("") ||
                            rrule != null && !rrule.equals("") ||
                            original_id != 0;
      event.put("is_recurring", isRecurring);

      // RSVP status of the PACO user (if they're invited to the event)
      int status = cursor.getInt(cursor.getColumnIndexOrThrow(Instances.SELF_ATTENDEE_STATUS));
      event.put("self_attendee_status",
          status == Attendees.ATTENDEE_STATUS_ACCEPTED ? "accepted" :
          status == Attendees.ATTENDEE_STATUS_DECLINED ? "declined" :
          status == Attendees.ATTENDEE_STATUS_INVITED ? "invited" :
          status == Attendees.ATTENDEE_STATUS_TENTATIVE ? "tentative" :
          status == Attendees.ATTENDEE_STATUS_NONE ? "none" :
          "other-" + status);

      // Indicates whether others are able to view details for this event
      int access = cursor.getInt(cursor.getColumnIndexOrThrow(Instances.ACCESS_LEVEL));
      event.put("access_level",
          access == Instances.ACCESS_CONFIDENTIAL ? "confidential" :
          access == Instances.ACCESS_DEFAULT ? "default" : // use the calendar's default
          access == Instances.ACCESS_PRIVATE ? "private" :
          access == Instances.ACCESS_PUBLIC ? "public" :
          "other-" + access);

      // Count the number of attendees for the event by RSVP status
      // If the event has too many attendees, Android will return no attendees
      // and these will all be set to zero (or one if the PACO user is an attendee)
      Integer numAccepted = 0, numDeclined = 0, numTentative = 0, numRooms = 0;
      long eventId = cursor.getLong(cursor.getColumnIndexOrThrow(Instances.EVENT_ID));
      Cursor attendees = Attendees.query(cr, eventId, new String[] {
        Attendees.ATTENDEE_TYPE, Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_EMAIL,
      });
      while (attendees.moveToNext()) {
        int attendeeType = attendees.getInt(attendees.getColumnIndexOrThrow(Attendees.ATTENDEE_TYPE));
        int attendeeStatus = attendees.getInt(attendees.getColumnIndexOrThrow(Attendees.ATTENDEE_STATUS));
        String attendeeEmail = attendees.getString(attendees.getColumnIndexOrThrow(Attendees.ATTENDEE_EMAIL));

        // The result returned Android for attendeeType appears to be
        // unreliable. So we also check the attendee's email to determinte
        // whether they're a resource.
        if (attendeeType == Attendees.TYPE_RESOURCE ||
            attendeeEmail.endsWith("@resource.calendar.google.com")) {
          numRooms += 1;

        } else {
          switch (attendeeStatus) {
            case Attendees.ATTENDEE_STATUS_ACCEPTED:
              numAccepted += 1;
              break;
            case Attendees.ATTENDEE_STATUS_DECLINED:
              numDeclined += 1;
              break;
            default:
              numTentative += 1;
          }
        }
      }
      event.put("attendees_accepted", numAccepted);
      event.put("attendees_declined", numDeclined);
      event.put("attendees_tentative", numTentative);
      event.put("attendees_rooms", numRooms); // Number of rooms booked for this event

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
