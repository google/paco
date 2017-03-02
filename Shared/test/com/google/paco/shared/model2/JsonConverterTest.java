package com.google.paco.shared.model2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import com.pacoapp.paco.shared.model2.JsonConverter;

public class JsonConverterTest {

  @Test
  public void testParenthesesWithJackson()  {
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    String json = "(\"foo\", \"bar\")";
    List<String> strs;
    try {
      strs = mapper.readValue(json,  new TypeReference<List<String>>() {});
      fail("should have thrown a parse exception on parenthesis");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //assertEquals("should have two items", 2, strs.size());
  }

  @Test
  public void testParenthesesWithOrgJson()  {
    String json = "(\"foo\", \"bar\")";
    JSONArray strs;
    try {
      strs = new JSONArray(json);
      assertEquals("should have two items", 2, strs.length());
      //fail("should have thrown a parse exception on parenthesis");
    } catch (JSONException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testParenthesesWithOrgJson_array()  {
    String json = "([\"foo\"], [\"bar\"])";
    JSONArray strs;
    try {
      strs = new JSONArray(json);
      assertEquals("should have two items", 2, strs.length());
      assertEquals("should have two items", 1, ((JSONArray)strs.get(0)).length());
      assertEquals("should have two items", 1, ((JSONArray)strs.get(1)).length());
      //fail("should have thrown a parse exception on parenthesis");
    } catch (JSONException e) {
      e.printStackTrace();
    }

  }
}
