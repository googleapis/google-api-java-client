/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.json.jackson;

import com.google.api.client.json.JsonGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Low-level JSON serializer implementation based on Jackson.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
final class JacksonGenerator extends JsonGenerator {
  private final org.codehaus.jackson.JsonGenerator generator;
  private final JacksonFactory factory;

  @Override
  public JacksonFactory getFactory() {
    return factory;
  }

  JacksonGenerator(JacksonFactory factory, org.codehaus.jackson.JsonGenerator generator) {
    this.factory = factory;
    this.generator = generator;
  }

  @Override
  public void flush() throws IOException {
    generator.flush();
  }

  @Override
  public void close() throws IOException {
    generator.close();
  }

  @Override
  public void writeBoolean(boolean state) throws IOException {
    generator.writeBoolean(state);
  }

  @Override
  public void writeEndArray() throws IOException {
    generator.writeEndArray();
  }

  @Override
  public void writeEndObject() throws IOException {
    generator.writeEndObject();
  }

  @Override
  public void writeFieldName(String name) throws IOException {
    generator.writeFieldName(name);
  }

  @Override
  public void writeNull() throws IOException {
    generator.writeNull();
  }

  @Override
  public void writeNumber(int v) throws IOException {
    generator.writeNumber(v);
  }

  @Override
  public void writeNumber(long v) throws IOException {
    generator.writeNumber(v);
  }

  @Override
  public void writeNumber(BigInteger v) throws IOException {
    generator.writeNumber(v);
  }

  @Override
  public void writeNumber(double v) throws IOException {
    generator.writeNumber(v);
  }

  @Override
  public void writeNumber(float v) throws IOException {
    generator.writeNumber(v);
  }

  @Override
  public void writeNumber(BigDecimal v) throws IOException {
    generator.writeNumber(v);
  }

  @Override
  public void writeNumber(String encodedValue) throws IOException {
    generator.writeNumber(encodedValue);
  }

  @Override
  public void writeStartArray() throws IOException {
    generator.writeStartArray();
  }

  @Override
  public void writeStartObject() throws IOException {
    generator.writeStartObject();
  }

  @Override
  public void writeString(String value) throws IOException {
    generator.writeString(value);
  }
}
