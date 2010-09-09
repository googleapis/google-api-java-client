/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.apache;

import com.google.api.client.http.LowLevelHttpTransport;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * HTTP low-level transport based on the Apache HTTP Client library.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ApacheHttpTransport extends LowLevelHttpTransport {

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
   */
  public static final ApacheHttpTransport INSTANCE = new ApacheHttpTransport();

  ApacheHttpTransport() {
    // Turn off stale checking. Our connections break all the time anyway,
    // and it's not worth it to pay the penalty of checking every time.
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setStaleCheckingEnabled(params, false);
    // Default connection and socket timeout of 20 seconds. Tweak to taste.
    HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
    HttpConnectionParams.setSoTimeout(params, 20 * 1000);
    HttpConnectionParams.setSocketBufferSize(params, 8192);
    params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    this.httpClient = new DefaultHttpClient(params);
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
    return new ApacheHttpRequest(this.httpClient, new HttpDelete(url));
  }

  @Override
  public ApacheHttpRequest buildGetRequest(String url) {
    return new ApacheHttpRequest(this.httpClient, new HttpGet(url));
  }

  @Override
  public ApacheHttpRequest buildHeadRequest(String url) {
    return new ApacheHttpRequest(this.httpClient, new HttpHead(url));
  }

  @Override
  public ApacheHttpRequest buildPatchRequest(String url) {
    return new ApacheHttpRequest(this.httpClient, new HttpPatch(url));
  }

  @Override
  public ApacheHttpRequest buildPostRequest(String url) {
    return new ApacheHttpRequest(this.httpClient, new HttpPost(url));
  }

  @Override
  public ApacheHttpRequest buildPutRequest(String url) {
    return new ApacheHttpRequest(this.httpClient, new HttpPut(url));
  }
}
