// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Experiment;

import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class PacoJacksonConverter extends JacksonConverter {
  private static PacoJacksonConverter instance;

  public static synchronized PacoJacksonConverter getInstance() {
    if (instance == null) {
      instance = new PacoJacksonConverter();
    }
    return instance;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.restlet.ext.jackson.JacksonConverter#toRepresentation(java.lang.Object,
   * org.restlet.representation.Variant, org.restlet.resource.Resource)
   */
  @Override
  public Representation toRepresentation(Object object, Variant variant, Resource resource) {
    if (resource instanceof ExperimentsResource) {
      getObjectMapper().setSerializationConfig(getObjectMapper().getSerializationConfig().withView(Experiment.Views.Summary.class));
    } else if (resource instanceof ExperimentResource) {
      getObjectMapper().setSerializationConfig(getObjectMapper().getSerializationConfig().withView(Experiment.Views.Subject.class));
    } else if (resource instanceof SubjectExperimentsResource) {
      getObjectMapper().setSerializationConfig(getObjectMapper().getSerializationConfig().withView(Experiment.Views.Summary.class));
    } else if (resource instanceof SubjectExperimentResource) {
      getObjectMapper().setSerializationConfig(getObjectMapper().getSerializationConfig().withView(Experiment.Views.Subject.class));
    } else if (resource instanceof ObserverExperimentsResource) {
      getObjectMapper().setSerializationConfig(getObjectMapper().getSerializationConfig().withView(Experiment.Views.Summary.class));
    } else if (resource instanceof ObserverExperimentResource) {
      getObjectMapper().setSerializationConfig(getObjectMapper().getSerializationConfig().withView(Experiment.Views.Observer.class));
    }

    return super.toRepresentation(object, variant, resource);
  }

  public static void replaceConverter() {
    List<ConverterHelper> converters = Engine.getInstance().getRegisteredConverters();

    for (ConverterHelper converter : converters) {
      if (converter.getClass().equals(JacksonConverter.class)) {
        converters.remove(converter);
        break;
      }
    }

    converters.add(PacoJacksonConverter.getInstance());
  }
}
