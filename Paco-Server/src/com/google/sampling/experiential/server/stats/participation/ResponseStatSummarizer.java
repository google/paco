package com.google.sampling.experiential.server.stats.participation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class ResponseStatSummarizer {

  /**
   * Computes the total statistics for one experiment across all participants.
   *
   *  Throws an exception if there are stats for multiple experiments.
   *
   * @param stats
   * @return
   */
//  public ResponseStat totalExperimentResponseStat(List<ResponseStat> stats) {
//    if (stats == null || stats.isEmpty()) {
//      return null;
//    }
//    Long experimentId = null;
//    String who = null;
//    DateTime date = null;
//    int schedRTotal = 0;
//    int missedRTotal = 0;
//    int selfRTotal = 0;
//
//    for (ResponseStat responseStat : stats) {
//      if (experimentId == null) {
//        experimentId = responseStat.experimentId;
//      } else if (experimentId.longValue() != responseStat.experimentId) {
//        throw new IllegalArgumentException("There are stats for multiple experiments. Only send stats for one experiment.");
//      }
//      schedRTotal += responseStat.schedR;
//      missedRTotal += responseStat.missedR;
//      selfRTotal += responseStat.selfR;
//    }
//    return new ResponseStat(experimentId, null, who, date, schedRTotal, missedRTotal, selfRTotal);
//  }

  /**
   * Computes the total statistics for each person in an experiment.
   * There is a different row for each day for each participant.
   *
   *
   *
   * @param stats
   * @param dateTime TODO
   * @return
   */
  public List<ResponseStat> totalExperimentResponseStatsByWho(List<ResponseStat> stats, DateTime dateTime) {
    return totalExperimentResponseStatsByWho(stats, null, dateTime);
  }

  /**
   * Computes the total statistics for each person in an experimentGroup within an experiment
   * There is a different row for each day for each participant.
   *
   * @param stats
   * @param dateTime TODO
   * @return
   */
  public List<ResponseStat> totalExperimentResponseStatsByWho(List<ResponseStat> stats, String experimentGroup, DateTime dateTime) {

    if (stats == null || stats.isEmpty()) {
      return Lists.newArrayList();
    }

    Map<String, ResponseStat> statsByWho = Maps.newConcurrentMap();
    Long experimentId = null;

    for (ResponseStat responseStat : stats) {

      if (experimentId == null) {
        experimentId = responseStat.experimentId;
      } else if (experimentId.longValue() != responseStat.experimentId) {
        throw new IllegalArgumentException("There are stats for multiple experiments. Only send stats for one experiment.");
      }

      String who = responseStat.who;
      if (who == null) {
        continue;
      }

      ResponseStat existingStatForWho = statsByWho.get(who);
      if (existingStatForWho == null) {
        responseStat = new ResponseStat(experimentId, experimentGroup, who, dateTime, responseStat.schedR, responseStat.missedR, responseStat.selfR, responseStat.getLastContactDateTime());
        statsByWho.put(who, responseStat);
      } else {
        existingStatForWho.schedR +=  responseStat.schedR;
        existingStatForWho.missedR += responseStat.missedR;
        existingStatForWho.selfR += responseStat.selfR;
        if (existingStatForWho.getLastContactDateTime() != null &&
                responseStat.getLastContactDateTime() != null &&
                        responseStat.getLastContactDateTime().isAfter(existingStatForWho.getLastContactDateTime())) {
          existingStatForWho.setLastContactDateTime(responseStat.getLastContactDateTime());
        }
      }
    }

    List<ResponseStat> results = Lists.newArrayList();
    for (Entry<String, ResponseStat> entry : statsByWho.entrySet()) {
      results.add(entry.getValue());

    }
    Collections.sort(results);
    return results;
  }

  /**
   * Computes the total statistics for one person in an experiment.
   *
   *  Throws an exception if there are stats for multiple experiments or multiple people.
   *
   * @param stats
   * @return
   */
//  public ResponseStat totalExperimentWhoResponseStat(List<ResponseStat> stats) {
//    if (stats == null || stats.isEmpty()) {
//      return null;
//    }
//
//    Long experimentId = null;
//    String who = null;
//    DateTime date = null;
//    int schedRTotal = 0;
//    int missedRTotal = 0;
//    int selfRTotal = 0;
//
//    for (ResponseStat responseStat : stats) {
//      if (experimentId == null) {
//        experimentId = responseStat.experimentId;
//      } else if (experimentId.longValue() != responseStat.experimentId) {
//        throw new IllegalArgumentException("There are stats for multiple experiments. Only send stats for one experiment.");
//      }
//
//      if (who == null) {
//        who = responseStat.who;
//      } else if (!who.equals(responseStat.who)) {
//        throw new IllegalArgumentException("There are stats for multiple people. Only send stats for one experiment and one participant.");
//      }
//      schedRTotal += responseStat.schedR;
//      missedRTotal += responseStat.missedR;
//      selfRTotal += responseStat.selfR;
//
//    }
//
//    return new ResponseStat(experimentId, null, who, date, schedRTotal, missedRTotal, selfRTotal);
//  }

  /**
   * Computes the total statistics for one date in an experiment.
   *
   *  Throws an exception if there are stats for multiple dates.
   *
   * @param stats
   * @return
   */
//  public ResponseStat totalExperimentResponseStatsOnDate(List<ResponseStat> stats) {
//    if (stats == null || stats.isEmpty()) {
//      return null;
//    }
//
//    Long experimentId = null;
//    String who = null;
//    DateTime date = null;
//    int schedRTotal = 0;
//    int missedRTotal = 0;
//    int selfRTotal = 0;
//
//    for (ResponseStat responseStat : stats) {
//      if (experimentId == null) {
//        experimentId = responseStat.experimentId;
//      } else if (experimentId.longValue() != responseStat.experimentId) {
//        throw new IllegalArgumentException("There are stats for multiple experiments. Only send stats for one experiment.");
//      }
//
//      if (date == null) {
//        date = responseStat.date;
//      } else if (!date.equals(responseStat.date)) {
//        throw new IllegalArgumentException("There are stats for multiple dates. Only send stats for one experiment and one date.");
//      }
//      schedRTotal += responseStat.schedR;
//      missedRTotal += responseStat.missedR;
//      selfRTotal += responseStat.selfR;
//
//    }
//
//    return new ResponseStat(experimentId, null, who, date, schedRTotal, missedRTotal, selfRTotal);
//  }

  public List<ResponseStat> dailyTotalsByWho(List<ResponseStat> stats) {

    if (stats == null || stats.isEmpty()) {
      return Lists.newArrayList();
    }

    Map<String, Map<DateTime, ResponseStat>> statsByWho = Maps.newConcurrentMap();
    Long experimentId = null;

    for (ResponseStat responseStat : stats) {

      if (experimentId == null) {
        experimentId = responseStat.experimentId;
      } else if (experimentId.longValue() != responseStat.experimentId) {
        throw new IllegalArgumentException("There are stats for multiple experiments. Only send stats for one experiment.");
      }

      String who = responseStat.who;
      if (who == null) {
        continue;
      }

      Map<DateTime, ResponseStat> existingStatsForWho = statsByWho.get(who);
      DateTime dateTime = responseStat.date;

      ResponseStat sumResponseStat = null;
      if (existingStatsForWho != null) {
        sumResponseStat = existingStatsForWho.get(dateTime);
      }

      if (sumResponseStat == null) {
        sumResponseStat = new ResponseStat(experimentId, null, who, dateTime, responseStat.schedR, responseStat.missedR, responseStat.selfR, responseStat.getLastContactDateTime());
      } else {
        sumResponseStat.schedR +=  responseStat.schedR;
        sumResponseStat.missedR += responseStat.missedR;
        sumResponseStat.selfR += responseStat.selfR;
        if (sumResponseStat.getLastContactDateTime() != null &&
                responseStat.getLastContactDateTime() != null &&
                        responseStat.getLastContactDateTime().isAfter(sumResponseStat.getLastContactDateTime())) {
          sumResponseStat.setLastContactDateTime(

                                             responseStat.getLastContactDateTime());
        }
      }
      if (existingStatsForWho == null) {
        existingStatsForWho = Maps.newConcurrentMap();
        statsByWho.put(who, existingStatsForWho);
      }
      existingStatsForWho.put(dateTime, sumResponseStat);
    }

    List<ResponseStat> results = Lists.newArrayList();
    for (Entry<String, Map<DateTime, ResponseStat>> entry : statsByWho.entrySet()) {
      Map<DateTime, ResponseStat> value = entry.getValue();
      for (Entry<DateTime, ResponseStat> dateTimeEntry : value.entrySet()) {
        results.add(dateTimeEntry.getValue());

      }
    }
    Collections.sort(results);
    return results;
  }

  /**
   * Returns three values in the ResponseStat: the total for each type of signal
   *
   * @param responseStatsByParticipant
   * @param experimentId
   * @return
   */
  public ResponseStat totalExperimentResponseStats(List<ResponseStat> responseStatsByParticipant, Long experimentId) {
    int schedR = 0;
    int missedR = 0;
    int selfR = 0;
    for (ResponseStat responseStat : responseStatsByParticipant) {
      schedR += responseStat.schedR;
      missedR += responseStat.missedR;
      selfR += responseStat.selfR;

    }
    return new ResponseStat(experimentId, null, null, schedR, missedR, selfR);
  }


}
