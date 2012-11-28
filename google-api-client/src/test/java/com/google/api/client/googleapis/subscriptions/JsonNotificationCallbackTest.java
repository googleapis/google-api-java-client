/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.googleapis.subscriptions;

import com.google.api.client.googleapis.subscriptions.json.JsonNotificationCallback;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.common.base.Charsets;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

/**
 * Tests for the {@link JsonNotificationCallback} class.
 *
 * @author Matthias Linder (mlinder)
 */
public class JsonNotificationCallbackTest extends TestCase {

  @SuppressWarnings("rawtypes")
  private static class MyNotificationCallback extends JsonNotificationCallback {

    private static final long serialVersionUID = 1L;

    public TypedNotification lastNotification = null;

    public MyNotificationCallback() {
    }

    @Override
    protected void handleNotification(Subscription subscription, TypedNotification notification) {
      lastNotification = notification;
    }

    @Override
    protected JsonFactory createJsonFactory() {
      return new JacksonFactory();
    }
  }

  private byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(obj);
    return baos.toByteArray();
  }

  public void testSerialization() throws Exception {
    MyNotificationCallback handler = new MyNotificationCallback();
    serialize(handler);
  }

  @SuppressWarnings("unchecked")
  private Object parseNotification(Class<?> dataType, String dataStr) throws Exception {
    MyNotificationCallback handler = new MyNotificationCallback();
    handler.setDataType(dataType);

    String contentType = null;
    InputStream inputStream = null;

    if (dataStr != null) {
      byte[] data = Charsets.UTF_8.encode(dataStr).array();
      inputStream = new ByteArrayInputStream(data);
      contentType = "application/json; charset=utf8";
    } else {
      inputStream = new ByteArrayInputStream(new byte[0]);
    }
    UnparsedNotification notification = new UnparsedNotification(
        "id", "topic", "uri", "token", 1, "event", null, contentType, inputStream);
    Subscription subscription = new Subscription(handler, "clientToken", "id");
    handler.handleNotification(subscription, notification);
    return handler.lastNotification.getContent();
  }

  public void testParsing_void_withContent() throws Exception {
    Object result = parseNotification(Void.class, "{ \"foo\": 123 }");
    assertEquals(null, result);
  }

  public void testParsing_void_noContent() throws Exception {
    Object result = parseNotification(Void.class, null);
    assertEquals(null, result);
  }

  public static class MyClass extends GenericJson {

    public MyClass() {
    }

    @Key
    public long foo = 0;
  }

  public void testParsing_MyClass_normalContent() throws Exception {
    Object result = parseNotification(MyClass.class, "{ \"foo\": 123 }");
    assertEquals(MyClass.class, result.getClass());
    assertEquals(123, ((MyClass) result).foo);
  }

  public void testParsing_MyClass_noContent() throws Exception {
    Object result = parseNotification(MyClass.class, null);
    assertEquals(null, result);
  }
}
