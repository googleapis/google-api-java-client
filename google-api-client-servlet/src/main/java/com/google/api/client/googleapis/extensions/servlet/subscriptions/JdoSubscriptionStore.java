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

import com.google.api.client.googleapis.subscriptions.StoredSubscription;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.api.client.util.Experimental;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Preconditions;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * {@link Experimental} <br/>
 * {@link SubscriptionStore} making use of JDO.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    service.setSubscriptionStore(new JdoSubscriptionStore());
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
@Experimental
public final class JdoSubscriptionStore implements SubscriptionStore {

  /** Persistence manager factory. */
  private final PersistenceManagerFactory persistenceManagerFactory;

  /**
   * @param persistenceManagerFactory persistence manager factory
   */
  public JdoSubscriptionStore(PersistenceManagerFactory persistenceManagerFactory) {
    this.persistenceManagerFactory = persistenceManagerFactory;
  }

  /** Container class for storing subscriptions in the JDO DataStore. */
  @PersistenceCapable
  @Experimental
  private static final class JdoStoredSubscription {

    @Persistent(serialized = "true")
    private StoredSubscription subscription;

    @Persistent
    @PrimaryKey
    private String subscriptionId;

    @SuppressWarnings("unused")
    JdoStoredSubscription() {
    }

    /**
     * Creates a stored subscription from an existing subscription.
     *
     * @param s subscription to store
     */
    public JdoStoredSubscription(StoredSubscription s) {
      setSubscription(s);
    }

    /**
     * Returns the stored subscription.
     */
    public StoredSubscription getSubscription() {
      return subscription;
    }

    /**
     * Returns the subscription ID.
     */
    @SuppressWarnings("unused")
    public String getSubscriptionId() {
      return subscriptionId;
    }

    /**
     * Changes the subscription stored in this database entry.
     *
     * @param subscription The new subscription to store
     */
    public void setSubscription(StoredSubscription subscription) {
      this.subscription = subscription;
      this.subscriptionId = subscription.getId();
    }
  }

  public void storeSubscription(StoredSubscription subscription) {
    Preconditions.checkNotNull(subscription);

    JdoStoredSubscription dbEntry = getStoredSubscription(subscription.getId());
    PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
    try {
      // Check if this subscription is being updated or newly created
      if (dbEntry != null) {
        // Existing entry
        dbEntry.setSubscription(subscription);
      } else {
        // New entry
        dbEntry = new JdoStoredSubscription(subscription);
        persistenceManager.makePersistent(dbEntry);
      }
    } finally {
      persistenceManager.close();
    }
  }

  public void removeSubscription(StoredSubscription subscription) {
    JdoStoredSubscription dbEntry = getStoredSubscription(subscription.getId());
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
  public List<StoredSubscription> listSubscriptions() {
    // Copy the results into a db-detached list.
    List<StoredSubscription> list = Lists.newArrayList();
    PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
    try {
      Query listQuery = persistenceManager.newQuery(JdoStoredSubscription.class);
      Iterable<JdoStoredSubscription> resultList =
          (Iterable<JdoStoredSubscription>) listQuery.execute();
      for (JdoStoredSubscription dbEntry : resultList) {
        list.add(dbEntry.getSubscription());
      }
    } finally {
      persistenceManager.close();
    }

    return list;
  }

  @SuppressWarnings("unchecked")
  private JdoStoredSubscription getStoredSubscription(String subscriptionID) {
    Iterable<JdoStoredSubscription> results = null;
    PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
    try {
      Query getByIDQuery = persistenceManager.newQuery(JdoStoredSubscription.class);
      getByIDQuery.setFilter("subscriptionId == idParam");
      getByIDQuery.declareParameters("String idParam");
      getByIDQuery.setRange(0, 1);
      results = (Iterable<JdoStoredSubscription>) getByIDQuery.execute(subscriptionID);
      // return the first result
      for (JdoStoredSubscription dbEntry : results) {
        return dbEntry;
      }
      return null;
    } finally {
      persistenceManager.close();
    }
  }

  public StoredSubscription getSubscription(String subscriptionID) {
    JdoStoredSubscription dbEntry = getStoredSubscription(subscriptionID);
    return dbEntry == null ? null : dbEntry.getSubscription();
  }
}
