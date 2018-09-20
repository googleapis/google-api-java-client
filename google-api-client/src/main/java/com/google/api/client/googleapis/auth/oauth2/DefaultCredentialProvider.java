/*
 * Copyright 2014 Google Inc.
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
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Beta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
class DefaultCredentialProvider extends SystemEnvironmentProvider {

  static final String CREDENTIAL_ENV_VAR = "GOOGLE_APPLICATION_CREDENTIALS";

  static final String WELL_KNOWN_CREDENTIALS_FILE = "application_default_credentials.json";

  static final String CLOUDSDK_CONFIG_DIRECTORY = "gcloud";

  static final String HELP_PERMALINK =
      "https://developers.google.com/accounts/docs/application-default-credentials";

  static final String APP_ENGINE_CREDENTIAL_CLASS =
      "com.google.api.client.googleapis.extensions.appengine.auth.oauth2"
      + ".AppIdentityCredential$AppEngineCredentialWrapper";

  static final String CLOUD_SHELL_ENV_VAR = "DEVSHELL_CLIENT_PORT";

  private static enum Environment {
    UNKNOWN, ENVIRONMENT_VARIABLE, WELL_KNOWN_FILE, CLOUD_SHELL, APP_ENGINE, COMPUTE_ENGINE,
  }

  // These variables should only be accessed inside a synchronized block
  private GoogleCredential cachedCredential = null;
  private Environment detectedEnvironment = null;

  DefaultCredentialProvider() {}

  /**
   * {@link Beta} <br/>
   * Returns the Application Default Credentials.
   *
   * <p>Returns the Application Default Credentials which are credentials that identify and
   * authorize the whole application. This is the built-in service account if running on Google
   * Compute Engine or the credentials file from the path in the environment variable
   * GOOGLE_APPLICATION_CREDENTIALS.</p>
   *
   * @param transport the transport for Http calls.
   * @param jsonFactory the factory for Json parsing and formatting.
   * @return the credential instance.
   * @throws IOException if the credential cannot be created in the current environment.
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
        "The Application Default Credentials are not available. They are available if running "
            + "on Google App Engine, Google Compute Engine, or Google Cloud Shell. Otherwise, "
            + "the environment variable %s must be defined pointing to a file defining "
            + "the credentials. See %s for more information.",
        CREDENTIAL_ENV_VAR,
        HELP_PERMALINK));
  }

  private final GoogleCredential getDefaultCredentialUnsynchronized(
      HttpTransport transport, JsonFactory jsonFactory) throws IOException {
    if (detectedEnvironment == null) {
      detectedEnvironment = detectEnvironment(transport);
    }

    switch (detectedEnvironment) {
      case ENVIRONMENT_VARIABLE:
        return getCredentialUsingEnvironmentVariable(transport, jsonFactory);
      case WELL_KNOWN_FILE:
        return getCredentialUsingWellKnownFile(transport, jsonFactory);
      case APP_ENGINE:
        return getAppEngineCredential(transport, jsonFactory);
      case CLOUD_SHELL:
        return getCloudShellCredential(jsonFactory);
      case COMPUTE_ENGINE:
        return getComputeCredential(transport, jsonFactory);
      default:
        return null;
    }
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

  private final Environment detectEnvironment(HttpTransport transport) throws IOException {
    // First try the environment variable
    if (runningUsingEnvironmentVariable()) {
      return Environment.ENVIRONMENT_VARIABLE;

    // Then try the well-known file
    } else if (runningUsingWellKnownFile()) {
      return Environment.WELL_KNOWN_FILE;

    // Try App Engine
    } else if (useGAEStandardAPI()) {
      return Environment.APP_ENGINE;

    // Then try Cloud Shell.  This must be done BEFORE checking
    // Compute Engine, as Cloud Shell runs on GCE VMs.
    } else if (runningOnCloudShell()) {
      return Environment.CLOUD_SHELL;

    // Then try Compute Engine
    } else if (OAuth2Utils.runningOnComputeEngine(transport, this)) {
      return Environment.COMPUTE_ENGINE;
    }

    return Environment.UNKNOWN;
  }

  private boolean runningUsingEnvironmentVariable() throws IOException {
    String credentialsPath = getEnv(CREDENTIAL_ENV_VAR);
    if (credentialsPath == null || credentialsPath.length() == 0) {
      return false;
    }

    try {
      File credentialsFile = new File(credentialsPath);
      if (!credentialsFile.exists() || credentialsFile.isDirectory()) {
        throw new IOException(
            String.format(
                "Error reading credential file from environment variable %s, value '%s': "
                + "File does not exist.", CREDENTIAL_ENV_VAR, credentialsPath));
      }
      return true;
    } catch (AccessControlException expected) {
      // Exception querying file system is expected on App-Engine
      return false;
    }
  }

  private GoogleCredential getCredentialUsingEnvironmentVariable(
      HttpTransport transport, JsonFactory jsonFactory) throws IOException {
    String credentialsPath = getEnv(CREDENTIAL_ENV_VAR);

    InputStream credentialsStream = null;
    try {
      credentialsStream = new FileInputStream(credentialsPath);
      return GoogleCredential.fromStream(credentialsStream, transport, jsonFactory);
    } catch (IOException e) {
      // Although it is also the cause, the message of the caught exception can have very
      // important information for diagnosing errors, so include its message in the
      // outer exception message also
      throw OAuth2Utils.exceptionWithCause(new IOException(String.format(
          "Error reading credential file from environment variable %s, value '%s': %s",
          CREDENTIAL_ENV_VAR, credentialsPath, e.getMessage())), e);
    } finally {
      if (credentialsStream != null) {
        credentialsStream.close();
      }
    }
  }

  private boolean runningUsingWellKnownFile() {
    File wellKnownFileLocation = getWellKnownCredentialsFile();
    try {
      return fileExists(wellKnownFileLocation);
    } catch (AccessControlException expected) {
      // Exception querying file system is expected on App-Engine
      return false;
    }
  }

  private GoogleCredential getCredentialUsingWellKnownFile(
      HttpTransport transport, JsonFactory jsonFactory) throws IOException {
    File wellKnownFileLocation = getWellKnownCredentialsFile();
    InputStream credentialsStream = null;
    try {
      credentialsStream = new FileInputStream(wellKnownFileLocation);
      return GoogleCredential.fromStream(credentialsStream, transport, jsonFactory);
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

  private boolean useGAEStandardAPI() {
    Class<?> systemPropertyClass = null;
    try {
      systemPropertyClass = forName("com.google.appengine.api.utils.SystemProperty");
    } catch (ClassNotFoundException expected) {
      // SystemProperty will always be present on App Engine.
      return false;
    }
    Exception cause = null;
    Field environmentField;
    try {
      environmentField = systemPropertyClass.getField("environment");
      Object environmentValue = environmentField.get(null);
      Class<?> environmentType = environmentField.getType();
      Method valueMethod = environmentType.getMethod("value");
      Object environmentValueValue = valueMethod.invoke(environmentValue);
      return (environmentValueValue != null);
    } catch (NoSuchFieldException exception) {
      cause = exception;
    } catch (SecurityException exception) {
      cause = exception;
    } catch (IllegalArgumentException exception) {
      cause = exception;
    } catch (IllegalAccessException exception) {
      cause = exception;
    } catch (NoSuchMethodException exception) {
      cause = exception;
    } catch (InvocationTargetException exception) {
      cause = exception;
    }
    throw OAuth2Utils.exceptionWithCause(new RuntimeException(String.format(
        "Unexpcted error trying to determine if runnning on Google App Engine: %s",
        cause.getMessage())), cause);
  }

  private final GoogleCredential getAppEngineCredential(
      HttpTransport transport, JsonFactory jsonFactory) throws IOException {
    Exception innerException = null;
    try {
      Class<?> credentialClass = forName(APP_ENGINE_CREDENTIAL_CLASS);
      Constructor<?> constructor = credentialClass
          .getConstructor(HttpTransport.class, JsonFactory.class);
      return (GoogleCredential) constructor.newInstance(transport, jsonFactory);
    } catch (ClassNotFoundException e) {
      innerException = e;
    } catch (NoSuchMethodException e) {
      innerException = e;
    } catch (InstantiationException e) {
      innerException = e;
    } catch (IllegalAccessException e) {
      innerException = e;
    } catch (InvocationTargetException e) {
      innerException = e;
    }
    throw OAuth2Utils.exceptionWithCause(new IOException(String.format(
        "Application Default Credentials failed to create the Google App Engine service account"
            + " credentials class %s. Check that the component 'google-api-client-appengine' is"
            + " deployed.",
        APP_ENGINE_CREDENTIAL_CLASS)), innerException);
  }

  private boolean runningOnCloudShell() {
    // Check for well known environment variable
    return getEnv(CLOUD_SHELL_ENV_VAR) != null;
  }

  private GoogleCredential getCloudShellCredential(JsonFactory jsonFactory) {
    String port = getEnv(CLOUD_SHELL_ENV_VAR);
    return new CloudShellCredential(Integer.parseInt(port), jsonFactory);
  }

  private final GoogleCredential getComputeCredential(
      HttpTransport transport, JsonFactory jsonFactory) {
    return new ComputeGoogleCredential(transport, jsonFactory);
  }

  private static class ComputeGoogleCredential extends GoogleCredential {

    /** Metadata Service Account token server encoded URL. */
    private static final String TOKEN_SERVER_ENCODED_URL = OAuth2Utils.getMetadataServerUrl()
        + "/computeMetadata/v1/instance/service-accounts/default/token";

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
      request.getHeaders().set("Metadata-Flavor", "Google");
      request.setThrowExceptionOnExecuteError(false);
      HttpResponse response = request.execute();
      int statusCode = response.getStatusCode();
      if (statusCode == HttpStatusCodes.STATUS_CODE_OK) {
        InputStream content = response.getContent();
        if (content == null) {
          // Throw explicitly rather than allow a later null reference as default mock
          // transports return success codes with empty contents.
          throw new IOException("Empty content from metadata token server request.");
        }
        return parser.parseAndClose(content, response.getContentCharset(), TokenResponse.class);
      }
      if (statusCode == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
        throw new IOException(String.format("Error code %s trying to get security access token from"
            + " Compute Engine metadata for the default service account. This may be because"
            + " the virtual machine instance does not have permission scopes specified.",
            statusCode));
      }
      throw new IOException(String.format("Unexpected Error code %s trying to get security access"
          + " token from Compute Engine metadata for the default service account: %s", statusCode,
          response.parseAsString()));
    }
  }
}
