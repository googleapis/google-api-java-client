package com.google.api.client.googleapis.testing.auth.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Mock for {@link GoogleCredential}.
 *
 * @since 1.20
 */
@Beta
public class MockGoogleCredential extends GoogleCredential {
  public static final String ACCESS_TOKEN = "access_xyz";
  public static final String REFRESH_TOKEN = "refresh123";
  private static final String EXPIRES_IN_SECONDS = "3600";
  private static final String TOKEN_TYPE = "Bearer";
  private static final String TOKEN_RESPONSE = "{"
      +   "\"access_token\": \"%s\", "
      +   "\"expires_in\":  %s, "
      +   "\"refresh_token\": \"%s\", "
      +   "\"token_type\": \"%s\""
      + "}";

  private static final String DEFAULT_TOKEN_RESPONSE_JSON = String.format(TOKEN_RESPONSE,
      ACCESS_TOKEN, EXPIRES_IN_SECONDS, REFRESH_TOKEN, TOKEN_TYPE);

  public MockGoogleCredential(Builder builder) {
    super(builder);
  }

  /**
   * Mock for GoogleCredential.Builder.
   *
   * <p>Setters that are necessary for simple {@link GoogleCredential} creation are overridden in
   * order to change the return type. A concrete {@link JsonFactory} is set by default, since JSON
   * parsing is relied upon often in GoogleCredential which makes mocking parse calls problematic.
   * </p>
   *
   * <p>By default, a standard {@link MockHttpTransport} is supplied. For simple tests in which
   * 'refresh' methods are called but the request/response isn't used
   * {@link #newMockHttpTransportWithSampleTokenResponse()} provides a minimal implementation. For
   * more complex tests which check request/response behavior prefer MockTokenServerTransport.</p>
   */
  @Beta
  public static class Builder extends GoogleCredential.Builder {
    @Override
    public Builder setTransport(HttpTransport transport) {
      return (MockGoogleCredential.Builder) super.setTransport(transport);
    }

    @Override
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      return (MockGoogleCredential.Builder) super.setClientAuthentication(clientAuthentication);
    }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (MockGoogleCredential.Builder) super.setJsonFactory(jsonFactory);
    }

    @Override
    public Builder setClock(Clock clock) {
      return (MockGoogleCredential.Builder) super.setClock(clock);
    }

    @Override
    public MockGoogleCredential build() {
      if (getTransport() == null) {
        setTransport(new MockHttpTransport.Builder().build());
      }
      if (getClientAuthentication() == null) {
        setClientAuthentication(new MockClientAuthentication());
      }
      if (getJsonFactory() == null) {
        setJsonFactory(new JacksonFactory());
      }
      return new MockGoogleCredential(this);
    }
  }

 /**
  * Returns a new {@link MockHttpTransport} with a sample {@link MockLowLevelHttpResponse}. The
  * response includes sample TokenResponse content as specified in DEFAULT_TOKEN_RESPONSE_JSON. This
  * is meant to produce a minimal implementation that allows methods such as
  * {@link GoogleCredential#executeRefreshToken()} to be called without failing abruptly. This
  * content is static. If you are making assertions based on the content of the request, then
  * MockTokenServerTransport should be used instead.
  *
  * @return mockHttpTransport
  */
  public static MockHttpTransport newMockHttpTransportWithSampleTokenResponse() {
    MockLowLevelHttpResponse mockLowLevelHttpResponse = new MockLowLevelHttpResponse()
       .setContentType(Json.MEDIA_TYPE)
       .setContent(DEFAULT_TOKEN_RESPONSE_JSON);
    MockLowLevelHttpRequest request = new MockLowLevelHttpRequest()
        .setResponse(mockLowLevelHttpResponse);
    return new MockHttpTransport.Builder()
        .setLowLevelHttpRequest(request)
        .build();
  }

  /**
   * Mock for ClientAuthentication.
   */
  @Beta
  private static class MockClientAuthentication implements HttpExecuteInterceptor {
    @Override
    public void intercept(HttpRequest request) throws IOException {
      // pass
    }
  }
}
