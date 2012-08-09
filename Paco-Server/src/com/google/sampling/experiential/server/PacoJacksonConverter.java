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
    if (resource instanceof ObserverExperimentResource) {
      getObjectMapper().setSerializationConfig(
          getObjectMapper().getSerializationConfig().withView(Experiment.Observer.class));
    } else if (resource instanceof PacoExperimentResource) {
      getObjectMapper().setSerializationConfig(
          getObjectMapper().getSerializationConfig().withView(Experiment.Viewer.class));
    } else if (resource instanceof PacoResource) {
      getObjectMapper().setSerializationConfig(
          getObjectMapper().getSerializationConfig().withView(Experiment.Summary.class));
    } else {
      getObjectMapper().setSerializationConfig(
          getObjectMapper().getSerializationConfig().withView(Object.class));
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
