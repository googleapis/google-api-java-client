/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.apache;

import com.google.api.client.http.HttpTransport;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * HTTP transport based on the Apache HTTP Client library.
 * <p>
 * Default settings:
 * </p>
 * <ul>
 * <li>The client connection manager is set to {@link ThreadSafeClientConnManager}.</li>
 * <li>Timeout is set to 20 seconds using {@link ConnManagerParams#setTimeout},
 * {@link HttpConnectionParams#setConnectionTimeout}, and {@link HttpConnectionParams#setSoTimeout}.
 * </li>
 * <li>The socket buffer size is set to 8192 using {@link HttpConnectionParams#setSocketBufferSize}.
 * </li>
 * </ul>
 * <p>
 * These parameters may be overridden by setting the values on the {@link #httpClient}.
 * {@link HttpClient#getParams() getParams()}. Please read the <a
 * href="http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html">Apache HTTP
 * Client connection management tutorial</a> for more complex configuration questions, such as how
 * to set up an HTTP proxy.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ApacheHttpTransport extends HttpTransport {

  /**
   * Apache HTTP client.
   *
   * @since 1.1
   */
  public final HttpClient httpClient;

  /**
   * Singleton instance of this transport.
   * <p>
   * Sample usage:
   *
   * <pre><code>HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);</code></pre>
   * </p>
   *
   * @deprecated (scheduled to be removed in 1.4) Use {@link #ApacheHttpTransport()}
   */
  @Deprecated
  public static final ApacheHttpTransport INSTANCE = new ApacheHttpTransport();

  /**
   * @since 1.3
   */
  public ApacheHttpTransport() {
    // Turn off stale checking. Our connections break all the time anyway,
    // and it's not worth it to pay the penalty of checking every time.
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setStaleCheckingEnabled(params, false);
    // Default connection and socket timeout of 20 seconds. Tweak to taste.
    HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
    HttpConnectionParams.setSoTimeout(params, 20 * 1000);
    HttpConnectionParams.setSocketBufferSize(params, 8192);
    ConnManagerParams.setTimeout(params, 20 * 1000);
    params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    // See http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e596
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
    ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, registry);
    httpClient = new DefaultHttpClient(connectionManager, params);
  }

  @Override
  public boolean supportsHead() {
    return true;
  }

  @Override
  public boolean supportsPatch() {
    return true;
  }

  @Override
  public ApacheHttpRequest buildDeleteRequest(String url) {
    return new ApacheHttpRequest(httpClient, new HttpDelete(url));
  }

  @Override
  public ApacheHttpRequest buildGetRequest(String url) {
    return new ApacheHttpRequest(httpClient, new HttpGet(url));
  }

  @Override
  public ApacheHttpRequest buildHeadRequest(String url) {
    return new ApacheHttpRequest(httpClient, new HttpHead(url));
  }

  @Override
  public ApacheHttpRequest buildPatchRequest(String url) {
    return new ApacheHttpRequest(httpClient, new HttpPatch(url));
  }

  @Override
  public ApacheHttpRequest buildPostRequest(String url) {
    return new ApacheHttpRequest(httpClient, new HttpPost(url));
  }

  @Override
  public ApacheHttpRequest buildPutRequest(String url) {
    return new ApacheHttpRequest(httpClient, new HttpPut(url));
  }
}
