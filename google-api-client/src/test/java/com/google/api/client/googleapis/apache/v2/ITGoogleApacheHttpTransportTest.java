/*
 * Copyright 2025 Google LLC
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

package com.google.api.client.googleapis.apache.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

public class ITGoogleApacheHttpTransportTest {

  @Test
  public void testHttpRequestFailsWhenMakingRequestToSiteWithoutDefaultJdkCerts()
      throws GeneralSecurityException, IOException {
    ApacheHttpTransport apacheHttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
    // Use a self-signed certificate site that won't be trusted by default trust store
    HttpGet httpGet = new HttpGet("https://self-signed.badssl.com/");
    Exception exception = null;
    try {
      apacheHttpTransport
          .getHttpClient()
          .execute(
              httpGet,
              new ResponseHandler<Object>() {

                @Override
                public Object handleResponse(HttpResponse httpResponse)
                    throws ClientProtocolException, IOException {
                  fail("Should not have been able to complete SSL request with untrusted cert.");
                  return null;
                }
              });
      fail("Expected SSLHandshakeException was not thrown");
    } catch (SSLHandshakeException e) {
      exception = e;
    }

    assertNotNull(exception);
    assertEquals(exception.getClass(), SSLHandshakeException.class);
  }

  @Test
  public void testHttpRequestPassesWhenMakingRequestToGoogleSite() throws Exception {
    ApacheHttpTransport apacheHttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
    HttpGet httpGet = new HttpGet("https://www.google.com/");

    apacheHttpTransport
        .getHttpClient()
        .execute(
            httpGet,
            new ResponseHandler<Object>() {
              @Override
              public Object handleResponse(HttpResponse httpResponse)
                  throws ClientProtocolException, IOException {
                assertEquals(200, httpResponse.getStatusLine().getStatusCode());
                return null;
              }
            });
  }

  @Test
  public void testHttpRequestPassesWhenMakingRequestToSiteContainedInDefaultCerts()
      throws Exception {

    ApacheHttpTransport apacheHttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
    HttpGet httpGet = new HttpGet("https://central.sonatype.com/");

    apacheHttpTransport
        .getHttpClient()
        .execute(
            httpGet,
            new ResponseHandler<Object>() {
              @Override
              public Object handleResponse(HttpResponse httpResponse)
                  throws ClientProtocolException, IOException {
                assertEquals(200, httpResponse.getStatusLine().getStatusCode());
                return null;
              }
            });
  }
}
