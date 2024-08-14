package com.google.api.client.googleapis.apache.v5;

import com.google.api.client.http.apache.v5.Apache5HttpTransport;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.junit.Test;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class ITGoogleApache5HttpTransportTest {

  @Test
  public void testHttpRequestFailsWhenMakingRequestToNonGoogleSite()
      throws GeneralSecurityException, IOException {
    Apache5HttpTransport apache5HttpTransport = GoogleApache5HttpTransport.newTrustedTransport();
    HttpGet httpGet = new HttpGet("https://www.bing.com/");

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
