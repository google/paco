package com.google.sampling.experiential.server;

import java.util.List;

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
