/*
 * Copyright (c) 2014 Google Inc.
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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Beta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.util.Locale;

/**
 * {@link Beta} <br/>
 * Provides a default credential available from the host or from an environment variable.
 *
 * <p>An instance represents the per-process state used to get and cache the credential and
 * allows overriding the state and environment for testing purposes.
 */
@Beta
class DefaultCredentialProvider {

  static final String CREDENTIAL_ENV_VAR = "GOOGLE_CREDENTIALS_DEFAULT";

  static final String WELL_KNOWN_CREDENTIALS_FILE = "credentials_default.json";

  static final String CLOUDSDK_CONFIG_DIRECTORY = "gcloud";

  static final String HELP_PERMALINK =
      "https://developers.google.com/accounts/docs/default-credentials";

  static final String APP_ENGINE_CREDENTIAL_CLASS =
      "com.google.api.client.googleapis.extensions.appengine.auth.oauth2"
      + ".AppIdentityCredential$AppEngineCredentialWrapper";

  // These variables should only be accessed inside a synchronized block
  private GoogleCredential cachedCredential = null;
  private boolean checkedAppEngine = false;
  private boolean checkedComputeEngine = false;

  DefaultCredentialProvider() {}

  /**
   * {@link Beta} <br/>
   * Returns a default credential for the application.
   *
   * <p>Returns the built-in service account's credential for the application if running on
   * Google App Engine or Google Compute Engine, or returns the credential pointed to by the
   * environment variable GOOGLE_CREDENTIALS_DEFAULT.
   * </p>
   *
   * @param transport the transport for Http calls.
   * @param jsonFactory the factory for Json parsing and formatting.
   * @return the credential instance.
   * @throws IOException if the credential cannot be created in the current environment.   *
   */
  final GoogleCredential getDefaultCredential(HttpTransport transport, JsonFactory jsonFactory)
      throws IOException {
    synchronized (this) {
      if (cachedCredential == null) {
        cachedCredential = getDefaultCredentialUnsynchronized(transport, jsonFactory);
      }
      if (cachedCredential != null) {
        return cachedCredential;
      }
    }

    throw new IOException(String.format(
        "The Default Credentials are not available. They are available if running"
            + " in Google App Engine or Google Compute Engine. They are also available if using"
            + " the Google Cloud SDK and running 'gcloud auth login'. Otherwise, the environment"
            + " variable %s must be defined pointing to a file defining the credentials."
            + " See %s for details.",
        CREDENTIAL_ENV_VAR,
        HELP_PERMALINK));
  }

  private final GoogleCredential getDefaultCredentialUnsynchronized(
      HttpTransport transport, JsonFactory jsonFactory) throws IOException {

    // First try the environment variable
    GoogleCredential credential = null;
    String credentialsPath = getEnv(CREDENTIAL_ENV_VAR);
    if (credentialsPath != null && credentialsPath.length() > 0) {
      InputStream credentialsStream = null;
      try {
        File credentialsFile = new File(credentialsPath);
        if (!credentialsFile.exists() || credentialsFile.isDirectory()) {
          // Path will get in the message from the catch block below
          throw new IOException("File does not exist.");
        }
        credentialsStream = new FileInputStream(credentialsFile);
        credential = GoogleCredential.fromStream(credentialsStream, transport, jsonFactory);
      } catch (IOException e) {
        // Although it is also the cause, the message of the caught exception can have very
        // important information for diagnosing errors, so include its message in the
        // outer exception message also
        throw OAuth2Utils.exceptionWithCause(new IOException(String.format(
            "Error reading credential file from environment variable %s, value '%s': %s",
            CREDENTIAL_ENV_VAR, credentialsPath, e.getMessage())), e);
      } catch (AccessControlException expected) {
        // Exception querying file system is expected on App-Engine
      } finally {
        if (credentialsStream != null) {
          credentialsStream.close();
        }
      }
    }

    // Then try the well-known file
    File wellKnownFileLocation = getWellKnownCredentialsFile();
    try {
      if (fileExists(wellKnownFileLocation)) {
        InputStream credentialsStream = null;
        try {
          credentialsStream = new FileInputStream(wellKnownFileLocation);
          credential = GoogleCredential.fromStream(credentialsStream, transport, jsonFactory);
        } catch (IOException e) {
          throw new IOException(String.format(
              "Error reading credential file from location %s: %s",
              wellKnownFileLocation, e.getMessage()));
        } finally {
          if (credentialsStream != null) {
            credentialsStream.close();
          }
        }
      }
    } catch (AccessControlException expected) {
      // Exception querying file system is expected on App-Engine
    }

    // Then try App Engine
    if (credential == null) {
      credential = tryGetAppEngineCredential(transport, jsonFactory);
    }

    // Then try Compute Engine
    if (credential == null) {
      credential = tryGetComputeCredential(transport, jsonFactory);
    }
    return credential;
  }

  private final File getWellKnownCredentialsFile() {
    File cloudConfigPath = null;
    String os = getProperty("os.name", "").toLowerCase(Locale.US);
    if (os.indexOf("windows") >= 0) {
      File appDataPath = new File(getEnv("APPDATA"));
      cloudConfigPath = new File(appDataPath, CLOUDSDK_CONFIG_DIRECTORY);
    } else {
      File configPath = new File(getProperty("user.home", ""), ".config");
      cloudConfigPath = new File(configPath, CLOUDSDK_CONFIG_DIRECTORY);
    }
    File credentialFilePath = new File(cloudConfigPath, WELL_KNOWN_CREDENTIALS_FILE);
    return credentialFilePath;
  }

  /**
   * Override in test code to isolate from environment.
   */
  String getEnv(String name) {
    return System.getenv(name);
  }

  /**
   * Override in test code to isolate from environment.
   */
  boolean fileExists(File file) {
    return file.exists() && !file.isDirectory();
  }

  /**
   * Override in test code to isolate from environment.
   */
  String getProperty(String property, String def) {
    return System.getProperty(property, def);
  }

  /**
   * Override in test code to isolate from environment.
   */
  Class<?> forName(String className) throws ClassNotFoundException {
    return Class.forName(className);
  }

  private final GoogleCredential tryGetAppEngineCredential(
      HttpTransport transport, JsonFactory jsonFactory) {
    // Checking for App Engine requires a class load, so check only once
    if (checkedAppEngine) {
      return null;
    }
    checkedAppEngine = true;

    try {
      Class<?> credentialClass = forName(APP_ENGINE_CREDENTIAL_CLASS);
      Constructor<?> constructor = credentialClass
          .getConstructor(HttpTransport.class, JsonFactory.class);
      return (GoogleCredential) constructor.newInstance(transport, jsonFactory);
    } catch (ReflectiveOperationException expected) {
      // Expected when not running on App Engine
    }
    return null;
  }


  private final GoogleCredential tryGetComputeCredential(
      HttpTransport transport, JsonFactory jsonFactory) {
    // Checking compute engine requires a round-trip, so check only once
    if (checkedComputeEngine) {
      return null;
    }
    checkedComputeEngine = true;

    try {
      // To determine if we are running in compute engine, at least one call to the metadata
      // server is required. Attempt to refresh a token which will answer whether we
      // are on compute engine and if so, use the credential as the default.
      GoogleCredential computeCredential = new ComputeGoogleCredential(transport, jsonFactory);
      if (computeCredential.refreshToken()) {
        return computeCredential;
      }
    } catch (IOException expected) {
      // Exception is expected when not running on Google Compute Engine.
    }
    return null;
  }

  private static class ComputeGoogleCredential extends GoogleCredential {

    /** Metadata Service Account token server encoded URL. */
    private static final String TOKEN_SERVER_ENCODED_URL =
        "http://metadata/computeMetadata/v1/instance/service-accounts/default/token";

    ComputeGoogleCredential(HttpTransport transport, JsonFactory jsonFactory) {
      super(new GoogleCredential.Builder()
          .setTransport(transport)
          .setJsonFactory(jsonFactory)
          .setTokenServerEncodedUrl(TOKEN_SERVER_ENCODED_URL));
    }

    @Override
    protected TokenResponse executeRefreshToken() throws IOException {
      GenericUrl tokenUrl = new GenericUrl(getTokenServerEncodedUrl());
      HttpRequest request = getTransport().createRequestFactory().buildGetRequest(tokenUrl);
      JsonObjectParser parser = new JsonObjectParser(getJsonFactory());
      request.setParser(parser);
      request.getHeaders().set("X-Google-Metadata-Request", true);
      HttpResponse response = request.execute();
      InputStream content = response.getContent();
      if (content == null) {
        // Throw explicitly rather than allow a later null reference as default mock
        // transports return success codes with empty contents.
        throw new IOException("Empty content from metadata token server request.");
      }
      return parser.parseAndClose(content, response.getContentCharset(), TokenResponse.class);
    }
  }
}
