package com.google.sampling.experiential.server;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.util.ISO8601DateFormat;

import com.pacoapp.paco.shared.model2.Views;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CustomResponse extends PacoResponse {
  String response;

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }
  @Override
  public String toString() { 
    String results = response;
    return results;
  }
}
