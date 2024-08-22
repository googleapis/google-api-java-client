package com.google.api.client.googleapis.apache.v5;

import com.google.api.client.http.apache.v5.Apache5HttpTransport;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.junit.Test;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

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

    assertThrows(
        SSLHandshakeException.class,
        () ->
            apache5HttpTransport
                .getHttpClient()
                .execute(
                    httpGet,
                    response -> {
                      fail(
                          "Should not have been able to complete SSL request to site without google certificates.");
                      return null;
                    }));

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
            response -> {
              assertEquals(200, response.getCode());
              return null;
            });
  }
}
