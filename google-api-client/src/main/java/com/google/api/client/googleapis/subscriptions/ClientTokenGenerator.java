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

package com.google.api.client.googleapis.subscriptions;

import java.util.Random;

/**
 * Generates Client-Tokens which are used by the {@link SubscriptionManager} to verify the authority
 * of the origin of a received notifications.
 *
 * <p>
 * Should be thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    SubscriptionManager pm = service.getSubscriptionManager();
    pm.setClientTokenGenerator(ClientTokengenerator.DEFAULT_RANDOM_GENERATOR);
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public interface ClientTokenGenerator {

  /**
   * Generates and returns a new client-token.
   */
  String generateToken() throws Exception;

  /**
   * Default implementation of the ClientTokenGenerator making use of the {@link Random} class.
   *
   * <p>
   * This generator will generate hex-encoded tokens with a minimum length of 32 characters.
   * </p>
   */
  public static final ClientTokenGenerator DEFAULT_RANDOM_GENERATOR = new ClientTokenGenerator() {
    private final Random generator = new Random();

    public synchronized String generateToken() {
      StringBuilder str = new StringBuilder();
      while (str.length() < 32) {
        int num = generator.nextInt();
        str.append(Integer.toHexString(num));
      }
      return str.toString();
    }};
}
