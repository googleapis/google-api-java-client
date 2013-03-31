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

package com.google.api.client.googleapis.testing.services.json;

import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * Thread-safe mock Google JSON request.
 *
 * @param <T> type of the response
 * @since 1.12
 * @author Yaniv Inbar
 */
@Beta
public class MockGoogleJsonClientRequest<T> extends AbstractGoogleJsonClientRequest<T> {

  /**
   * @param client Google client
   * @param method HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *        the base path from the base URL will be stripped out. The URI template can also be a
   *        full URL. URI template expansion is done using
   *        {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param content A POJO that can be serialized into JSON or {@code null} for none
   */
  public MockGoogleJsonClientRequest(AbstractGoogleJsonClient client, String method,
      String uriTemplate, Object content, Class<T> responseClass) {
    super(client, method, uriTemplate, content, responseClass);
  }

  @Override
  public MockGoogleJsonClient getAbstractGoogleClient() {
    return (MockGoogleJsonClient) super.getAbstractGoogleClient();
  }

  @Override
  public MockGoogleJsonClientRequest<T> setDisableGZipContent(boolean disableGZipContent) {
    return (MockGoogleJsonClientRequest<T>) super.setDisableGZipContent(disableGZipContent);
  }

  @Override
  public MockGoogleJsonClientRequest<T> setRequestHeaders(HttpHeaders headers) {
    return (MockGoogleJsonClientRequest<T>) super.setRequestHeaders(headers);
  }
}
