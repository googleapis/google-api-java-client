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

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Low-level JSON serializer implementation based on Jackson.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
final class JacksonParser extends JsonParser {

  private final org.codehaus.jackson.JsonParser parser;
  private final JacksonFactory factory;

  @Override
  public JacksonFactory getFactory() {
    return factory;
  }

  JacksonParser(JacksonFactory factory, org.codehaus.jackson.JsonParser parser) {
    this.factory = factory;
    this.parser = parser;
  }

  @Override
  public void close() throws IOException {
    parser.close();
  }

  @Override
  public JsonToken nextToken() throws IOException {
    return JacksonFactory.convert(parser.nextToken());
  }

  @Override
  public String getCurrentName() throws IOException {
    return parser.getCurrentName();
  }

  @Override
  public JsonToken getCurrentToken() {
    return JacksonFactory.convert(parser.getCurrentToken());
  }

  @Override
  public JsonParser skipChildren() throws IOException {
    parser.skipChildren();
    return this;
  }

  @Override
  public String getText() throws IOException {
    return parser.getText();
  }

  @Override
  public byte getByteValue() throws IOException {
    return parser.getByteValue();
  }

  @Override
  public float getFloatValue() throws IOException {
    return parser.getFloatValue();
  }

  @Override
  public int getIntValue() throws IOException {
    return parser.getIntValue();
  }

  @Override
  public short getShortValue() throws IOException {
    return parser.getShortValue();
  }

  @Override
  public BigInteger getBigIntegerValue() throws IOException {
    return parser.getBigIntegerValue();
  }

  @Override
  public BigDecimal getDecimalValue() throws IOException {
    return parser.getDecimalValue();
  }

  @Override
  public double getDoubleValue() throws IOException {
    return parser.getDoubleValue();
  }

  @Override
  public long getLongValue() throws IOException {
    return parser.getLongValue();
  }
}
