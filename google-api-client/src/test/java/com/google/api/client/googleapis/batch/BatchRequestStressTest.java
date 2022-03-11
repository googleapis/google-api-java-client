/*
 * Copyright 2021 Google LLC
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

package com.google.api.client.googleapis.batch;

import static org.junit.Assert.assertEquals;

import com.google.api.client.http.ByteArrayContent;
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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests {@link BatchRequest}. */
public class BatchRequestStressTest {

  private static final int BATCH_SIZE = 100;
  public static BatchRequest batchRequest;
  private static AtomicInteger parseCount = new AtomicInteger(0);
  private static AtomicInteger errorCount = new AtomicInteger(0);

  @BeforeClass
  public static void setup() throws IOException {
    HttpTransport transport =
        new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
              @Override
              public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                response.setStatusCode(200);
                response.addHeader("Content-Type", "multipart/mixed; boundary=__END_OF_PART__");
                response.setContentEncoding("gzip");
                StringBuilder responseContent = new StringBuilder();
                for (int i = 0; i < BATCH_SIZE; i++) {
                  responseContent
                      .append("--" + "__END_OF_PART__" + "\n")
                      .append("Content-Type: application/http\n")
                      .append("Content-ID: response-" + i + "\n\n")
                      .append("HTTP/1.1 200 OK\n")
                      .append("Content-Type: application/json\n\n")
                      .append("{}\n\n");
                }
                responseContent.append("--" + "__END_OF_PART__--" + "\n\n");

                // gzip this content
                PipedInputStream is = new PipedInputStream();
                PipedOutputStream os = new PipedOutputStream(is);
                GZIPOutputStream gzip = new GZIPOutputStream(os);
                gzip.write(responseContent.toString().getBytes("UTF-8"));
                gzip.close();
                response.setContent(is);
                return response;
              }
            };
          }
        };

    BatchCallback<Void, Void> callback =
        new BatchCallback<Void, Void>() {
          @Override
          public void onSuccess(Void t, HttpHeaders responseHeaders) {
            parseCount.incrementAndGet();
          }

          @Override
          public void onFailure(Void e, HttpHeaders responseHeaders) {
            errorCount.incrementAndGet();
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
    for (int i = 0; i < BATCH_SIZE; i++) {
      batchRequest.queue(request1, Void.class, Void.class, callback);
    }
  }

  @Test(timeout = 4000)
  public void testResponse() throws IOException {
    batchRequest.execute();
    assertEquals(BATCH_SIZE, parseCount.get());
    assertEquals(0, errorCount.get());
  }
}
