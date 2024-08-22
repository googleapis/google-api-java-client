/*
 * Copyright 2024 Google LLC
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

package com.google.api.client.googleapis.apache.v5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.api.client.http.apache.v5.Apache5HttpTransport;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

public class ITGoogleApache5HttpTransportTest {

  @Test
  public void testHttpRequestFailsWhenMakingRequestToSiteWithoutGoogleCerts()
      throws GeneralSecurityException, IOException {
    int port = PortFactory.findFreePort();
    // MockServer handles all SSL traffic transparently by auto-generating an appropriate SSL
    // certificate using their own cert
    // https://github.com/mock-server/mockserver/blob/master/mockserver-core/src/main/resources/org/mockserver/socket/CertificateAuthorityCertificate.pem
    ClientAndServer mockServer = ClientAndServer.startClientAndServer(port);
    Apache5HttpTransport apache5HttpTransport = GoogleApache5HttpTransport.newTrustedTransport();
    HttpGet httpGet = new HttpGet("https://localhost:" + port + "/");
    Exception exception = null;
    try {
      apache5HttpTransport
          .getHttpClient()
          .execute(
              httpGet,
              new HttpClientResponseHandler<Void>() {
                @Override
                public Void handleResponse(ClassicHttpResponse response) {
                  fail(
                      "Should not have been able to complete SSL request on non google site."
                  );
                  return null;
                }
              });
      fail("Expected SSLHandshakeException was not thrown");
    } catch (SSLHandshakeException e) {
      exception = e;
    }

    assertNotNull(exception);
    assertEquals(exception.getClass(), SSLHandshakeException.class);

    mockServer.stop();
  }

  @Test
  public void testHttpRequestPassesWhenMakingRequestToGoogleSite() throws Exception {
    Apache5HttpTransport apache5HttpTransport = GoogleApache5HttpTransport.newTrustedTransport();
    HttpGet httpGet = new HttpGet("https://www.google.com/");

    apache5HttpTransport
        .getHttpClient()
        .execute(
            httpGet,
            new HttpClientResponseHandler<Void>() {
              @Override
              public Void handleResponse(ClassicHttpResponse response) {
                assertEquals(200, response.getCode());
                return null;
              }
            });
  }
}
