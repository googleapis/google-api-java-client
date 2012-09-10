/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.googleapis.extensions.servlet.subscriptions;

import com.google.api.client.googleapis.subscriptions.Subscription;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * {@link SubscriptionStore} making use of JDO.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    SubscriptionStore store = new JdoSubscriptionStore();
    service.setSubscriptionManager(new SubscriptionManager(store));
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.12
 */
public final class JdoSubscriptionStore implements SubscriptionStore {

  /** The persistence manager factory used to create JDO persistence managers. */
  private final PersistenceManagerFactory persistenceManagerFactory;

  /**
   * Creates a new {@link JdoSubscriptionStore} using the specified PersistenceManagerFactory.
   *
   * @param persistenceManagerFactory Initialized PersistenceManager used to store and retrieve data
   */
  public JdoSubscriptionStore(PersistenceManagerFactory persistenceManagerFactory) {
    this.persistenceManagerFactory = persistenceManagerFactory;
  }

  /** Container class for storing subscriptions in the JDO DataStore. */
  @PersistenceCapable
  private static class StoredSubscription {

    @Persistent(serialized = "true")
    private Subscription subscription;

    @Persistent
    @PrimaryKey
    private String subscriptionId;

    @SuppressWarnings("unused")
    StoredSubscription() {
    }

    /**
     * Creates a StoredSubscription from an existing Subscription.
     *
     * @param s Subscription to store
     */
    public StoredSubscription(Subscription s) {
      setSubscription(s);
    }

    /**
     * Returns the stored subscription.
     */
    public Subscription getSubscription() {
      return subscription;
    }

    /**
     * ID of the contained subscription.
     */
    @SuppressWarnings("unused")
    public String getSubscriptionId() {
      return subscriptionId;
    }

    /**
     * Changes the subscription stored in this database entry.
     * @param subscription The new subscription to store
     */
    public void setSubscription(Subscription subscription) {
      this.subscription = subscription;
      this.subscriptionId = subscription.getSubscriptionID();
    }
  }

  public void storeSubscription(Subscription subscription) {
    Preconditions.checkNotNull(subscription);

    StoredSubscription dbEntry = getStoredSubscription(subscription.getSubscriptionID());
    PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
    try {
      // Check if this subscription is being updated or newly created
      if (dbEntry != null) { // Existing entry
        dbEntry.setSubscription(subscription);
      } else { // New entry
        dbEntry = new StoredSubscription(subscription);
        persistenceManager.makePersistent(dbEntry); // Store the object in the datastore.
      }
    } finally {
      persistenceManager.close();
    }
  }

  public void removeSubscription(Subscription subscription) {
    StoredSubscription dbEntry = getStoredSubscription(subscription.getSubscriptionID());

    if (dbEntry != null) {
      PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
      try {
        persistenceManager.deletePersistent(dbEntry);
      } finally {
        persistenceManager.close();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<Subscription> listSubscriptions() {
    // Copy the results into a db-detached list.
    List<Subscription> list = Lists.newArrayList();
    PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
    try {
      Query listQuery = persistenceManager.newQuery(StoredSubscription.class);
      Iterable<StoredSubscription> resultList = (Iterable<StoredSubscription>) listQuery.execute();
      for (StoredSubscription dbEntry : resultList) {
        list.add(dbEntry.getSubscription());
      }
    } finally {
      persistenceManager.close();
    }

    return list;
  }

  @SuppressWarnings("unchecked")
  private StoredSubscription getStoredSubscription(String subscriptionID) {
    Iterable<StoredSubscription> results = null;

    PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
    try {
      Query getByIDQuery = persistenceManager.newQuery(StoredSubscription.class);
      getByIDQuery.setFilter("subscriptionId == idParam");
      getByIDQuery.declareParameters("String idParam");
      getByIDQuery.setRange(0, 1);
      results = (Iterable<StoredSubscription>) getByIDQuery.execute(subscriptionID);

      for (StoredSubscription dbEntry : results) {
        return dbEntry; // return the first result
      }
      return null;
    } finally {
      persistenceManager.close();
    }
  }

  public Subscription getSubscription(String subscriptionID) {
    StoredSubscription dbEntry = getStoredSubscription(subscriptionID);
    return dbEntry == null ? null : dbEntry.getSubscription();
  }
}
