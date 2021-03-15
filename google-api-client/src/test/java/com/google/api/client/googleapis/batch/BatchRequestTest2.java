// Copyright 2021 Google Inc. All Rights Reserved.

package com.google.api.client.googleapis.batch;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GZipEncoding;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Charsets;
import java.io.IOException;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests {@link BatchRequest}.
 */
public class BatchRequestTest2 {

  public static BatchRequest batchRequest;

  @BeforeClass
  public static void setup() throws IOException {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {

            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setStatusCode(200);
            response.addHeader(
                "Content-Type", "multipart/mixed; boundary=__END_OF_PART__");
            StringBuilder responseContent = new StringBuilder();
            responseContent
                .append("--" + "__END_OF_PART__" + "\n")
                .append("Content-Length: 415\n")
                .append("Content-Type: application/http\n")
                .append("Content-ID: 1\n")
                .append("Content-Transfer-Encoding: binary\n\n")
                .append("Content-Length: 300" + "\n")
                .append("Content-Type: text/plain; charset=UTF-8\n\n")
                .append("--" + "__END_OF_PART__--" + "\n");

            response.setContent(responseContent.toString());
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
    batchRequest = new BatchRequest(transport, null);
    byte[] content = new byte[300];
    Arrays.fill(content, (byte) ' ');
    HttpRequest request1 =
        transport
            .createRequestFactory()
            .buildRequest(
                "POST",
                new GenericUrl("http://www.google.com/"),
                new ByteArrayContent(
                    new HttpMediaType("text/plain").setCharsetParameter(Charsets.UTF_8).build(),
                    content));
    request1.setEncoding(new GZipEncoding());
    request1.execute();
    for (int i = 0; i < 3; i++) {
      batchRequest.queue(request1, Void.class, Void.class, callback);
    }
  }

  @Test(timeout = 4000)
  public void testResponse() throws IOException {
    batchRequest.execute();
  }
}
