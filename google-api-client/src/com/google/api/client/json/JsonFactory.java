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

package com.google.api.client.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Abstract low-level JSON factory.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public abstract class JsonFactory {

  /**
   * Returns a new instance of a low-level JSON parser for the given input stream.
   *
   * @param in input stream
   * @return new instance of a low-level JSON parser
   * @throws IOException if failed
   */
  public abstract JsonParser createJsonParser(InputStream in) throws IOException;

  /**
   * Returns a new instance of a low-level JSON parser for the given string value.
   *
   * @param value string value
   * @return new instance of a low-level JSON parser
   * @throws IOException if failed
   */
  public abstract JsonParser createJsonParser(String value) throws IOException;

  /**
   * Returns a new instance of a low-level JSON parser for the given reader.
   *
   * @param reader reader
   * @return new instance of a low-level JSON parser
   * @throws IOException if failed
   */
  public abstract JsonParser createJsonParser(Reader reader) throws IOException;

  /**
   * Returns a new instance of a low-level JSON serializer for the given output stream and encoding.
   *
   * @param out output stream
   * @param enc encoding
   * @return new instance of a low-level JSON serializer
   * @throws IOException if failed
   */
  public abstract JsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc)
      throws IOException;

  /**
   * Returns a new instance of a low-level JSON serializer for the given writer.
   *
   * @param writer writer
   * @return new instance of a low-level JSON serializer
   * @throws IOException if failed
   */
  public abstract JsonGenerator createJsonGenerator(Writer writer) throws IOException;

  /**
   * Returns a serialized JSON string representation for the given item using
   * {@link JsonGenerator#serialize(Object)} suitable for debugging in {@link Object#toString()}.
   *
   * @param item data key/value pairs
   * @return serialized JSON string representation
   */
  public final String toString(Object item) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      JsonGenerator generator = createJsonGenerator(byteStream, JsonEncoding.UTF8);
      generator.serialize(item);
      generator.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return byteStream.toString();
  }
}
