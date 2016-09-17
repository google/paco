package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.stats.participation.ParticipationStatsService;
import com.google.sampling.experiential.server.stats.participation.ResponseStat;
import com.google.sampling.experiential.server.stats.usage.UsageStat;
import com.google.sampling.experiential.server.stats.usage.UsageStatsEntityManager;
import com.google.sampling.experiential.server.stats.usage.UsageStatsReport;
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
        ExperimentQueryResult result =((DefaultExperimentService)ExperimentServiceFactory.getExperimentService()).getExperimentsPublishedPubliclyPopular(timezone, limit, cursor, email);
        cursor = result.getCursor();
        List<ExperimentDAO> e = result.getExperiments();
        return result.getExperiments();
    }
}
