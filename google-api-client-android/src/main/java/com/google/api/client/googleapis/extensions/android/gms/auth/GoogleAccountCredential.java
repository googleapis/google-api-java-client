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
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.Beta;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Joiner;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sleeper;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.util.Collection;

/**
 * {@link Beta} <br/>
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
 * <p>
 * Upgrade warning: in prior version 1.14 exponential back-off was enabled by default when I/O
 * exception was thrown inside {@link #getToken}, but starting with version 1.15 you need to call
 * {@link #setBackOff} with {@link ExponentialBackOff} to enable it.
 * </p>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
@Beta
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

  /** Sleeper. */
  private Sleeper sleeper = Sleeper.DEFAULT;

  /**
   * Back-off policy which is used when an I/O exception is thrown inside {@link #getToken} or
   * {@code null} for none.
   */
  private BackOff backOff;

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
   * Constructs a new instance using OAuth 2.0 scopes.
   *
   * @param context context
   * @param scopes non empty OAuth 2.0 scope list
   * @return new instance
   *
   * @since 1.15
   */
  public static GoogleAccountCredential usingOAuth2(Context context, Collection<String> scopes) {
    Preconditions.checkArgument(scopes != null && scopes.iterator().hasNext());
    String scopesStr = "oauth2: " + Joiner.on(' ').join(scopes);
    return new GoogleAccountCredential(context, scopesStr);
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

  /**
   * Sets the selected Google {@link Account} or {@code null} for none.
   *
   * <p>
   * Caller must ensure the given Google account exists.
   * </p>
   */
  public final GoogleAccountCredential setSelectedAccount(Account selectedAccount) {
    this.selectedAccount = selectedAccount;
    this.accountName = selectedAccount == null ? null : selectedAccount.name;
    return this;
  }

  @Override
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
   * Returns the back-off policy which is used when an I/O exception is thrown inside
   * {@link #getToken} or {@code null} for none.
   *
   * @since 1.15
   */
  public BackOff getBackOff() {
    return backOff;
  }

  /**
   * Sets the back-off policy which is used when an I/O exception is thrown inside {@link #getToken}
   * or {@code null} for none.
   *
   * @since 1.15
   */
  public GoogleAccountCredential setBackOff(BackOff backOff) {
    this.backOff = backOff;
    return this;
  }

  /**
   * Returns the sleeper.
   *
   * @since 1.15
   */
  public final Sleeper getSleeper() {
    return sleeper;
  }

  /**
   * Sets the sleeper. The default value is {@link Sleeper#DEFAULT}.
   *
   * @since 1.15
   */
  public final GoogleAccountCredential setSleeper(Sleeper sleeper) {
    this.sleeper = Preconditions.checkNotNull(sleeper);
    return this;
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
  public String getToken() throws IOException, GoogleAuthException {
    if (backOff != null) {
      backOff.reset();
    }

    while (true) {
      try {
        return GoogleAuthUtil.getToken(context, accountName, scope);
      } catch (IOException e) {
        // network or server error, so retry using back-off policy
        try {
          if (backOff == null || !BackOffUtils.next(sleeper, backOff)) {
            throw e;
          }
        } catch (InterruptedException e2) {
          // ignore
        }
      }
    }
  }


  @Beta
  class RequestHandler implements HttpExecuteInterceptor, HttpUnsuccessfulResponseHandler {

    /** Whether we've received a 401 error code indicating the token is invalid. */
    boolean received401;
    String token;

    @Override
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

    @Override
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
