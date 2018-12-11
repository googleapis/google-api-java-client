/*
 * Copyright 2012 Google Inc.
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

import com.google.api.client.googleapis.batch.BatchRequest.RequestInfo;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * The unparsed batch response.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
final class BatchUnparsedResponse {

  /** The boundary used in the batch response to separate individual responses. */
  private final String boundary;

  /** List of request infos. */
  private final List<RequestInfo<?, ?>> requestInfos;

  /** Input stream that contains the batch response. */
  private final InputStream inputStream;

  /** Determines whether there are any responses to be parsed. */
  boolean hasNext = true;

  /** List of unsuccessful HTTP requests that can be retried. */
  List<RequestInfo<?, ?>> unsuccessfulRequestInfos = new ArrayList<RequestInfo<?, ?>>();

  /** The content Id the response is currently at. */
  private int contentId = 0;

  /** Whether unsuccessful HTTP requests can be retried. */
  private final boolean retryAllowed;

  /**
   * Construct the {@link BatchUnparsedResponse}.
   *
   * @param inputStream Input stream that contains the batch response
   * @param boundary The boundary of the batch response
   * @param requestInfos List of request infos
   * @param retryAllowed Whether unsuccessful HTTP requests can be retried
   */
  BatchUnparsedResponse(InputStream inputStream, String boundary,
      List<RequestInfo<?, ?>> requestInfos, boolean retryAllowed)
      throws IOException {
    this.boundary = boundary;
    this.requestInfos = requestInfos;
    this.retryAllowed = retryAllowed;
    this.inputStream = inputStream;
    // First line in the stream will be the boundary.
    checkForFinalBoundary(readLine());
  }

  /**
   * Parses the next response in the queue if a data class and a {@link BatchCallback} is specified.
   *
   * <p>
   * This method closes the input stream if there are no more individual responses left.
   * </p>
   */
  void parseNextResponse() throws IOException {
    contentId++;

    // Extract the outer headers.
    String line;
    while ((line = readLine()) != null && !line.equals("")) {
      // Do nothing.
    }

    // Extract the status code.
    String statusLine = readLine();
    String[] statusParts = statusLine.split(" ");
    int statusCode = Integer.parseInt(statusParts[1]);

    // Extract and store the inner headers.
    // TODO(rmistry): Handle inner headers that span multiple lines. More details here:
    // http://tools.ietf.org/html/rfc2616#section-2.2
    List<String> headerNames = new ArrayList<String>();
    List<String> headerValues = new ArrayList<String>();
    long contentLength = -1L;
    while ((line = readLine()) != null && !line.equals("")) {
      String[] headerParts = line.split(": ", 2);
      String headerName = headerParts[0];
      String headerValue = headerParts[1];
      headerNames.add(headerName);
      headerValues.add(headerValue);
      if ("Content-Length".equalsIgnoreCase(headerName.trim())) {
          contentLength = Long.parseLong(headerValue);
      }
    }

    InputStream body;
    if (contentLength == -1) {
      // This isn't very efficient, but most respectable servers should set the Content-Length
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      while ((line = readRawLine()) != null && !line.startsWith(boundary)) {
        // Convert characters back to bytes:
        buffer.write(line.getBytes("ISO-8859-1"));
      }

      // Remove CRLF that separates body from boundary token
      body = trimCrlf(buffer.toByteArray());

      // Remove CRLF from the boundary token (to match readLine)
      line = trimCrlf(line);
    } else {
      body = new FilterInputStream(ByteStreams.limit(inputStream, contentLength)) {
        @Override
        public void close() {
          // Don't allow the parser to close the underlying stream
        }
      };
    }

    HttpResponse response =
        getFakeResponse(statusCode, body, headerNames, headerValues);

    parseAndCallback(requestInfos.get(contentId - 1), statusCode, response);

    // Consume any bytes that were not consumed by the parser
    while (body.skip(contentLength) > 0 || body.read() != -1) {
    }

    if (contentLength != -1) {
      line = readLine();
    } else {
      // The line was already read
    }
    // Consume any blank lines that follow the response (not included in Content-Length)
    while ((line != null) && (line.length() == 0)) {
      line = readLine();
    }

    checkForFinalBoundary(line);
  }

  /**
   * Parse an object into a new instance of the data class using
   * {@link HttpResponse#parseAs(java.lang.reflect.Type)}.
   */
  private <T, E> void parseAndCallback(
      RequestInfo<T, E> requestInfo, int statusCode, HttpResponse response)
      throws IOException {
    BatchCallback<T, E> callback = requestInfo.callback;

    HttpHeaders responseHeaders = response.getHeaders();
    HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler =
        requestInfo.request.getUnsuccessfulResponseHandler();

    if (HttpStatusCodes.isSuccess(statusCode)) {
      if (callback == null) {
        // No point in parsing if there is no callback.
        return;
      }
      T parsed = getParsedDataClass(requestInfo.dataClass, response, requestInfo);
      callback.onSuccess(parsed, responseHeaders);
    } else {
      HttpContent content = requestInfo.request.getContent();
      boolean retrySupported = retryAllowed && (content == null || content.retrySupported());
      boolean errorHandled = false;
      boolean redirectRequest = false;
      if (unsuccessfulResponseHandler != null) {
        errorHandled = unsuccessfulResponseHandler.handleResponse(
            requestInfo.request, response, retrySupported);
      }
      if (!errorHandled) {
        if (requestInfo.request.handleRedirect(response.getStatusCode(), response.getHeaders())) {
          redirectRequest = true;
        }
      }
      if (retrySupported && (errorHandled || redirectRequest)) {
        unsuccessfulRequestInfos.add(requestInfo);
      } else {
        if (callback == null) {
          // No point in parsing if there is no callback.
          return;
        }
        E parsed = getParsedDataClass(requestInfo.errorClass, response, requestInfo);
        callback.onFailure(parsed, responseHeaders);
      }
    }
  }

  private <A, T, E> A getParsedDataClass(
      Class<A> dataClass, HttpResponse response, RequestInfo<T, E> requestInfo) throws IOException {
    // TODO(yanivi): Remove the HttpResponse reference and directly parse the InputStream
    if (dataClass == Void.class) {
      return null;
    }
    return requestInfo.request.getParser().parseAndClose(
        response.getContent(), response.getContentCharset(), dataClass);
  }

  /** Create a fake HTTP response object populated with the partContent and the statusCode. */
  private HttpResponse getFakeResponse(final int statusCode, final InputStream partContent,
      List<String> headerNames, List<String> headerValues)
      throws IOException {
    HttpRequest request = new FakeResponseHttpTransport(
        statusCode, partContent, headerNames, headerValues).createRequestFactory()
        .buildPostRequest(new GenericUrl("http://google.com/"), null);
    request.setLoggingEnabled(false);
    request.setThrowExceptionOnExecuteError(false);
    return request.execute();
  }

  /**
   * Reads an HTTP response line (ISO-8859-1 encoding).
   *
   * @return The line that was read, including CRLF.
   */
  private String readRawLine() throws IOException {
    int b = inputStream.read();
    if (b == -1) {
      return null;
    } else {
      StringBuilder buffer = new StringBuilder();
      for (; b != -1; b = inputStream.read()) {
        buffer.append((char) b);
        if (b == '\n') {
          break;
        }
      }
      return buffer.toString();
    }
  }

  /**
   * Reads an HTTP response line (ISO-8859-1 encoding)
   * <p>
   * This method is similar to {@link java.io.BufferedReader#readLine()}, but handles newlines in a
   * way that is consistent with the HTTP RFC 2616.
   *
   * @return The line that was read, excluding CRLF.
   */
  private String readLine() throws IOException {
    return trimCrlf(readRawLine());
  }

  private static String trimCrlf(String input) {
    if (input.endsWith("\r\n")) {
      return input.substring(0, input.length() - 2);
    } else if (input.endsWith("\n")) {
      return input.substring(0, input.length() - 1);
    } else {
      return input;
    }
  }

  private static InputStream trimCrlf(byte[] bytes) {
    int length = bytes.length;
    if (length > 0 && bytes[length - 1] == '\n') {
      length--;
    }
    if (length > 0 && bytes[length - 1] == '\r') {
      length--;
    }
    return new ByteArrayInputStream(bytes, 0, length);
  }

  /**
   * If the boundary line consists of the boundary and "--" then there are no more individual
   * responses left to be parsed and the input stream is closed.
   */
  private void checkForFinalBoundary(String boundaryLine) throws IOException {
    if (boundaryLine.equals(boundary + "--")) {
      hasNext = false;
      inputStream.close();
    }
  }

  private static class FakeResponseHttpTransport extends HttpTransport {

    private int statusCode;
    private InputStream partContent;
    private List<String> headerNames;
    private List<String> headerValues;

    FakeResponseHttpTransport(int statusCode, InputStream partContent, List<String> headerNames,
        List<String> headerValues) {
      super();
      this.statusCode = statusCode;
      this.partContent = partContent;
      this.headerNames = headerNames;
      this.headerValues = headerValues;
    }

    @Override
    protected LowLevelHttpRequest buildRequest(String method, String url) {
      return new FakeLowLevelHttpRequest(partContent, statusCode, headerNames, headerValues);
    }
  }

  private static class FakeLowLevelHttpRequest extends LowLevelHttpRequest {

    private InputStream partContent;
    private int statusCode;
    private List<String> headerNames;
    private List<String> headerValues;

    FakeLowLevelHttpRequest(InputStream partContent, int statusCode, List<String> headerNames,
        List<String> headerValues) {
      this.partContent = partContent;
      this.statusCode = statusCode;
      this.headerNames = headerNames;
      this.headerValues = headerValues;
    }

    @Override
    public void addHeader(String name, String value) {
    }

    @Override
    public LowLevelHttpResponse execute() {
      FakeLowLevelHttpResponse response = new FakeLowLevelHttpResponse(
          partContent, statusCode, headerNames, headerValues);
      return response;
    }
  }

  private static class FakeLowLevelHttpResponse extends LowLevelHttpResponse {

    private InputStream partContent;
    private int statusCode;
    private List<String> headerNames = new ArrayList<String>();
    private List<String> headerValues = new ArrayList<String>();

    FakeLowLevelHttpResponse(InputStream partContent, int statusCode, List<String> headerNames,
        List<String> headerValues) {
      this.partContent = partContent;
      this.statusCode = statusCode;
      this.headerNames = headerNames;
      this.headerValues = headerValues;
    }

    @Override
    public InputStream getContent() {
      return partContent;
    }

    @Override
    public int getStatusCode() {
      return statusCode;
    }

    @Override
    public String getContentEncoding() {
      return null;
    }

    @Override
    public long getContentLength() {
      return 0;
    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public String getStatusLine() {
      return null;
    }

    @Override
    public String getReasonPhrase() {
      return null;
    }

    @Override
    public int getHeaderCount() {
      return headerNames.size();
    }

    @Override
    public String getHeaderName(int index) {
      return headerNames.get(index);
    }

    @Override
    public String getHeaderValue(int index) {
      return headerValues.get(index);
    }
  }
}
