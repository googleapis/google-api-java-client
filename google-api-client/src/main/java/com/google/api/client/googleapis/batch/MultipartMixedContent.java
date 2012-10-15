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

package com.google.api.client.googleapis.batch;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

/**
 * Serializes MIME Multipart/Mixed content as specified by <a
 * href="http://tools.ietf.org/html/rfc2046">RFC 2046: Multipurpose Internet Mail Extensions</a>.
 *
 * <p>
 * Takes in a list of {@link HttpRequest} and serializes their headers and content separating each
 * request with a boundary. The "Content-ID" header of each request is incremented in order.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 * @author rmistry@google.com (Ravi Mistry)
 */
class MultipartMixedContent extends AbstractHttpContent {

  private static final String CR_LF = "\r\n";
  private static final String TWO_DASHES = "--";

  /** List of request infos. */
  private List<BatchRequest.RequestInfo<?, ?>> requestInfos;

  /**
   * Construct an instance of {@link MultipartMixedContent}.
   *
   * @param requestInfos List of request infos
   * @param boundary Boundary string to use for separating each HTTP request
   */
  MultipartMixedContent(List<BatchRequest.RequestInfo<?, ?>> requestInfos, String boundary) {
    super(new HttpMediaType("multipart/mixed").setParameter(
        "boundary", Preconditions.checkNotNull(boundary)));
    Preconditions.checkNotNull(requestInfos);
    Preconditions.checkArgument(!requestInfos.isEmpty());
    this.requestInfos = Collections.unmodifiableList(requestInfos);
  }

  private String getBoundary() {
    return getMediaType().getParameter("boundary");
  }

  public void writeTo(OutputStream out) throws IOException {
    int contentId = 1;
    Writer writer = new OutputStreamWriter(out);
    String boundary = getBoundary();

    for (BatchRequest.RequestInfo<?, ?> requestInfo : requestInfos) {
      HttpRequest request = requestInfo.request;

      // Write batch separator.
      writer.write(TWO_DASHES);
      writer.write(boundary);
      writer.write(CR_LF);

      // Write multipart headers.
      writer.write("Content-Type: application/http");
      writer.write(CR_LF);
      writer.write("Content-Transfer-Encoding: binary");
      writer.write(CR_LF);
      writer.write("Content-ID: ");
      writer.write(String.valueOf(contentId++));
      writer.write(CR_LF);
      writer.write(CR_LF);

      // Write the batch method and path.
      writer.write(request.getRequestMethod());
      writer.write(" ");
      writer.write(request.getUrl().build());
      writer.write(CR_LF);

      // Write the batch headers.
      HttpHeaders.serializeHeadersForMultipartRequests(request.getHeaders(), null, null, writer);

      // Write the data to the body.
      HttpContent data = request.getContent();
      if (data != null) {
        String type = data.getType();
        if (type != null) {
          writeHeader(writer, "Content-Type", type);
        }
        long length = data.getLength();
        if (length != -1) {
          writeHeader(writer, "Content-Length", length);
        }
        writer.write(CR_LF);
        writer.flush();
        data.writeTo(out);
      }
      writer.write(CR_LF);
    }

    // Write the end of the batch separator.
    writer.write(TWO_DASHES);
    writer.write(boundary);
    writer.write(TWO_DASHES);
    writer.write(CR_LF);

    writer.flush();
  }

  /** Writes a header to the Writer. */
  private void writeHeader(Writer writer, String name, Object value) throws IOException {
    writer.write(name);
    writer.write(": ");
    writer.write(value.toString());
    writer.write(CR_LF);
  }

  @Override
  public MultipartMixedContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }
}
