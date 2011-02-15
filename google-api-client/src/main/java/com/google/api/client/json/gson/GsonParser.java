// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.api.client.json.gson;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level JSON serializer implementation based on GSON.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public class GsonParser extends JsonParser {
  private final JsonReader reader;
  private final GsonFactory factory;

  private List<String> currentNameStack = new ArrayList<String>();
  private JsonToken currentToken;
  private String currentText;

  GsonParser(GsonFactory factory, JsonReader reader) {
    this.factory = factory;
    this.reader = reader;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  @Override
  public String getCurrentName() {
    return currentNameStack.isEmpty() ? null : currentNameStack.get(currentNameStack.size() - 1);
  }

  @Override
  public JsonToken getCurrentToken() {
    return currentToken;
  }

  @Override
  public JsonFactory getFactory() {
    return factory;
  }

  @Override
  public byte getByteValue() {
    checkNumber();
    return Byte.valueOf(currentText);
  }

  @Override
  public short getShortValue() {
    checkNumber();
    return Short.valueOf(currentText);
  }


  @Override
  public int getIntValue() {
    checkNumber();
    return Integer.valueOf(currentText);
  }

  @Override
  public float getFloatValue() {
    checkNumber();
    return Float.valueOf(currentText);
  }

  @Override
  public BigInteger getBigIntegerValue() {
    checkNumber();
    return new BigInteger(currentText);
  }

  @Override
  public BigDecimal getDecimalValue() {
    checkNumber();
    return new BigDecimal(currentText);
  }

  @Override
  public double getDoubleValue() {
    checkNumber();
    return Double.valueOf(currentText);
  }

  @Override
  public long getLongValue() {
    checkNumber();
    return Long.valueOf(currentText);
  }

  private void checkNumber() {
    Preconditions.checkArgument(
        currentToken == JsonToken.VALUE_NUMBER_INT || currentToken == JsonToken.VALUE_NUMBER_FLOAT);
  }

  @Override
  public String getText() {
    return currentText;
  }

  @Override
  public JsonToken nextToken() throws IOException {
    if (currentToken != null) {
      switch (currentToken) {
        case START_ARRAY:
          reader.beginArray();
          currentNameStack.add(null);
          break;
        case START_OBJECT:
          reader.beginObject();
          currentNameStack.add(null);
          break;
      }
    }
    switch (reader.peek()) {
      case BEGIN_ARRAY:
        currentText = "[";
        currentToken = JsonToken.START_ARRAY;
        break;
      case END_ARRAY:
        currentText = "]";
        currentToken = JsonToken.END_ARRAY;
        currentNameStack.remove(currentNameStack.size() - 1);
        reader.endArray();
        break;
      case BEGIN_OBJECT:
        currentText = "{";
        currentToken = JsonToken.START_OBJECT;
        break;
      case END_OBJECT:
        currentText = "}";
        currentToken = JsonToken.END_OBJECT;
        currentNameStack.remove(currentNameStack.size() - 1);
        reader.endObject();
        break;
      case BOOLEAN:
        if (reader.nextBoolean()) {
          currentText = "true";
          currentToken = JsonToken.VALUE_TRUE;
        } else {
          currentText = "false";
          currentToken = JsonToken.VALUE_FALSE;
        }
        break;
      case NULL:
        currentText = "null";
        currentToken = JsonToken.VALUE_NULL;
        reader.nextNull();
        break;
      case STRING:
        currentText = reader.nextString();
        currentToken = JsonToken.VALUE_STRING;
        break;
      case NUMBER:
        currentText = reader.nextString();
        currentToken = currentText.indexOf('.') == -1
            ? JsonToken.VALUE_NUMBER_INT : JsonToken.VALUE_NUMBER_FLOAT;
        break;
      case NAME:
        currentText = reader.nextName();
        currentToken = JsonToken.FIELD_NAME;
        currentNameStack.set(currentNameStack.size() - 1, currentText);
        break;
      default:
        currentText = null;
        currentToken = null;
    }
    return currentToken;
  }

  @Override
  public JsonParser skipChildren() throws IOException {
    switch (currentToken) {
      case START_ARRAY:
        reader.skipValue();
        currentText = "]";
        currentToken = JsonToken.END_ARRAY;
        break;
      case START_OBJECT:
        reader.skipValue();
        currentText = "}";
        currentToken = JsonToken.END_OBJECT;
        break;
    }
    return this;
  }
}
