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

import com.google.api.client.googleapis.testing.subscriptions.MockNotificationCallback;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Tests for the {@link SubscriptionManager} class.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public class SubscriptionManagerTest extends TestCase {

  /** Test for .deliverPushNotification() using the normal flow. */
  public void testDeliverPushNotification_simple() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    MockNotificationCallback handler = new MockNotificationCallback();
    SubscriptionManager pm = new SubscriptionManager(store);

    store.storeSubscription(new Subscription("id", handler, "clientToken"));

    // Send a notification
    InputStream contentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    UnparsedNotification notification = new UnparsedNotification("id",
        "topicID",
        "topicURI",
        "clientToken",
        "eventType",
        "foo/bar",
        contentStream);
    assertTrue(pm.deliverNotification(notification));
    assertEquals(true, handler.wasCalled());
  }

  /** Test for .deliverPushNotification() with a non-existing subscription. */
  public void testDeliverPushNotification_nonExistent() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    MockNotificationCallback handler = new MockNotificationCallback();
    SubscriptionManager pm = new SubscriptionManager(store);

    store.storeSubscription(new Subscription("differentId", handler, "clientToken"));

    // Send a notification
    InputStream contentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    UnparsedNotification notification = new UnparsedNotification("id",
        "topicID",
        "topicURI",
        "clientToken",
        "eventType",
        "foo/bar",
        contentStream);

    assertFalse(pm.deliverNotification(notification));
    assertEquals(false, handler.wasCalled());
  }

  /** Test for .deliverPushNotification() with an invalid client token. */
  public void testDeliverPushNotification_invalidToken()
      throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    MockNotificationCallback handler = new MockNotificationCallback();
    SubscriptionManager pm = new SubscriptionManager(store);

    Subscription s = new Subscription("id", handler, "randomToken");
    store.storeSubscription(s);

    // Send a notification
    InputStream contentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    UnparsedNotification notification = new UnparsedNotification("id",
        "topicID",
        "topicURI",
        "differentClientToken",
        "eventType",
        "foo/bar",
        contentStream);

    try {
      pm.deliverNotification(notification);
      fail("Expected IllegalArgumentException but code passed");
    } catch (IllegalArgumentException expected) {}

    assertEquals(false, handler.wasCalled());
  }

  /** Interface used for creating mock responses to http requests. */
  private interface FakeHttpRequestExecutor {
    public void execute(MockLowLevelHttpRequest request, MockLowLevelHttpResponse response);
  }

  /** Fake LowLevelHttpRequest. */
  private static class FakeLowLevelHttpRequest extends MockLowLevelHttpRequest {
    private FakeHttpRequestExecutor executor;
    private MockLowLevelHttpResponse response;

    public FakeLowLevelHttpRequest(FakeHttpRequestExecutor executor) {
      this.executor = executor;
    }

    @Override
    public LowLevelHttpResponse execute() {
      response = new MockLowLevelHttpResponse();
      executor.execute(this, response);
      return response;
    }

    @Override
    public MockLowLevelHttpResponse getResponse() {
      return response;
    }

    /** Fake HttpTransport class using the FakeHttpRequestExecutor. */
    private static class FakeHttpTransport extends MockHttpTransport {
      private FakeHttpRequestExecutor executor;

      public FakeHttpTransport(FakeHttpRequestExecutor executor) {
        this.executor = executor;
      }

      @Override
      public LowLevelHttpRequest buildGetRequest(String url) {
        return new FakeLowLevelHttpRequest(executor);
      }
    }

    /** Returns a HttpRequest using the specified FakeHttpRequestExecutor. */
    public static HttpRequest buildHttpRequest(FakeHttpRequestExecutor executor)
        throws Exception {
      GenericUrl url = new GenericUrl("http://example.com");
      return new FakeHttpTransport(executor).createRequestFactory().buildGetRequest(url);
    }
  }

  /** Tests the normal flow execution. */
  @SuppressWarnings("unchecked")
  public void testSubscribeFlow() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    SubscriptionManager pm = new SubscriptionManager(store, "something");

    // Create a fake response
    HttpRequest request = FakeLowLevelHttpRequest.buildHttpRequest(new FakeHttpRequestExecutor() {
      public void execute(MockLowLevelHttpRequest request, MockLowLevelHttpResponse response) {
        assertEquals("something", request.getHeaders().get("x-subscribe").get(0));
        String clientToken = request.getHeaders().get("x-client-token").get(0);

        response.addHeader(SubscriptionHeaders.SUBSCRIPTION_ID, "12345");
        response.addHeader(SubscriptionHeaders.CLIENT_TOKEN, clientToken);
        response.addHeader(SubscriptionHeaders.TOPIC_ID, "topicID");
        response.addHeader(SubscriptionHeaders.TOPIC_URI, "http://topic.uri/");
      }
    });
    pm.addSubscriptionRequestHeaders(request);
    HttpResponse response = request.execute();
    Subscription subscription =
        pm.processSubscribeResponse(response.getHeaders(), new MockNotificationCallback());

    assertEquals(1, store.listSubscriptions().size());
    assertEquals("12345", subscription.getSubscriptionID());
  }

  /** Tests the flow where no subscription id is sent back. */
  @SuppressWarnings("unchecked")
  public void testSubscribeFlow_nofeedback() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    SubscriptionManager pm = new SubscriptionManager(store, "something");

    // Create a fake response
    HttpRequest request = FakeLowLevelHttpRequest.buildHttpRequest(new FakeHttpRequestExecutor() {
      public void execute(MockLowLevelHttpRequest request, MockLowLevelHttpResponse response) {
        assertEquals("something", request.getHeaders().get("x-subscribe").get(0));
        assertEquals("foobar", request.getHeaders().get("x-client-token").get(0));
      }
    });

    try {
      pm.addSubscriptionRequestHeaders(request, "foobar");
      HttpResponse response = request.execute();
      pm.processSubscribeResponse(response.getHeaders(), new MockNotificationCallback());
      fail("Did not receive IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
    assertEquals(0, store.listSubscriptions().size());
  }
}
