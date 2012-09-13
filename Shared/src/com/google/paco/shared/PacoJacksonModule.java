package com.google.paco.shared;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class PacoJacksonModule extends SimpleModule {
  private final static Version VERSION = new Version(0, 1, 0, null);

  public PacoJacksonModule() {
    super("Paco", VERSION);

    // Add serializers
    this.addSerializer(LocalTime.class, new LocalTimeSerializer());
    this.addSerializer(LocalDate.class, new LocalDateSerializer());
    this.addSerializer(DateTime.class, new DateTimeSerializer());

    // Add deserializers
    this.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
    this.addDeserializer(LocalDate.class, new LocalDateDeserializer());
    this.addDeserializer(DateTime.class, new DateTimeDeserializer());
  }

  public class DateTimeSerializer extends SerializerBase<DateTime> {
    protected DateTimeSerializer() {
      super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      jgen.writeString(value.toString());
    }
  }

  public class DateTimeDeserializer extends StdScalarDeserializer<DateTime> {
    public DateTimeDeserializer() {
      super(DateTime.class);
    }

    @Override
    public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
      return DateTime.parse(jp.getText());
    }
  }

  public class LocalTimeSerializer extends SerializerBase<LocalTime> {
    public LocalTimeSerializer() {
      super(LocalTime.class);
    }

    @Override
    public void serialize(LocalTime value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      jgen.writeString(value.toString());
    }
  }

  public class LocalTimeDeserializer extends StdScalarDeserializer<LocalTime> {
    public LocalTimeDeserializer() {
      super(LocalTime.class);
    }

    @Override
    public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
      return LocalTime.parse(jp.getText());
    }
  }

  public class LocalDateSerializer extends SerializerBase<LocalDate> {
    public LocalDateSerializer() {
      super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      jgen.writeString(value.toString());
    }
  }

  public class LocalDateDeserializer extends StdScalarDeserializer<LocalDate> {
    public LocalDateDeserializer() {
      super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
      return LocalDate.parse(jp.getText());
    }
  }
}
