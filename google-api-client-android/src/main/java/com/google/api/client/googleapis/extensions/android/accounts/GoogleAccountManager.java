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

package com.google.api.client.googleapis.extensions.android.accounts;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * {@link Beta} <br/>
 * Account manager wrapper for Google accounts.
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
@Beta
public final class GoogleAccountManager {

  /** Google account type. */
  public static final String ACCOUNT_TYPE = "com.google";

  /** Account manager. */
  private final AccountManager manager;

  /**
   * @param accountManager account manager
   */
  public GoogleAccountManager(AccountManager accountManager) {
    this.manager = Preconditions.checkNotNull(accountManager);
  }

  /**
   * @param context context from which to retrieve the account manager
   */
  public GoogleAccountManager(Context context) {
    this(AccountManager.get(context));
  }

  /**
   * Returns the account manager.
   *
   * @since 1.8
   */
  public AccountManager getAccountManager() {
    return manager;
  }

  /**
   * Returns all Google accounts.
   *
   * @return array of Google accounts
   */
  public Account[] getAccounts() {
    return manager.getAccountsByType("com.google");
  }

  /**
   * Returns the Google account of the given {@link Account#name}.
   *
   * @param accountName Google account name or {@code null} for {@code null} result
   * @return Google account or {@code null} for none found or for {@code null} input
   */
  public Account getAccountByName(String accountName) {
    if (accountName != null) {
      for (Account account : getAccounts()) {
        if (accountName.equals(account.name)) {
          return account;
        }
      }
    }
    return null;
  }

  /**
   * Invalidates the given Google auth token by removing it from the account manager's cache (if
   * necessary) for example if the auth token has expired or otherwise become invalid.
   *
   * @param authToken auth token
   */
  public void invalidateAuthToken(String authToken) {
    manager.invalidateAuthToken(ACCOUNT_TYPE, authToken);
  }
}
