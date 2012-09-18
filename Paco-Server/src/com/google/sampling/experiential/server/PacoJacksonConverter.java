// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.paco.shared.PacoJacksonModule;
import com.google.paco.shared.model.Experiment;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.joda.time.DateTime;
import org.restlet.data.Tag;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
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
   * @see org.restlet.ext.jackson.JacksonConverter#createObjectMapper()
   */
  @Override
  protected ObjectMapper createObjectMapper() {
    ObjectMapper mapper = super.createObjectMapper();

    // Set configuration
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

    // Add custom module
    mapper.registerModule(new PacoJacksonModule());

    return mapper;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.restlet.ext.jackson.JacksonConverter#toRepresentation(java.lang.Object,
   * org.restlet.representation.Variant, org.restlet.resource.Resource)
   */
  @Override
  public Representation toRepresentation(Object object, Variant variant, Resource resource) {
    ObjectMapper mapper = getObjectMapper();

    // Set view
    if (resource instanceof ObserverExperimentResource) {
      mapper.setSerializationConfig(mapper.getSerializationConfig().withView(
          Experiment.Observer.class));
    } else if (resource instanceof PacoExperimentResource) {
      mapper.setSerializationConfig(mapper.getSerializationConfig().withView(
          Experiment.Subject.class));
    } else if (resource instanceof PacoResource) {
      mapper.setSerializationConfig(mapper.getSerializationConfig().withView(
          Experiment.Viewer.class));
    } else {
      mapper.setSerializationConfig(mapper.getSerializationConfig().withView(Object.class));
    }

    // Jsonify
    Representation representation = super.toRepresentation(object, variant, resource);

    // Set tag and modification date
    if (object instanceof Experiment) {
      Experiment experiment = (Experiment) object;
      representation.setModificationDate(experiment.getModificationDate().toDate());
      representation.setTag(new Tag(Long.toString(experiment.getVersion()), false));
    } else if (object instanceof List<?>) {
      List<?> list = (List<?>) object;

      if (!list.isEmpty()) {
        Object first = list.get(0);

        if (first instanceof Experiment) {
          @SuppressWarnings("unchecked")
          List<Experiment> experiments = (List<Experiment>) object;

          DateTime modificationDate = null;

          for (Experiment experiment : experiments) {
            if (modificationDate == null
                || modificationDate.isBefore(experiment.getModificationDate())) {
              modificationDate = experiment.getModificationDate();
            }
          }

          representation.setModificationDate(modificationDate.toDate());
        }
      }
    }

    return representation;
  }

  /**
   * Replaces the default jackson converted with our own subclass.
   */
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
