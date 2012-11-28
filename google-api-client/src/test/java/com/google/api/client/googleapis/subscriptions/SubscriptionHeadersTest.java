/*
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

import com.google.api.client.http.HttpHeaders;

import junit.framework.TestCase;

/**
 * Tests {@link SubscriptionHeaders}.
 *
 * @author Yaniv Inbar
 */
public class SubscriptionHeadersTest extends TestCase {

  private static final String SUBSCRIBE_VALUE = "web_hook?url=https://example.com/foo/bar";
  private static final String SUBSCRIPTION_ID_VALUE = "someSubscriptionID";
  private static final String TOPIC_ID_VALUE = "someTopicID";
  private static final String TOPIC_URI_VALUE = "http://some.topic.uri/";
  private static final String CLIENT_TOKEN_VALUE = "someClienToken";
  private static final String SUBSCRIPTION_EXPIRES_VALUE = "Fri, 07 Sep 2012 18:52:00 GMT";

  public void testGetters() {
    HttpHeaders headers = new HttpHeaders();
    headers.put(SubscriptionHeaders.SUBSCRIBE, SUBSCRIBE_VALUE);
    headers.put(SubscriptionHeaders.SUBSCRIPTION_ID, SUBSCRIPTION_ID_VALUE);
    headers.put(SubscriptionHeaders.TOPIC_ID, TOPIC_ID_VALUE);
    headers.put(SubscriptionHeaders.TOPIC_URI, TOPIC_URI_VALUE);
    headers.put(SubscriptionHeaders.CLIENT_TOKEN, CLIENT_TOKEN_VALUE);
    headers.put(SubscriptionHeaders.SUBSCRIPTION_EXPIRES, SUBSCRIPTION_EXPIRES_VALUE);
    assertEquals(SUBSCRIBE_VALUE, SubscriptionHeaders.getSubscribe(headers));
    assertEquals(SUBSCRIPTION_ID_VALUE, SubscriptionHeaders.getSubscriptionId(headers));
    assertEquals(TOPIC_ID_VALUE, SubscriptionHeaders.getTopicId(headers));
    assertEquals(TOPIC_URI_VALUE, SubscriptionHeaders.getTopicUri(headers));
    assertEquals(CLIENT_TOKEN_VALUE, SubscriptionHeaders.getClientToken(headers));
    assertEquals(SUBSCRIPTION_EXPIRES_VALUE, SubscriptionHeaders.getSubscriptionExpires(headers));
  }

  public void testSetters() {
    HttpHeaders headers = new HttpHeaders();
    SubscriptionHeaders.setSubscribe(headers, SUBSCRIBE_VALUE);
    SubscriptionHeaders.setSubscriptionId(headers, SUBSCRIPTION_ID_VALUE);
    SubscriptionHeaders.setTopicId(headers, TOPIC_ID_VALUE);
    SubscriptionHeaders.setTopicUri(headers, TOPIC_URI_VALUE);
    SubscriptionHeaders.setClientToken(headers, CLIENT_TOKEN_VALUE);
    SubscriptionHeaders.setSubscriptionExpires(headers, SUBSCRIPTION_EXPIRES_VALUE);
    assertEquals(SUBSCRIBE_VALUE, headers.get(SubscriptionHeaders.SUBSCRIBE));
    assertEquals(SUBSCRIPTION_ID_VALUE, headers.get(SubscriptionHeaders.SUBSCRIPTION_ID));
    assertEquals(TOPIC_ID_VALUE, headers.get(SubscriptionHeaders.TOPIC_ID));
    assertEquals(TOPIC_URI_VALUE, headers.get(SubscriptionHeaders.TOPIC_URI));
    assertEquals(CLIENT_TOKEN_VALUE, headers.get(SubscriptionHeaders.CLIENT_TOKEN));
    assertEquals(SUBSCRIPTION_EXPIRES_VALUE, headers.get(SubscriptionHeaders.SUBSCRIPTION_EXPIRES));
  }
}
