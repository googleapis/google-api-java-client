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

import com.google.api.client.googleapis.batch.BatchRequest.RequestInfo;
import com.google.api.client.http.BackOffPolicy;
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
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Preconditions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

  /** Buffers characters from the input stream. */
  private final BufferedReader bufferedReader;

  /** Determines whether there are any responses to be parsed. */
  boolean hasNext = true;

  /** List of unsuccessful HTTP requests that can be retried. */
  List<RequestInfo<?, ?>> unsuccessfulRequestInfos = new ArrayList<RequestInfo<?, ?>>();

  /** Indicates if back off is required before the next retry. */
  boolean backOffRequired;

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
    this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    // First line in the stream will be the boundary.
    checkForFinalBoundary(bufferedReader.readLine());
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
    while ((line = bufferedReader.readLine()) != null && !line.equals("")) {
      // Do nothing.
    }

    // Extract the status code.
    String statusLine = bufferedReader.readLine();
    Preconditions.checkState(statusLine != null);
    String[] statusParts = statusLine.split(" ");
    int statusCode = Integer.parseInt(statusParts[1]);

    // Extract and store the inner headers.
    // TODO(rmistry): Handle inner headers that span multiple lines. More details here:
    // http://tools.ietf.org/html/rfc2616#section-2.2
    List<String> headerNames = new ArrayList<String>();
    List<String> headerValues = new ArrayList<String>();
    while ((line = bufferedReader.readLine()) != null && !line.equals("")) {
      String[] headerParts = line.split(": ", 2);
      headerNames.add(headerParts[0]);
      headerValues.add(headerParts[1]);
    }

    // Extract the response part content.
    // TODO(rmistry): Investigate a way to use the stream directly. This is to reduce the chance of
    // an OutOfMemoryError and will make parsing more efficient.
    StringBuilder partContent = new StringBuilder();
    while ((line = bufferedReader.readLine()) != null && !line.startsWith(boundary)) {
      partContent.append(line);
    }

    HttpResponse response =
        getFakeResponse(statusCode, partContent.toString(), headerNames, headerValues);

    parseAndCallback(requestInfos.get(contentId - 1), statusCode, contentId, response);

    checkForFinalBoundary(line);
  }

  /**
   * Parse an object into a new instance of the data class using
   * {@link HttpResponse#parseAs(java.lang.reflect.Type)}.
   */
  private <T, E> void parseAndCallback(
      RequestInfo<T, E> requestInfo, int statusCode, int contentID, HttpResponse response)
      throws IOException {
    BatchCallback<T, E> callback = requestInfo.callback;

    HttpHeaders responseHeaders = response.getHeaders();
    HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler =
        requestInfo.request.getUnsuccessfulResponseHandler();
    BackOffPolicy backOffPolicy = requestInfo.request.getBackOffPolicy();

    // Reset backOff flag.
    backOffRequired = false;

    if (HttpStatusCodes.isSuccess(statusCode)) {
      if (callback == null) {
        // No point in parsing if there is no callback.
        return;
      }
      T parsed = getParsedDataClass(
          requestInfo.dataClass, response, requestInfo, responseHeaders.getContentType());
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
        } else if (retrySupported && backOffPolicy != null
            && backOffPolicy.isBackOffRequired(response.getStatusCode())) {
          backOffRequired = true;
        }
      }
      if (retrySupported && (errorHandled || backOffRequired || redirectRequest)) {
        unsuccessfulRequestInfos.add(requestInfo);
      } else {
        if (callback == null) {
          // No point in parsing if there is no callback.
          return;
        }
        E parsed = getParsedDataClass(
            requestInfo.errorClass, response, requestInfo, responseHeaders.getContentType());
        callback.onFailure(parsed, responseHeaders);
      }
    }
  }

  @SuppressWarnings("deprecation")
  private <A, T, E> A getParsedDataClass(
      Class<A> dataClass, HttpResponse response, RequestInfo<T, E> requestInfo, String contentType)
      throws IOException {
    // TODO(mlinder): Remove the HttpResponse reference and directly parse the InputStream
    com.google.api.client.http.HttpParser oldParser = requestInfo.request.getParser(contentType);
    ObjectParser parser = requestInfo.request.getParser();
    A parsed = null;
    if (dataClass != Void.class) {
      parsed = parser != null ? parser.parseAndClose(
          response.getContent(), response.getContentCharset(), dataClass) : oldParser.parse(
          response, dataClass);
    }
    return parsed;
  }

  /** Create a fake HTTP response object populated with the partContent and the statusCode. */
  @Deprecated
  private HttpResponse getFakeResponse(final int statusCode, final String partContent,
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
   * If the boundary line consists of the boundary and "--" then there are no more individual
   * responses left to be parsed and the input stream is closed.
   */
  private void checkForFinalBoundary(String boundaryLine) throws IOException {
    if (boundaryLine.equals(boundary + "--")) {
      hasNext = false;
      bufferedReader.close();
    }
  }

  @Deprecated
  private static class FakeResponseHttpTransport extends HttpTransport {

    private int statusCode;
    private String partContent;
    private List<String> headerNames;
    private List<String> headerValues;

    FakeResponseHttpTransport(
        int statusCode, String partContent, List<String> headerNames, List<String> headerValues) {
      super();
      this.statusCode = statusCode;
      this.partContent = partContent;
      this.headerNames = headerNames;
      this.headerValues = headerValues;
    }

    @Override
    protected LowLevelHttpRequest buildDeleteRequest(String url) {
      return null;
    }

    @Override
    protected LowLevelHttpRequest buildGetRequest(String url) {
      return null;
    }

    @Override
    protected LowLevelHttpRequest buildPostRequest(String url) {
      return new FakeLowLevelHttpRequest(partContent, statusCode, headerNames, headerValues);
    }

    @Override
    protected LowLevelHttpRequest buildPutRequest(String url) {
      return null;
    }
  }

  @Deprecated
  private static class FakeLowLevelHttpRequest extends LowLevelHttpRequest {

    // TODO(rmistry): Read in partContent as bytes instead of String for efficiency.
    private String partContent;
    private int statusCode;
    private List<String> headerNames;
    private List<String> headerValues;

    FakeLowLevelHttpRequest(
        String partContent, int statusCode, List<String> headerNames, List<String> headerValues) {
      this.partContent = partContent;
      this.statusCode = statusCode;
      this.headerNames = headerNames;
      this.headerValues = headerValues;
    }

    @Override
    public void addHeader(String name, String value) {
    }

    @Override
    public void setContent(HttpContent content) {
    }

    @Override
    public LowLevelHttpResponse execute() {
      FakeLowLevelHttpResponse response = new FakeLowLevelHttpResponse(new ByteArrayInputStream(
          StringUtils.getBytesUtf8(partContent)), statusCode, headerNames, headerValues);
      return response;
    }
  }

  @Deprecated
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
