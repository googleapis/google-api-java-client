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

package com.google.api.client.googleapis.extensions.android.gms.auth;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.common.base.Preconditions;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

/**
 * Manages authorization and account selection for Google accounts.
 *
 * <p>
 * When fetching a token, any thrown {@link GoogleAuthException} would be wrapped:
 * <ul>
 * <li>{@link GooglePlayServicesAvailabilityException} would be wrapped inside of
 * {@link GooglePlayServicesAvailabilityIOException}</li>
 * <li>{@link UserRecoverableAuthException} would be wrapped inside of
 * {@link UserRecoverableAuthIOException}</li>
 * <li>{@link GoogleAuthException} when be wrapped inside of {@link GoogleAuthIOException}</li>
 * </ul>
 * </p>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public class GoogleAccountCredential implements HttpRequestInitializer {

  /** Context. */
  final Context context;

  /** Scope to use on {@link GoogleAuthUtil#getToken}. */
  final String scope;

  /** Google account manager. */
  private final GoogleAccountManager accountManager;

  /**
   * Selected Google account name (e-mail address), for example {@code "johndoe@gmail.com"}, or
   * {@code null} for none.
   */
  private String accountName;

  /** Selected Google account or {@code null} for none. */
  private Account selectedAccount;

  /**
   * @param context context
   * @param scope scope to use on {@link GoogleAuthUtil#getToken}
   */
  public GoogleAccountCredential(Context context, String scope) {
    accountManager = new GoogleAccountManager(context);
    this.context = context;
    this.scope = scope;
  }

  /**
   * Constructor a new instance using OAuth 2.0 scopes.
   *
   * @param context context
   * @param scope first OAuth 2.0 scope
   * @param extraScopes any additional OAuth 2.0 scopes
   * @return new instance
   */
  public static GoogleAccountCredential usingOAuth2(
      Context context, String scope, String... extraScopes) {
    StringBuilder scopeBuilder = new StringBuilder("oauth2:").append(scope);
    for (String extraScope : extraScopes) {
      scopeBuilder.append(' ').append(extraScope);
    }
    return new GoogleAccountCredential(context, scopeBuilder.toString());
  }

  /**
   * Sets the audience scope to use with Google Cloud Endpoints.
   *
   * @param context context
   * @param audience audience
   * @return new instance
   */
  public static GoogleAccountCredential usingAudience(Context context, String audience) {
    Preconditions.checkArgument(audience.length() != 0);
    return new GoogleAccountCredential(context, "audience:" + audience);
  }

  /**
   * Sets the selected Google account name (e-mail address) -- for example
   * {@code "johndoe@gmail.com"} -- or {@code null} for none.
   */
  public final GoogleAccountCredential setSelectedAccountName(String accountName) {
    selectedAccount = accountManager.getAccountByName(accountName);
    // check if account has been deleted
    this.accountName = selectedAccount == null ? null : accountName;
    return this;
  }

  public void initialize(HttpRequest request) {
    RequestHandler handler = new RequestHandler();
    request.setInterceptor(handler);
    request.setUnsuccessfulResponseHandler(handler);
  }

  /** Returns the context. */
  public final Context getContext() {
    return context;
  }

  /** Returns the scope to use on {@link GoogleAuthUtil#getToken}. */
  public final String getScope() {
    return scope;
  }

  /** Returns the Google account manager. */
  public final GoogleAccountManager getGoogleAccountManager() {
    return accountManager;
  }

  /** Returns all Google accounts or {@code null} for none. */
  public final Account[] getAllAccounts() {
    return accountManager.getAccounts();
  }

  /** Returns the selected Google account or {@code null} for none. */
  public final Account getSelectedAccount() {
    return selectedAccount;
  }

  /**
   * Returns the selected Google account name (e-mail address), for example
   * {@code "johndoe@gmail.com"}, or {@code null} for none.
   */
  public final String getSelectedAccountName() {
    return accountName;
  }

  /**
   * Returns an intent to show the user to select a Google account, or create a new one if there are
   * none on the device yet.
   *
   * <p>
   * Must be run from the main UI thread.
   * </p>
   */
  public final Intent newChooseAccountIntent() {
    return AccountPicker.newChooseAccountIntent(selectedAccount,
        null,
        new String[] {GoogleAccountManager.ACCOUNT_TYPE},
        true,
        null,
        null,
        null,
        null);
  }

  /**
   * Returns an OAuth 2.0 access token.
   *
   * <p>
   * Must be run from a background thread, not the main UI thread.
   * </p>
   */
  public final String getToken() throws IOException, GoogleAuthException {
    BackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    while (true) {
      try {
        return GoogleAuthUtil.getToken(context, accountName, scope);
      } catch (IOException e) {
        // network or server error, so retry using exponential backoff
        long backOffMillis = backOffPolicy.getNextBackOffMillis();
        if (backOffMillis == BackOffPolicy.STOP) {
          throw e;
        }
        // sleep
        try {
          Thread.sleep(backOffMillis);
        } catch (InterruptedException e2) {
          // ignore
        }
      }
    }
  }

  class RequestHandler implements HttpExecuteInterceptor, HttpUnsuccessfulResponseHandler {

    /** Whether we've received a 401 error code indicating the token is invalid. */
    boolean received401;
    String token;

    public void intercept(HttpRequest request) throws IOException {
      try {
        token = getToken();
        request.getHeaders().setAuthorization("Bearer " + token);
      } catch (GooglePlayServicesAvailabilityException e) {
        throw new GooglePlayServicesAvailabilityIOException(e);
      } catch (UserRecoverableAuthException e) {
        throw new UserRecoverableAuthIOException(e);
      } catch (GoogleAuthException e) {
        throw new GoogleAuthIOException(e);
      }
    }

    public boolean handleResponse(
        HttpRequest request, HttpResponse response, boolean supportsRetry) {
      if (response.getStatusCode() == 401 && !received401) {
        received401 = true;
        GoogleAuthUtil.invalidateToken(context, token);
        return true;
      }
      return false;
    }
  }
}
