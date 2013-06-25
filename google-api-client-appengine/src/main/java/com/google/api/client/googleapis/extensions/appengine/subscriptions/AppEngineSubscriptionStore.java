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

package com.google.api.client.googleapis.extensions.appengine.subscriptions;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.notifications.StoredChannel;
import com.google.api.client.googleapis.subscriptions.StoredSubscription;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * {@link Beta} <br/>
 * Persistent {@link SubscriptionStore} making use of native DataStore on AppEngine.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    service.setSubscriptionStore(new AppEngineSubscriptionStore());
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 * @deprecated (scheduled to be removed in 1.17) Use
 *             {@link StoredChannel#getDefaultDataStore(DataStoreFactory)} with
 *             {@link AppEngineDataStoreFactory} instead, possibly setting
 *             {@link AppEngineDataStoreFactory.Builder#setDisableMemcache} to {@code true}.
 */
@Deprecated
@Beta
public class AppEngineSubscriptionStore implements SubscriptionStore {

  /** Name of the table in the AppEngine datastore. */
  private static final String KIND = AppEngineSubscriptionStore.class.getName();

  /** Name of the field in which the subscription is stored. */
  private static final String FIELD_SUBSCRIPTION = "serializedSubscription";

  /**
   * Creates a new {@link AppEngineSubscriptionStore}.
   */
  public AppEngineSubscriptionStore() {
  }

  /** Serializes the specified object into a Blob using an {@link ObjectOutputStream}. */
  private Blob serialize(Object obj) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      new ObjectOutputStream(baos).writeObject(obj);
      return new Blob(baos.toByteArray());
    } finally {
      baos.close();
    }
  }

  /** Deserializes the specified object from a Blob using an {@link ObjectInputStream}. */
  @SuppressWarnings("unchecked")
  private <T> T deserialize(Blob data, Class<T> dataType) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
    try {
      Object obj = new ObjectInputStream(bais).readObject();
      if (!dataType.isAssignableFrom(obj.getClass())) {
        return null;
      }
      return (T) obj;
    } catch (ClassNotFoundException exception) {
      throw new IOException("Failed to deserialize object", exception);
    } finally {
      bais.close();
    }
  }

  /** Parses the specified Entity and returns the contained Subscription object. */
  private StoredSubscription getSubscriptionFromEntity(Entity entity) throws IOException {
    Blob serializedSubscription = (Blob) entity.getProperty(FIELD_SUBSCRIPTION);
    return deserialize(serializedSubscription, StoredSubscription.class);
  }

  @Override
  public void storeSubscription(StoredSubscription subscription) throws IOException {
    DatastoreService service = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity(KIND, subscription.getId());
    entity.setProperty(FIELD_SUBSCRIPTION, serialize(subscription));
    service.put(entity);
  }

  @Override
  public void removeSubscription(StoredSubscription subscription) throws IOException {
    if (subscription == null) {
      return;
    }

    DatastoreService service = DatastoreServiceFactory.getDatastoreService();
    service.delete(KeyFactory.createKey(KIND, subscription.getId()));
  }

  @Override
  public List<StoredSubscription> listSubscriptions() throws IOException {
    List<StoredSubscription> list = Lists.newArrayList();
    DatastoreService service = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = service.prepare(new Query(KIND));

    for (Entity entity : results.asIterable()) {
      list.add(getSubscriptionFromEntity(entity));
    }

    return list;
  }

  @Override
  public StoredSubscription getSubscription(String subscriptionId) throws IOException {
    try {
      DatastoreService service = DatastoreServiceFactory.getDatastoreService();
      Entity entity = service.get(KeyFactory.createKey(KIND, subscriptionId));
      return getSubscriptionFromEntity(entity);
    } catch (EntityNotFoundException exception) {
      return null;
    }
  }
}
