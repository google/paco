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

//TODO
//Temporary
import java.util.ArrayList;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;


public class ExperimentServletExperimentsPopularLoadHandler extends ExperimentServletShortLoadHandler {

    public ExperimentServletExperimentsPopularLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
        super(email, timezone, limit, cursor, pacoProtocol);
    }

    protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
        ExperimentQueryResult result = ExperimentServiceFactory.getExperimentService().getExperimentsPublishedPublicly(timezone, limit, cursor, email);
        //TODO
        //MARIOS FIX NAIVE APPROACH!
        ParticipationStatsService ps =  new ParticipationStatsService();
        List<ExperimentDAO> experiments = result.getExperiments();
        ArrayList<Integer> participantsCount = new ArrayList<Integer>();

        //No "pair" collection as in C++, so "manually" sort for now

        for(ExperimentDAO e : experiments){
            List<ResponseStat> totalParticipationStats = ps.getTotalByParticipant(e.getId());
            participantsCount.add(totalParticipationStats.size());
        }
        //using bubble sort due to simple code/fewer lines --- NEEDS FIXING, URGENTLY
        for(int i = 0; i < participantsCount.size() - 1 ; i++){
            for(int j = i+1; j < participantsCount.size(); j++){
                if(participantsCount.get(i) < participantsCount.get(j)){
                    ExperimentDAO tmpExp = experiments.get(i);
                    experiments.set(i, experiments.get(j));
                    experiments.set(j, tmpExp);

                    Integer tmp = participantsCount.get(i);
                    participantsCount.set(i, participantsCount.get(j));
                    participantsCount.set(j, tmp);
                }
            }
        }

        cursor = result.getCursor();

        //return result.getExperiments();
        return experiments;
    }
}
