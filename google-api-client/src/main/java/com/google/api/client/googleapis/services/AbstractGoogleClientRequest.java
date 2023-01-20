/*
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

package com.google.api.client.googleapis.services;

import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static com.google.common.base.StandardSystemProperty.OS_VERSION;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.batch.BatchCallback;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GZipEncoding;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpResponseInterceptor;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Preconditions;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract Google client request for a {@link AbstractGoogleClient}.
 *
 * <p>Implementation is not thread-safe.
 *
 * @param <T> type of the response
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class AbstractGoogleClientRequest<T> extends GenericData {

  /**
   * User agent suffix for all requests.
   *
   * @since 1.20
   */
  public static final String USER_AGENT_SUFFIX = "Google-API-Java-Client";

  private static final String API_VERSION_HEADER = "X-Goog-Api-Client";

  /** Google client. */
  private final AbstractGoogleClient abstractGoogleClient;

  /** HTTP method. */
  private final String requestMethod;

  /** URI template for the path relative to the base URL. */
  private final String uriTemplate;

  /** HTTP content or {@code null} for none. */
  private final HttpContent httpContent;

  /** HTTP headers used for the Google client request. */
  private HttpHeaders requestHeaders = new HttpHeaders();

  /** HTTP headers of the last response or {@code null} before request has been executed. */
  private HttpHeaders lastResponseHeaders;

  /** Status code of the last response or {@code -1} before request has been executed. */
  private int lastStatusCode = -1;

  /** Status message of the last response or {@code null} before request has been executed. */
  private String lastStatusMessage;

  /** Whether to disable GZip compression of HTTP content. */
  private boolean disableGZipContent;

  /** Whether to return raw input stream in {@link HttpResponse#getContent()}. */
  private boolean returnRawInputStream;

  /** Response class to parse into. */
  private Class<T> responseClass;

  /** Media HTTP uploader or {@code null} for none. */
  private MediaHttpUploader uploader;

  /** Media HTTP downloader or {@code null} for none. */
  private MediaHttpDownloader downloader;

  /**
   * @param abstractGoogleClient Google client
   * @param requestMethod HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *     the base path from the base URL will be stripped out. The URI template can also be a full
   *     URL. URI template expansion is done using {@link UriTemplate#expand(String, String, Object,
   *     boolean)}
   * @param httpContent HTTP content or {@code null} for none
   * @param responseClass response class to parse into
   */
  protected AbstractGoogleClientRequest(
      AbstractGoogleClient abstractGoogleClient,
      String requestMethod,
      String uriTemplate,
      HttpContent httpContent,
      Class<T> responseClass) {
    this.responseClass = Preconditions.checkNotNull(responseClass);
    this.abstractGoogleClient = Preconditions.checkNotNull(abstractGoogleClient);
    this.requestMethod = Preconditions.checkNotNull(requestMethod);
    this.uriTemplate = Preconditions.checkNotNull(uriTemplate);
    this.httpContent = httpContent;
    // application name
    String applicationName = abstractGoogleClient.getApplicationName();
    if (applicationName != null) {
      requestHeaders.setUserAgent(
          applicationName + " " + USER_AGENT_SUFFIX + "/" + GoogleUtils.VERSION);
    } else {
      requestHeaders.setUserAgent(USER_AGENT_SUFFIX + "/" + GoogleUtils.VERSION);
    }
    // Set the header for the Api Client version (Java and OS version)
    requestHeaders.set(API_VERSION_HEADER, ApiClientVersion.DEFAULT_VERSION);
  }

  /**
   * Internal class to help build the X-Goog-Api-Client header. This header identifies the API
   * Client version and environment.
   *
   * <p>See <a href="https://cloud.google.com/apis/docs/system-parameters"></a>
   */
  static class ApiClientVersion {
    static final String DEFAULT_VERSION = new ApiClientVersion().toString();
    private final String versionString;

    ApiClientVersion() {
      this(getJavaVersion(), OS_NAME.value(), OS_VERSION.value(), GoogleUtils.VERSION);
    }

    ApiClientVersion(String javaVersion, String osName, String osVersion, String clientVersion) {
      StringBuilder sb = new StringBuilder("gl-java/");
      sb.append(formatSemver(javaVersion));
      sb.append(" gdcl/");
      sb.append(formatSemver(clientVersion));
      if (osName != null && osVersion != null) {
        sb.append(" ");
        sb.append(formatName(osName));
        sb.append("/");
        sb.append(formatSemver(osVersion));
      }
      this.versionString = sb.toString();
    }

    public String toString() {
      // When running the application as a native image, append `-graalvm` to the
      // version.
      String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
      if (imageCode != null && imageCode.equals("runtime")) {
        String[] tokens = versionString.split(" ");
        if (tokens.length > 0 && tokens[0].startsWith("gl-java")) {
          tokens[0] += "-graalvm";
          return Joiner.on(" ").join(tokens);
        }
      }
      return versionString;
    }

    private static String getJavaVersion() {
      String version = System.getProperty("java.version");
      if (version == null) {
        return null;
      }

      // Try parsing the full semver
      String formatted = formatSemver(version, null);
      if (formatted != null) {
        return formatted;
      }

      // Some java versions start with the version number and may contain extra info
      // e.g. Java 9 reports something like 9-Debian+0-x-y while Java 11 reports "11"
      Matcher m = Pattern.compile("^(\\d+)[^\\d]?").matcher(version);
      if (m.find()) {
        return m.group(1) + ".0.0";
      }

      return null;
    }

    private static String formatName(String name) {
      // Only lowercase letters, digits, and "-" are allowed
      return name.toLowerCase().replaceAll("[^\\w\\d\\-]", "-");
    }

    private static String formatSemver(String version) {
      return formatSemver(version, version);
    }

    private static String formatSemver(String version, String defaultValue) {
      if (version == null) {
        return null;
      }

      // Take only the semver version: x.y.z-a_b_c -> x.y.z
      Matcher m = Pattern.compile("(\\d+\\.\\d+\\.\\d+).*").matcher(version);
      if (m.find()) {
        return m.group(1);
      } else {
        return defaultValue;
      }
    }
  }

  /** Returns whether to disable GZip compression of HTTP content. */
  public final boolean getDisableGZipContent() {
    return disableGZipContent;
  }

  /**
   * Returns whether response should return raw input stream.
   *
   * @since 1.30
   */
  public final boolean getReturnRawInputSteam() {
    return returnRawInputStream;
  }

  /**
   * Sets whether to disable GZip compression of HTTP content.
   *
   * <p>By default it is {@code false}.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AbstractGoogleClientRequest<T> setDisableGZipContent(boolean disableGZipContent) {
    this.disableGZipContent = disableGZipContent;
    return this;
  }

  /**
   * Sets whether the response should return raw input stream or not.
   *
   * <p>By default it is {@code false}.
   *
   * <p>When the response contains a known content-encoding header, the response stream is wrapped
   * with an InputStream that decodes the content. This fails when we download large files in chunks
   * (see <a href="https://github.com/googleapis/google-api-java-client/issues/1009">#1009 </a>).
   * Setting this to true will make the response return the raw input stream.
   *
   * @since 1.30
   */
  public AbstractGoogleClientRequest<T> setReturnRawInputStream(boolean returnRawInputStream) {
    this.returnRawInputStream = returnRawInputStream;
    return this;
  }

  /** Returns the HTTP method. */
  public final String getRequestMethod() {
    return requestMethod;
  }

  /** Returns the URI template for the path relative to the base URL. */
  public final String getUriTemplate() {
    return uriTemplate;
  }

  /** Returns the HTTP content or {@code null} for none. */
  public final HttpContent getHttpContent() {
    return httpContent;
  }

  /**
   * Returns the Google client.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AbstractGoogleClient getAbstractGoogleClient() {
    return abstractGoogleClient;
  }

  /** Returns the HTTP headers used for the Google client request. */
  public final HttpHeaders getRequestHeaders() {
    return requestHeaders;
  }

  /**
   * Sets the HTTP headers used for the Google client request.
   *
   * <p>These headers are set on the request after {@link #buildHttpRequest} is called, this means
   * that {@link HttpRequestInitializer#initialize} is called first.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AbstractGoogleClientRequest<T> setRequestHeaders(HttpHeaders headers) {
    this.requestHeaders = headers;
    return this;
  }

  /**
   * Returns the HTTP headers of the last response or {@code null} before request has been executed.
   */
  public final HttpHeaders getLastResponseHeaders() {
    return lastResponseHeaders;
  }

  /**
   * Returns the status code of the last response or {@code -1} before request has been executed.
   */
  public final int getLastStatusCode() {
    return lastStatusCode;
  }

  /**
   * Returns the status message of the last response or {@code null} before request has been
   * executed.
   */
  public final String getLastStatusMessage() {
    return lastStatusMessage;
  }

  /** Returns the response class to parse into. */
  public final Class<T> getResponseClass() {
    return responseClass;
  }

  /** Returns the media HTTP Uploader or {@code null} for none. */
  public final MediaHttpUploader getMediaHttpUploader() {
    return uploader;
  }

  /**
   * Initializes the media HTTP uploader based on the media content.
   *
   * @param mediaContent media content
   */
  protected final void initializeMediaUpload(AbstractInputStreamContent mediaContent) {
    HttpRequestFactory requestFactory = abstractGoogleClient.getRequestFactory();
    String applicationName = abstractGoogleClient.getApplicationName();
    HttpRequestInitializer requestInitializer =
        mediaUploadRequestUserAgentInitializer(applicationName, requestFactory.getInitializer());
    this.uploader =
        new MediaHttpUploader(mediaContent, requestFactory.getTransport(), requestInitializer);
    this.uploader.setInitiationRequestMethod(requestMethod);
    if (httpContent != null) {
      this.uploader.setMetadata(httpContent);
    }
  }

  private static HttpRequestInitializer mediaUploadRequestUserAgentInitializer(
      final String applicationName, final HttpRequestInitializer originalInitializer) {
    if (applicationName == null) {
      return originalInitializer;
    }
    if (originalInitializer == null) {
      return new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) {
          HttpHeaders headers = request.getHeaders();
          headers.setUserAgent(applicationName);
        }
      };
    } else {
      return new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) throws IOException {
          originalInitializer.initialize(request);
          HttpHeaders headers = request.getHeaders();
          headers.setUserAgent(applicationName);
        }
      };
    }
  }

  /** Returns the media HTTP downloader or {@code null} for none. */
  public final MediaHttpDownloader getMediaHttpDownloader() {
    return downloader;
  }

  /** Initializes the media HTTP downloader. */
  protected final void initializeMediaDownload() {
    HttpRequestFactory requestFactory = abstractGoogleClient.getRequestFactory();
    this.downloader =
        new MediaHttpDownloader(requestFactory.getTransport(), requestFactory.getInitializer());
  }

  /**
   * Creates a new instance of {@link GenericUrl} suitable for use against this service.
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return newly created {@link GenericUrl}
   */
  public GenericUrl buildHttpRequestUrl() {
    return new GenericUrl(
        UriTemplate.expand(abstractGoogleClient.getBaseUrl(), uriTemplate, this, true));
  }

  /**
   * Create a request suitable for use against this service.
   *
   * <p>Subclasses may override by calling the super implementation.
   */
  public HttpRequest buildHttpRequest() throws IOException {
    return buildHttpRequest(false);
  }

  /**
   * Create a request suitable for use against this service, but using HEAD instead of GET.
   *
   * <p>Only supported when the original request method is GET.
   *
   * <p>Subclasses may override by calling the super implementation.
   */
  protected HttpRequest buildHttpRequestUsingHead() throws IOException {
    return buildHttpRequest(true);
  }

  /** Create a request suitable for use against this service. */
  private HttpRequest buildHttpRequest(boolean usingHead) throws IOException {
    Preconditions.checkArgument(uploader == null);
    Preconditions.checkArgument(!usingHead || requestMethod.equals(HttpMethods.GET));
    String requestMethodToUse = usingHead ? HttpMethods.HEAD : requestMethod;
    final HttpRequest httpRequest =
        getAbstractGoogleClient()
            .getRequestFactory()
            .buildRequest(requestMethodToUse, buildHttpRequestUrl(), httpContent);
    new MethodOverride().intercept(httpRequest);
    httpRequest.setParser(getAbstractGoogleClient().getObjectParser());
    // custom methods may use POST with no content but require a Content-Length header
    if (httpContent == null
        && (requestMethod.equals(HttpMethods.POST)
            || requestMethod.equals(HttpMethods.PUT)
            || requestMethod.equals(HttpMethods.PATCH))) {
      httpRequest.setContent(new EmptyContent());
    }
    httpRequest.getHeaders().putAll(requestHeaders);
    if (!disableGZipContent) {
      httpRequest.setEncoding(new GZipEncoding());
    }
    httpRequest.setResponseReturnRawInputStream(returnRawInputStream);
    final HttpResponseInterceptor responseInterceptor = httpRequest.getResponseInterceptor();
    httpRequest.setResponseInterceptor(
        new HttpResponseInterceptor() {

          public void interceptResponse(HttpResponse response) throws IOException {
            if (responseInterceptor != null) {
              responseInterceptor.interceptResponse(response);
            }
            if (!response.isSuccessStatusCode() && httpRequest.getThrowExceptionOnExecuteError()) {
              throw newExceptionOnError(response);
            }
          }
        });
    return httpRequest;
  }

  /**
   * Sends the metadata request to the server and returns the raw metadata {@link HttpResponse}.
   *
   * <p>Callers are responsible for disconnecting the HTTP response by calling {@link
   * HttpResponse#disconnect}. Example usage:
   *
   * <pre>{@code
   * HttpResponse response = request.executeUnparsed();
   * try {
   *   // process response..
   * } finally {
   *   response.disconnect();
   * }
   * }</pre>
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return the {@link HttpResponse}
   */
  public HttpResponse executeUnparsed() throws IOException {
    return executeUnparsed(false);
  }

  /**
   * Sends the media request to the server and returns the raw media {@link HttpResponse}.
   *
   * <p>Callers are responsible for disconnecting the HTTP response by calling {@link
   * HttpResponse#disconnect}. Example usage:
   *
   * <pre>{@code
   * HttpResponse response = request.executeMedia();
   * try {
   *   // process response..
   * } finally {
   *   response.disconnect();
   * }
   * }</pre>
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return the {@link HttpResponse}
   */
  protected HttpResponse executeMedia() throws IOException {
    set("alt", "media");
    return executeUnparsed();
  }

  /**
   * Sends the metadata request using HEAD to the server and returns the raw metadata {@link
   * HttpResponse} for the response headers.
   *
   * <p>Only supported when the original request method is GET. The response content is assumed to
   * be empty and ignored. Calls {@link HttpResponse#ignore()} so there is no need to disconnect the
   * response. Example usage:
   *
   * <pre>{@code
   * HttpResponse response = request.executeUsingHead();
   * // look at response.getHeaders()
   * }</pre>
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return the {@link HttpResponse}
   */
  protected HttpResponse executeUsingHead() throws IOException {
    Preconditions.checkArgument(uploader == null);
    HttpResponse response = executeUnparsed(true);
    response.ignore();
    return response;
  }

  /**
   * Sends the metadata request using the given request method to the server and returns the raw
   * metadata {@link HttpResponse}.
   */
  private HttpResponse executeUnparsed(boolean usingHead) throws IOException {
    HttpResponse response;
    if (uploader == null) {
      // normal request (not upload)
      response = buildHttpRequest(usingHead).execute();
    } else {
      // upload request
      GenericUrl httpRequestUrl = buildHttpRequestUrl();
      HttpRequest httpRequest =
          getAbstractGoogleClient()
              .getRequestFactory()
              .buildRequest(requestMethod, httpRequestUrl, httpContent);
      boolean throwExceptionOnExecuteError = httpRequest.getThrowExceptionOnExecuteError();

      response =
          uploader
              .setInitiationHeaders(requestHeaders)
              .setDisableGZipContent(disableGZipContent)
              .upload(httpRequestUrl);
      response.getRequest().setParser(getAbstractGoogleClient().getObjectParser());
      // process any error
      if (throwExceptionOnExecuteError && !response.isSuccessStatusCode()) {
        throw newExceptionOnError(response);
      }
    }
    // process response
    lastResponseHeaders = response.getHeaders();
    lastStatusCode = response.getStatusCode();
    lastStatusMessage = response.getStatusMessage();
    return response;
  }

  /**
   * Returns the exception to throw on an HTTP error response as defined by {@link
   * HttpResponse#isSuccessStatusCode()}.
   *
   * <p>It is guaranteed that {@link HttpResponse#isSuccessStatusCode()} is {@code false}. Default
   * implementation is to call {@link HttpResponseException#HttpResponseException(HttpResponse)},
   * but subclasses may override.
   *
   * @param response HTTP response
   * @return exception to throw
   */
  protected IOException newExceptionOnError(HttpResponse response) {
    return new HttpResponseException(response);
  }

  /**
   * Sends the metadata request to the server and returns the parsed metadata response.
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return parsed HTTP response
   */
  public T execute() throws IOException {
    return executeUnparsed().parseAs(responseClass);
  }

  /**
   * Sends the metadata request to the server and returns the metadata content input stream of
   * {@link HttpResponse}.
   *
   * <p>Callers are responsible for closing the input stream after it is processed. Example sample:
   *
   * <pre>{@code
   * InputStream is = request.executeAsInputStream();
   * try {
   *   // Process input stream..
   * } finally {
   *   is.close();
   * }
   * }</pre>
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return input stream of the response content
   */
  public InputStream executeAsInputStream() throws IOException {
    return executeUnparsed().getContent();
  }

  /**
   * Sends the media request to the server and returns the media content input stream of {@link
   * HttpResponse}.
   *
   * <p>Callers are responsible for closing the input stream after it is processed. Example sample:
   *
   * <pre>{@code
   * InputStream is = request.executeMediaAsInputStream();
   * try {
   *   // Process input stream..
   * } finally {
   *   is.close();
   * }
   * }</pre>
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @return input stream of the response content
   */
  protected InputStream executeMediaAsInputStream() throws IOException {
    return executeMedia().getContent();
  }

  /**
   * Sends the metadata request to the server and writes the metadata content input stream of {@link
   * HttpResponse} into the given destination output stream.
   *
   * <p>This method closes the content of the HTTP response from {@link HttpResponse#getContent()}.
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @param outputStream destination output stream
   */
  public void executeAndDownloadTo(OutputStream outputStream) throws IOException {
    executeUnparsed().download(outputStream);
  }

  /**
   * Sends the media request to the server and writes the media content input stream of {@link
   * HttpResponse} into the given destination output stream.
   *
   * <p>This method closes the content of the HTTP response from {@link HttpResponse#getContent()}.
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @param outputStream destination output stream
   */
  protected void executeMediaAndDownloadTo(OutputStream outputStream) throws IOException {
    if (downloader == null) {
      executeMedia().download(outputStream);
    } else {
      downloader.download(buildHttpRequestUrl(), requestHeaders, outputStream);
    }
  }

  /**
   * Queues the request into the specified batch request container using the specified error class.
   *
   * <p>Batched requests are then executed when {@link BatchRequest#execute()} is called.
   *
   * @param batchRequest batch request container
   * @param errorClass data class the unsuccessful response will be parsed into or {@code
   *     Void.class} to ignore the content
   * @param callback batch callback
   */
  public final <E> void queue(
      BatchRequest batchRequest, Class<E> errorClass, BatchCallback<T, E> callback)
      throws IOException {
    Preconditions.checkArgument(uploader == null, "Batching media requests is not supported");
    batchRequest.queue(buildHttpRequest(), getResponseClass(), errorClass, callback);
  }

  // @SuppressWarnings was added here because this is generic class.
  // see: http://stackoverflow.com/questions/4169806/java-casting-object-to-a-generic-type and
  // http://www.angelikalanger.com/GenericsFAQ/FAQSections/TechnicalDetails.html#Type%20Erasure
  // for more details
  @SuppressWarnings("unchecked")
  @Override
  public AbstractGoogleClientRequest<T> set(String fieldName, Object value) {
    return (AbstractGoogleClientRequest<T>) super.set(fieldName, value);
  }

  /**
   * Ensures that the specified required parameter is not null or {@link
   * AbstractGoogleClient#getSuppressRequiredParameterChecks()} is true.
   *
   * @param value the value of the required parameter
   * @param name the name of the required parameter
   * @throws IllegalArgumentException if the specified required parameter is null and {@link
   *     AbstractGoogleClient#getSuppressRequiredParameterChecks()} is false
   * @since 1.14
   */
  protected final void checkRequiredParameter(Object value, String name) {
    Preconditions.checkArgument(
        abstractGoogleClient.getSuppressRequiredParameterChecks() || value != null,
        "Required parameter %s must be specified",
        name);
  }
}
