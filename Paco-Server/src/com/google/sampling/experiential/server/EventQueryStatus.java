package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.ISO8601DateFormat;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.Views;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class EventQueryStatus extends PacoResponse {
  private List<EventDAO> events = Lists.newArrayList();
  
  @JsonIgnore
  Float pacoProtocol;
  
  @JsonIgnore
  ObjectMapper mapper = JsonConverter.getObjectMapper();
  
  public static final Logger log = Logger.getLogger(EventQueryStatus.class.getName());
  
  public EventQueryStatus(Float pacoProtocol) { 
    this.pacoProtocol = pacoProtocol;
  }
  
  public EventQueryStatus() { 
    this.pacoProtocol = 5.0f;
  }

  public List<EventDAO> getEvents() {
    return events;
  }

  public void setEvents(List<EventDAO> events) {
    this.events = events;
  }
  
  @Override
  public String toString() { 
    String results = "";
    final String error = " during string conversion";
    try {
      if (pacoProtocol != null && pacoProtocol < 5) {
        results = mapper.writerWithView(Views.V4.class).writeValueAsString(this);
      } else {
        mapper.setDateFormat(new ISO8601DateFormat());
        results = mapper.writerWithView(Views.V5.class).writeValueAsString(this);
      }
    } catch (JsonGenerationException e) {
      setErrorMessage(ErrorMessages.JSON_GENERATION_EXCEPTION.getDescription() + error);
    } catch (JsonMappingException e) {
      setErrorMessage(ErrorMessages.JSON_MAPPING_EXCEPTION.getDescription() + error);
    } catch (IOException e) {
      setErrorMessage(ErrorMessages.IO_EXCEPTION.getDescription() + error);
    }
    return results;
  }
}
