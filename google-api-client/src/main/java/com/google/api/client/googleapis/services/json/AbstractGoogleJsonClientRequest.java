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

package com.google.api.client.googleapis.services.json;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.json.JsonCContent;
import com.google.api.client.googleapis.json.JsonCParser;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.subscriptions.json.JsonSubscribeRequest;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.json.JsonHttpContent;

import java.io.IOException;

/**
 * Google JSON request for a {@link AbstractGoogleJsonClient}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @param <T> type of the response
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class AbstractGoogleJsonClientRequest<T> extends AbstractGoogleClientRequest<T> {

  /** POJO that can be serialized into JSON content or {@code null} for none. */
  private final Object jsonContent;

  /**
   * @param abstractGoogleJsonClient Google JSON client
   * @param requestMethod HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *        the base path from the base URL will be stripped out. The URI template can also be a
   *        full URL. URI template expansion is done using
   *        {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param jsonContent POJO that can be serialized into JSON content or {@code null} for none
   * @param responseClass response class to parse into
   */
  protected AbstractGoogleJsonClientRequest(AbstractGoogleJsonClient abstractGoogleJsonClient,
      String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass) {
    super(abstractGoogleJsonClient, requestMethod, uriTemplate, jsonContent == null
        ? null : (abstractGoogleJsonClient.getObjectParser() instanceof JsonCParser)
            ? new JsonCContent(abstractGoogleJsonClient.getJsonFactory(), jsonContent)
            : new JsonHttpContent(abstractGoogleJsonClient.getJsonFactory(), jsonContent),
        responseClass);
    this.jsonContent = jsonContent;
  }

  @Override
  public AbstractGoogleJsonClient getAbstractGoogleClient() {
    return (AbstractGoogleJsonClient) super.getAbstractGoogleClient();
  }

  @Override
  public AbstractGoogleJsonClientRequest<T> setDisableGZipContent(boolean disableGZipContent) {
    return (AbstractGoogleJsonClientRequest<T>) super.setDisableGZipContent(disableGZipContent);
  }

  @Override
  public AbstractGoogleJsonClientRequest<T> setRequestHeaders(HttpHeaders headers) {
    return (AbstractGoogleJsonClientRequest<T>) super.setRequestHeaders(headers);
  }

  /**
   * Queues the request into the specified batch request container.
   *
   * <p>
   * Batched requests are then executed when {@link BatchRequest#execute()} is called.
   * </p>
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
     request.queue(batchRequest, new JsonBatchCallback&lt;SomeResponseType&gt;() {

       public void onSuccess(SomeResponseType content, HttpHeaders responseHeaders) {
         log("Success");
       }

       public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
         log(e.getMessage());
       }
     });
   * </pre>
   *
   *
   * @param batchRequest batch request container
   * @param callback batch callback
   */
  public final void queue(BatchRequest batchRequest, JsonBatchCallback<T> callback)
      throws IOException {
    super.queue(batchRequest, GoogleJsonErrorContainer.class, callback);
  }

  @Override
  protected GoogleJsonResponseException newExceptionOnError(HttpResponse response) {
    return GoogleJsonResponseException.from(getAbstractGoogleClient().getJsonFactory(), response);
  }

  /**
   * Subscribes to parsed JSON notifications.
   *
   * <p>
   * Overriding is only supported for the purpose of changing visibility to public, but nothing
   * else.
   * </p>
   *
   * @param notificationDeliveryMethod notification delivery method
   * @throws IOException
   *
   * @since 1.14
   */
  @Override
  protected JsonSubscribeRequest subscribe(String notificationDeliveryMethod) throws IOException {
    return new JsonSubscribeRequest(
        buildHttpRequest(), notificationDeliveryMethod, getAbstractGoogleClient().getJsonFactory());
  }

  /** Returns POJO that can be serialized into JSON content or {@code null} for none. */
  public Object getJsonContent() {
    return jsonContent;
  }

  @Override
  public AbstractGoogleJsonClientRequest<T> set(String fieldName, Object value) {
    return (AbstractGoogleJsonClientRequest<T>) super.set(fieldName, value);
  }
}
