package com.google.paco.shared.model;

import java.util.Iterator;

import org.joda.time.LocalDate;

import com.google.ical.compat.jodatime.LocalDateIterator;

public abstract class ScheduleIterator implements Iterator<LocalDate> {
  LocalDateIterator iterator;

  public void advanceTo(LocalDate newStart) {
    if (iterator == null) {
      return;
    }

    iterator.advanceTo(newStart);
  }

  @Override
  public boolean hasNext() {
    if (iterator == null) {
      return false;
    }

    return iterator.hasNext();
  }

  @Override
  public LocalDate next() {
    if (iterator == null) {
      return null;
    }

    return iterator.next();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }


  /**
   * Determines whether the specified date is valid according to the schedule.
   * 
   * @param date a date
   * @return whether the date is valid according to the schedule
   */
  // protected abstract boolean isValidDate(LocalDate date);

  /**
   * Compute the valid date in the schedule that is closest to the base date without going over. We
   * accomplish this by iterating through the schedule until it is after the base date and returning
   * a pointer to the previous valid schedule date that we stored.
   * 
   * @param now the base date
   * @return a valid date in the schedule just before or equal to the base date
   */
  /*
   * public LocalDate getCurrentDate(LocalDate now) { // Check pre-conditions if (hasStartDate() ==
   * false || getEvery() < 1) { return null; }
   * 
   * // Don't bother if the base date is before the start date if (now.isBefore(getStartDate())) {
   * return null; }
   * 
   * // Create schedule iterator LocalDateIterator ldi;
   * 
   * try { ldi = LocalDateIteratorFactory.createLocalDateIterator(getRData(), getStartDate(), true);
   * } catch (ParseException e) { e.printStackTrace(); return null; }
   * 
   * // Compute the first possible date before now by subtracting the appropriate amount of epochs
   * LocalDate last = now;
   * 
   * switch (getType()) { case Daily: last = last.minusDays(getEvery()); break; case Weekly: last =
   * last.minusWeeks(getEvery()); break; case Monthly: last = last.minusMonths(getEvery()); break; }
   * 
   * // Skip to this date... ldi.advanceTo(last);
   * 
   * // ...and get the next date. LocalDate date = ldi.next();
   * 
   * // If the next date is not valid... if (!isValidDate(date)) { // Make sure that date is not
   * after the base date, otherwise we've gone to far... if (now.isAfter(date)) { // ..get the next
   * date and start from there date = ldi.next(); last = date; } else { // ...which means this is a
   * bad schedule return null; } }
   * 
   * // Now iterate through the schedule until the next date is after now while (!date.isAfter(now))
   * { // Ensure we don't move past the end date, if it exists if (hasEndDate() &&
   * date.isAfter(getEndDate())) { break; }
   * 
   * // Iterate and save the date! last = date; date = ldi.next(); }
   * 
   * // If we went past the end date, then we have a bad schedule. if (hasEndDate() &&
   * last.isAfter(getEndDate())) { return null; }
   * 
   * return last; }
   */

  /**
   * Compute the next valid date in the schedule after the specified date.
   * 
   * @param now the base date
   * @return the next valid date in the schedule after the specified date now
   */
  /*
   * public LocalDate getNextDate(LocalDate now) { // Check pre-conditions if (hasStartDate() ==
   * false || getEvery() < 1) { return null; }
   * 
   * // Don't bother if we're already past the end date if (hasEndDate() &&
   * now.isAfter(getEndDate())) { return null; }
   * 
   * // Create schedule iterator LocalDateIterator ldi;
   * 
   * try { ldi = LocalDateIteratorFactory.createLocalDateIterator(getRData(), getStartDate(), true);
   * } catch (ParseException e) { e.printStackTrace(); return null; }
   * 
   * // Skip to the base date... ldi.advanceTo(now);
   * 
   * // ..and get the next date LocalDate date = ldi.next();
   * 
   * // If its not valid or we just arrived at the base date, then get the next date if
   * (!isValidDate(date) || now.isEqual(date)) { date = ldi.next(); }
   * 
   * // If there is an end date, ensure we haven't gone past it. if (hasEndDate() &&
   * date.isAfter(getEndDate())) { return null; }
   * 
   * return date; }
   */
}
