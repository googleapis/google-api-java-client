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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.StringUtils;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;

/**
 * Tests {@link GoogleClientSecrets}.
 *
 * @author Yaniv Inbar
 */
public class GoogleClientSecretsTest extends TestCase {

  private static final String CLIENT_ID = "812741506391.apps.googleusercontent.com";

  private static final String CLIENT_SECRET = "{client_secret}";

  private final static String CLIENT_SECRETS = "{\"installed\": {\"client_id\": \"" + CLIENT_ID
      + "\",\"client_secret\": \"" + CLIENT_SECRET + "\"}}";

  public void testLoad() throws Exception {
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        new GsonFactory(), new ByteArrayInputStream(StringUtils.getBytesUtf8(CLIENT_SECRETS)));
    Details installed = clientSecrets.getInstalled();
    assertNotNull(installed);
    assertEquals(CLIENT_ID, installed.getClientId());
    assertEquals(CLIENT_SECRET, installed.getClientSecret());
  }
}
