// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.api.client.json.gson;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Low-level JSON serializer implementation based on GSON.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public class GsonGenerator extends JsonGenerator {
  private final JsonWriter writer;
  private final GsonFactory factory;

  GsonGenerator(GsonFactory factory, JsonWriter writer) {
    this.factory = factory;
    this.writer = writer;
  }

  @Override
  public void flush() throws IOException {
    writer.flush();
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  @Override
  public JsonFactory getFactory() {
    return factory;
  }

  @Override
  public void writeBoolean(boolean state) throws IOException {
    writer.value(state);
  }

  @Override
  public void writeEndArray() throws IOException {
    writer.endArray();
  }

  @Override
  public void writeEndObject() throws IOException {
    writer.endObject();
  }

  @Override
  public void writeFieldName(String name) throws IOException {
    writer.name(name);
  }

  @Override
  public void writeNull() throws IOException {
    writer.nullValue();
  }

  @Override
  public void writeNumber(int v) throws IOException {
    writer.value(v);
  }

  @Override
  public void writeNumber(long v) throws IOException {
    writer.value(v);
  }

  @Override
  public void writeNumber(BigInteger v) throws IOException {
    writer.value(v);
  }

  @Override
  public void writeNumber(double v) throws IOException {
    writer.value(v);
  }

  @Override
  public void writeNumber(float v) throws IOException {
    writer.value(v);
  }

  @Override
  public void writeNumber(BigDecimal v) throws IOException {
    writer.value(v);
  }

  /**
   * Hack to support numbers encoded as a string for GSON writer.
   */
  static final class StringNumber extends Number {
    private static final long serialVersionUID = 1L;
    private final String encodedValue;

    StringNumber(String encodedValue) {
      this.encodedValue = encodedValue;
    }

    @Override
    public double doubleValue() {
      return 0;
    }

    @Override
    public float floatValue() {
      return 0;
    }

    @Override
    public int intValue() {
      return 0;
    }

    @Override
    public long longValue() {
      return 0;
    }

    @Override
    public String toString() {
      return encodedValue;
    }
  }

  @Override
  public void writeNumber(String encodedValue) throws IOException {
    writer.value(new StringNumber(encodedValue));
  }

  @Override
  public void writeStartArray() throws IOException {
    writer.beginArray();
  }

  @Override
  public void writeStartObject() throws IOException {
    writer.beginObject();
  }

  @Override
  public void writeString(String value) throws IOException {
    writer.value(value);
  }
}
