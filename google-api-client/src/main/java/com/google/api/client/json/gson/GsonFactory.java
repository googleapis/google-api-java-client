// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.api.client.json.gson;

import com.google.api.client.json.JsonEncoding;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Strings;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Low-level JSON library implementation based on GSON.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public class GsonFactory extends JsonFactory {

  @Override
  public JsonParser createJsonParser(InputStream in) {
    return createJsonParser(new InputStreamReader(in, Strings.UTF8_CHARSET));
  }

  @Override
  public JsonParser createJsonParser(String value) {
    return createJsonParser(new StringReader(value));
  }

  @Override
  public JsonParser createJsonParser(Reader reader) {
    return new GsonParser(this, new JsonReader(reader));
  }

  @Override
  public JsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc) {
    return createJsonGenerator(new OutputStreamWriter(out, Strings.UTF8_CHARSET));
  }

  @Override
  public JsonGenerator createJsonGenerator(Writer writer) {
    return new GsonGenerator(this, new JsonWriter(writer));
  }
}
