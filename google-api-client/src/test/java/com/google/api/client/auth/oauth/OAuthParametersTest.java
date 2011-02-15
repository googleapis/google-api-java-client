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

package com.google.api.client.auth.oauth;

import junit.framework.TestCase;

/**
 * Tests {@link OAuthParameters}.
 *
 * @author Yaniv Inbar
 */
public class OAuthParametersTest extends TestCase {

  public OAuthParametersTest() {
  }

  public OAuthParametersTest(String name) {
    super(name);
  }

  public void testEscape() {
    assertEquals(
        "abcdefghijklmnopqrstuvwxyz", OAuthParameters.escape("abcdefghijklmnopqrstuvwxyz"));
    assertEquals(
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ", OAuthParameters.escape("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
    assertEquals("01234567890", OAuthParameters.escape("01234567890"));
    assertEquals("-_.~", OAuthParameters.escape("-_.~"));
    assertEquals("%20%2B%25%3A%2F", OAuthParameters.escape(" +%:/"));
  }

  public void testGetAuthorizationHeader() {
    OAuthParameters parameters = new OAuthParameters();
    parameters.verifier = "gZ1BFee1qSijpqbxfnX+o8rQ";
    parameters.consumerKey = "anonymous";
    parameters.nonce = "b51df3249df9dfd";
    parameters.signatureMethod = "HMAC-SHA1";
    parameters.timestamp = "1274732403";
    parameters.token = "4/1mZ3ZPynTry3szE49h3XyXk24p_I";
    parameters.signature = "OTfTeiNjKsNeqBtYhUPIiJO9pC4=";
    assertEquals("OAuth oauth_consumer_key=\"anonymous\", oauth_nonce=\"b51df3249df9dfd\", "
        + "oauth_signature=\"OTfTeiNjKsNeqBtYhUPIiJO9pC4%3D\", "
        + "oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1274732403\", "
        + "oauth_token=\"4%2F1mZ3ZPynTry3szE49h3XyXk24p_I\", "
        + "oauth_verifier=\"gZ1BFee1qSijpqbxfnX%2Bo8rQ\"", parameters.getAuthorizationHeader());
  }
}
