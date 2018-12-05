package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;


public class ExperimentServletExperimentsNewLoadHandler extends ExperimentServletShortLoadHandler {

    public ExperimentServletExperimentsNewLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
        super(email, timezone, limit, cursor, pacoProtocol);
    }

    protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {

        ExperimentQueryResult result = ((DefaultExperimentService)ExperimentServiceFactory.getExperimentService()).getExperimentsPublishedPubliclyNew(timezone, limit, cursor, email);

        cursor = result.getCursor();

        return result.getExperiments();
    }
}
