// Copyright 2021 Google Inc. All Rights Reserved.

package com.google.api.client.googleapis.batch;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Test;

/**
 * Tests {@link BatchRequest}.
 *
 */
public class BatchRequestTest2 {

  @Test
  public void testResponse() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {

            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setContent(new FileInputStream("path/to/file.gz"));
            response.setContentEncoding("gzip");
            response.setStatusCode(200);
            response.addHeader("Content-Type", "multipart/mixed; boundary=" + "__END_OF_PART__");
            return response;
          }
        };
      }
    };
    BatchCallback<Void, Void> callback =
        new BatchCallback<Void, Void>() {

          @Override
          public void onSuccess(Void t, HttpHeaders responseHeaders) {
          }

          @Override
          public void onFailure(Void e, HttpHeaders responseHeaders) {
          }
        };
    BatchRequest batchRequest = new BatchRequest(transport, null);
    HttpRequest request1 =
        transport
            .createRequestFactory()
            .buildRequest(
                "GET",
                new GenericUrl("http://www.testgoogleapis.com/batch"),
                new ByteArrayContent("text/plain", "".getBytes(UTF_8)));
    HttpResponse httpResponse = request1.execute();
    batchRequest.queue(request1, Void.class, Void.class, callback);
    batchRequest.execute();
  }

}