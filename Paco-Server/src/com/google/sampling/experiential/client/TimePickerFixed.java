/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.client;

/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.widgetideas.client.Spinner.SpinnerResources;
import com.google.gwt.widgetideas.client.SpinnerListener;


/**
 *
 * This is a copy of package com.google.gwt.gen2.picker.client;
 * that fixes a bug in which the time editing events do not actually get fired.
 * See bug report:
 * http://code.google.com/p/google-web-toolkit-incubator/issues/detail?id=267
 *
 * @author Bob Evans
 *
 */
public class TimePickerFixed extends Composite implements HasValueChangeHandlers<Date> {
  private class TimeSpinner extends ValueSpinnerFixed {
    private DateTimeFormat dateTimeFormat;

    public TimeSpinner(Date date, DateTimeFormat dateTimeFormat, int step,
        ValueSpinnerResources styles, SpinnerResources images) {
      super(date.getTime(), styles, images);
      this.dateTimeFormat = dateTimeFormat;
      getSpinner().setMinStep(step);
      getSpinner().setMaxStep(step);
      // Refresh value after dateTimeFormat is set
      getSpinner().setValue(date.getTime(), true);
    }

    protected String formatValue(long value) {
      if (dateTimeFormat != null) {
        return dateTimeFormat.format(new Date(value));
      }
      return "";
    }

    protected long parseValue(String value) {
      Date parsedDate = new Date(dateInMillis);
      dateTimeFormat.parse(value, 0, parsedDate);
      return parsedDate.getTime();
    }
  }

  private static final int SECOND_IN_MILLIS = 1000;
  private static final int MINUTE_IN_MILLIS = 60000;
  private static final int HOUR_IN_MILLIS = 3600000;
  private static final int HALF_DAY_IN_MS = 43200000;
  private static final int DAY_IN_MS = 86400000;

  private List<TimeSpinner> timeSpinners = new ArrayList<TimeSpinner>();
  private long dateInMillis;
  private boolean enabled = true;

  private SpinnerListener listener = new SpinnerListener() {
    public void onSpinning(long value) {
      // ValueChangeEvent.fireIfNotEqual(TimePicker.this, new
      // Date(dateInMillis),
      long oldValue = dateInMillis;
      dateInMillis = value;
      ValueChangeEvent.fireIfNotEqual(TimePickerFixed.this, new Date(oldValue), new Date(value));
    };
  };

  /**
   * @param use24Hours if set to true the {@link TimePicker} will use 24h format
   */
  public TimePickerFixed(boolean use24Hours) {
    this(new Date(), use24Hours);
  }

  /**
   * @param date the date providing the initial time to display
   * @param use24Hours if set to true the {@link TimePicker} will use 24h format
   */
  public TimePickerFixed(Date date, boolean use24Hours) {
    this(date, use24Hours ? null : DateTimeFormat.getFormat("aa"),
        use24Hours ? DateTimeFormat.getFormat("hh") : DateTimeFormat.getFormat("hh"),
        DateTimeFormat.getFormat("mm"), DateTimeFormat.getFormat("ss"));
  }

  /**
   * @param date the date providing the initial time to display
   * @param amPmFormat the format to display AM/PM. Can be null to hide AM/PM
   *        field
   * @param hoursFormat the format to display the hours. Can be null to hide
   *        hours field
   * @param minutesFormat the format to display the minutes. Can be null to hide
   *        minutes field
   * @param secondsFormat the format to display the seconds. Can be null to
   *        seconds field
   */
  public TimePickerFixed(Date date, DateTimeFormat amPmFormat, DateTimeFormat hoursFormat,
      DateTimeFormat minutesFormat, DateTimeFormat secondsFormat) {
    this(date, amPmFormat, hoursFormat, minutesFormat, secondsFormat, null, null);
  }

  /**
   * @param date the date providing the initial time to display
   * @param amPmFormat the format to display AM/PM. Can be null to hide AM/PM
   *        field
   * @param hoursFormat the format to display the hours. Can be null to hide
   *        hours field
   * @param minutesFormat the format to display the minutes. Can be null to hide
   *        minutes field
   * @param secondsFormat the format to display the seconds. Can be null to
   *        seconds field
   * @param styles styles to be used by this TimePicker instance
   * @param images images to be used by all nested Spinner widgets
   *
   */
  public TimePickerFixed(Date date, DateTimeFormat amPmFormat, DateTimeFormat hoursFormat,
      DateTimeFormat minutesFormat, DateTimeFormat secondsFormat, ValueSpinnerFixed.ValueSpinnerResources styles,
      SpinnerResources images) {
    this.dateInMillis = date.getTime();
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setStylePrimaryName("gwt-TimePicker");
    if (amPmFormat != null) {
      TimeSpinner amPmSpinner = new TimeSpinner(date, amPmFormat, HALF_DAY_IN_MS, styles, images);
      timeSpinners.add(amPmSpinner);
      horizontalPanel.add(amPmSpinner);
    }
    if (hoursFormat != null) {
      TimeSpinner hoursSpinner = new TimeSpinner(date, hoursFormat, HOUR_IN_MILLIS, styles, images);
      timeSpinners.add(hoursSpinner);
      horizontalPanel.add(hoursSpinner);
    }
    if (minutesFormat != null) {
      TimeSpinner minutesSpinner =
          new TimeSpinner(date, minutesFormat, MINUTE_IN_MILLIS, styles, images);
      timeSpinners.add(minutesSpinner);
      horizontalPanel.add(minutesSpinner);
    }
    if (secondsFormat != null) {
      TimeSpinner secondsSpinner =
          new TimeSpinner(date, secondsFormat, SECOND_IN_MILLIS, styles, images);
      timeSpinners.add(secondsSpinner);
      horizontalPanel.add(secondsSpinner);
    }
    for (TimeSpinner timeSpinner : timeSpinners) {
      for (TimeSpinner nestedSpinner : timeSpinners) {
        if (nestedSpinner != timeSpinner) {
          timeSpinner.getSpinner().addSpinnerListener(nestedSpinner.getSpinnerListener());
        }
      }
      timeSpinner.getSpinner().addSpinnerListener(listener);
    }
    initWidget(horizontalPanel);
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  /**
   * @return the date specified by this {@link TimePicker}
   */
  public Date getDateTime() {
    return new Date(dateInMillis);
  }

  /**
   * @return Gets whether this widget is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @param date the date to be set. Only the date part will be set, the time
   *        part will not be affected
   */
  public void setDate(Date date) {
    // Only change the date part, leave time part untouched
    dateInMillis = (long) ((Math.floor(date.getTime() / DAY_IN_MS) + 1) * DAY_IN_MS) + dateInMillis
        % DAY_IN_MS;
    for (TimeSpinner spinner : timeSpinners) {
      spinner.getSpinner().setValue(dateInMillis, false);
    }
  }

  /**
   * @param date the date to be set. Both date and time part will be set
   */
  public void setDateTime(Date date) {
    dateInMillis = date.getTime();
    for (TimeSpinner spinner : timeSpinners) {
      spinner.getSpinner().setValue(dateInMillis, true);
    }
  }

  /**
   * Sets whether this widget is enabled.
   *
   * @param enabled true to enable the widget, false to disable it
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    for (TimeSpinner spinner : timeSpinners) {
      spinner.setEnabled(enabled);
    }
  }
}
