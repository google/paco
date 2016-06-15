package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.pacoapp.paco.shared.model2.JsonConverter;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTimeZone;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;


public class ExperimentServletExperimentsPopularLoadHandler extends ExperimentServletShortLoadHandler {

    public ExperimentServletExperimentsPopularLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
        super(email, timezone, limit, cursor, pacoProtocol);
    }

    protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
        ExperimentQueryResult result = ExperimentServiceFactory.getExperimentService().getExperimentsPublishedPublicly(timezone, limit, cursor, email);
        //MARIOS TODO RETURN POPULAR ONLY
        cursor = result.getCursor();
        return result.getExperiments();
    }
/*
    private void computeStatsFromEventsTable(HttpServletRequest req, HttpServletResponse resp, User user,
                                             Long experimentId, String whoParam, DateTimeZone timeZoneForClient)
            throws IOException,
            JsonGenerationException,
            JsonMappingException {
        String fullQuery = "experimentId=" + experimentId;
        if (!Strings.isNullOrEmpty(whoParam)) {
            fullQuery += ":who=" + whoParam;
        }
        List<Query> queryFilters = new QueryParser().parse(fullQuery);
        String cursor = req.getParameter("cursor");
        String limitStr = req.getParameter("limit");
        int limit = 0;
        if (!Strings.isNullOrEmpty(limitStr)) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance()
                .getEventsInBatchesOneBatch(queryFilters,
                        AuthUtil.getEmailOfUser(req,
                                user),
                        timeZoneForClient, limit,
                        cursor);

        Map<String, ParticipantReport> participantReports = Maps.newConcurrentMap();
        for (Event event : eventQueryResultPair.getEvents()) {
            ParticipantReport participantReport = participantReports.get(event.getWho());
            if (participantReport == null) {
                participantReport = new ParticipantReport(event.getWho(), timeZoneForClient);
                participantReports.put(event.getWho(), participantReport);
            }
            participantReport.addEvent(event);
        }

        List<ParticipationStats.ParticipantParticipationStat> participantStats = Lists.newArrayList();

        List<ParticipantReport> participantReportValues = Lists.newArrayList(participantReports.values());
        for (ParticipantReport report : participantReportValues) {
            report.computeStats();
            participantStats.add(new ParticipationStats.ParticipantParticipationStat(
                    report.getWho(),
                    report.getTodaysScheduledCount(),
                    report.getTodaysSignaledResponseCount(),
                    report.getTodaysSelfReportResponseCount(),
                    report.getScheduledCount(),
                    report.getSignaledResponseCount(),
                    report.getSelfReportResponseCount()));
        }

        Collections.sort(participantStats);
        String nextCursor = eventQueryResultPair.getCursor();
        if (nextCursor == null || nextCursor.equals(cursor)) {
            nextCursor = null;
        }
        ParticipationStats participationStats = new ParticipationStats(participantStats, nextCursor);

        PrintWriter writer = resp.getWriter();
        ObjectMapper mapper = JsonConverter.getObjectMapper();
        writer.write(mapper.writeValueAsString(participationStats));
    }
*/
}
