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

package com.google.api.client.extensions.appengine.http.urlfetch;

import com.google.api.client.http.HttpTransport;
import com.google.appengine.api.urlfetch.FetchOptions;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * HTTP transport for Google App Engine based on <a
 * href="http://code.google.com/appengine/docs/java/urlfetch/">URL Fetch</a>.
 * <p>
 * URL Fetch is only available on Google App Engine (not on any other Java environment), and is the
 * underlying HTTP transport used for App Engine. Their implementation of {@link HttpURLConnection}
 * is simply an abstraction layer on top of URL Fetch. By implementing a transport that directly
 * uses URL Fetch, we can optimize the behavior slightly, and can potentially take advantage of
 * features in URL Fetch that are not available in {@link HttpURLConnection}. Furthermore, there is
 * currently a serious bug in how HTTP headers are processed in the App Engine implementation of
 * {@link HttpURLConnection}, which we are able to avoid using this implementation. Therefore, this
 * is the recommended transport to use on App Engine.
 * </p>
 *
 * @since 1.2
 * @author Yaniv Inbar
 */
public final class UrlFetchTransport extends HttpTransport {

  /**
   * Singleton instance of this transport.
   *
   * @deprecated (scheduled to be removed in 1.4) Use {@link #UrlFetchTransport()}
   */
  @Deprecated
  public static volatile UrlFetchTransport INSTANCE = new UrlFetchTransport();

  /**
   * Sets the deadline in seconds or {@code null} for no deadline using
   * {@link FetchOptions#setDeadline(Double)}. By default it is 20 seconds.
   */
  public Double deadline = 20.0;

  @Override
  public boolean supportsHead() {
    return true;
  }

  @Override
  public UrlFetchRequest buildDeleteRequest(String url) throws IOException {
    return new UrlFetchRequest(this, "DELETE", url);
  }

  @Override
  public UrlFetchRequest buildGetRequest(String url) throws IOException {
    return new UrlFetchRequest(this, "GET", url);
  }

  @Override
  public UrlFetchRequest buildHeadRequest(String url) throws IOException {
    return new UrlFetchRequest(this, "HEAD", url);
  }

  @Override
  public UrlFetchRequest buildPostRequest(String url) throws IOException {
    return new UrlFetchRequest(this, "POST", url);
  }

  @Override
  public UrlFetchRequest buildPutRequest(String url) throws IOException {
    return new UrlFetchRequest(this, "PUT", url);
  }
}
