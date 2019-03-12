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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.testing.auth.oauth2.MockTokenServerTransport;
import com.google.api.client.googleapis.testing.compute.MockMetadataServerTransport;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import junit.framework.TestCase;

/**
 * Tests {@link DefaultCredentialProvider}.
 *
 */
public class DefaultCredentialProviderTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private static final Collection<String> SCOPES =
      Collections.unmodifiableCollection(Arrays.asList("scope1", "scope2"));

  private static final String SA_KEY_ID = "key_id";
  private static final String SA_KEY_TEXT = "-----BEGIN PRIVATE KEY-----\n"
      + "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALX0PQoe1igW12i"
      + "kv1bN/r9lN749y2ijmbc/mFHPyS3hNTyOCjDvBbXYbDhQJzWVUikh4mvGBA07qTj79Xc3yBDfKP2IeyYQIFe0t0"
      + "zkd7R9Zdn98Y2rIQC47aAbDfubtkU1U72t4zL11kHvoa0/RuFZjncvlr42X7be7lYh4p3NAgMBAAECgYASk5wDw"
      + "4Az2ZkmeuN6Fk/y9H+Lcb2pskJIXjrL533vrDWGOC48LrsThMQPv8cxBky8HFSEklPpkfTF95tpD43iVwJRB/Gr"
      + "CtGTw65IfJ4/tI09h6zGc4yqvIo1cHX/LQ+SxKLGyir/dQM925rGt/VojxY5ryJR7GLbCzxPnJm/oQJBANwOCO6"
      + "D2hy1LQYJhXh7O+RLtA/tSnT1xyMQsGT+uUCMiKS2bSKx2wxo9k7h3OegNJIu1q6nZ6AbxDK8H3+d0dUCQQDTrP"
      + "SXagBxzp8PecbaCHjzNRSQE2in81qYnrAFNB4o3DpHyMMY6s5ALLeHKscEWnqP8Ur6X4PvzZecCWU9BKAZAkAut"
      + "LPknAuxSCsUOvUfS1i87ex77Ot+w6POp34pEX+UWb+u5iFn2cQacDTHLV1LtE80L8jVLSbrbrlH43H0DjU5AkEA"
      + "gidhycxS86dxpEljnOMCw8CKoUBd5I880IUahEiUltk7OLJYS/Ts1wbn3kPOVX3wyJs8WBDtBkFrDHW2ezth2QJ"
      + "ADj3e1YhMVdjJW5jqwlD/VNddGjgzyunmiZg0uOXsHXbytYmsA545S8KRQFaJKFXYYFo2kOjqOiC1T2cAzMDjCQ"
      + "==\n-----END PRIVATE KEY-----\n";
  private static final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";

  private static final String GAE_SIGNAL_CLASS = "com.google.appengine.api.utils.SystemProperty";

  private static final Lock lock = new ReentrantLock();

  private static File tempDirectory = null;

  public void testDefaultCredentialAppEngineDeployed() throws IOException  {
    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.addType(DefaultCredentialProvider.APP_ENGINE_CREDENTIAL_CLASS,
        MockAppEngineCredential.class);
    testProvider.addType(GAE_SIGNAL_CLASS, MockAppEngineSystemProperty.class);

    Credential defaultCredential = testProvider.getDefaultCredential(transport, JSON_FACTORY);

    assertNotNull(defaultCredential);
    assertTrue(defaultCredential instanceof MockAppEngineCredential);
    assertSame(transport, defaultCredential.getTransport());
    assertSame(JSON_FACTORY, defaultCredential.getJsonFactory());
  }

  public void testDefaultCredentialAppEngineComponentOffAppEngineGivesNotFoundError() {
    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.addType(DefaultCredentialProvider.APP_ENGINE_CREDENTIAL_CLASS,
        MockAppEngineCredential.class);
    testProvider.addType(GAE_SIGNAL_CLASS, MockOffAppEngineSystemProperty.class);

    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("No credential expected when not on App Engine.");
    } catch (IOException e) {
      String message = e.getMessage();
      assertTrue(message.contains(DefaultCredentialProvider.HELP_PERMALINK));
    }
  }

  public void testDefaultCredentialAppEngineWithoutDependencyThrowsHelpfulLoadError() {
    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    testProvider.addType(GAE_SIGNAL_CLASS, MockAppEngineSystemProperty.class);

    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("Credential expected to fail to load if credential class not present.");
    } catch (IOException e) {
      String message = e.getMessage();
      assertFalse(message.contains(DefaultCredentialProvider.HELP_PERMALINK));
      assertTrue(message.contains(DefaultCredentialProvider.APP_ENGINE_CREDENTIAL_CLASS));
    }
  }

  public void testDefaultCredentialCaches() throws IOException  {
    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.addType(DefaultCredentialProvider.APP_ENGINE_CREDENTIAL_CLASS,
        MockAppEngineCredential.class);
    testProvider.addType(GAE_SIGNAL_CLASS, MockAppEngineSystemProperty.class);

    Credential firstCall = testProvider.getDefaultCredential(transport, JSON_FACTORY);

    assertNotNull(firstCall);

    Credential secondCall = testProvider.getDefaultCredential(transport, JSON_FACTORY);

    assertSame(firstCall, secondCall);
  }

  public void testGetDefaultCredentials_cloudshell() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.setEnv(DefaultCredentialProvider.CLOUD_SHELL_ENV_VAR, "4");

    GoogleCredential defaultCredential = testProvider.getDefaultCredential(transport, JSON_FACTORY);

    assertTrue(defaultCredential instanceof CloudShellCredential);
    assertEquals(((CloudShellCredential) defaultCredential).getAuthPort(), 4);
  }

  public void testGetDefaultCredentials_cloudshell_withComputCredentialsPresent()
      throws IOException {
    MockMetadataServerTransport transport = new MockMetadataServerTransport(ACCESS_TOKEN);
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.setEnv(DefaultCredentialProvider.CLOUD_SHELL_ENV_VAR, "4");

    GoogleCredential defaultCredential = testProvider.getDefaultCredential(transport, JSON_FACTORY);

    assertTrue(defaultCredential instanceof CloudShellCredential);
    assertEquals(((CloudShellCredential) defaultCredential).getAuthPort(), 4);
  }

  public void testDefaultCredentialCompute() throws IOException {
    HttpTransport transport = new MockMetadataServerTransport(ACCESS_TOKEN);
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    Credential defaultCredential = testProvider.getDefaultCredential(transport, JSON_FACTORY);
    assertNotNull(defaultCredential);

    assertTrue(defaultCredential.refreshToken());
    assertEquals(ACCESS_TOKEN, defaultCredential.getAccessToken());
  }

  public void testDefaultCredentialComputeErrorNotFound() throws IOException {
    MockMetadataServerTransport transport = new MockMetadataServerTransport(ACCESS_TOKEN);
    transport.setTokenRequestStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    Credential defaultCredential = testProvider.getDefaultCredential(transport, JSON_FACTORY);
    assertNotNull(defaultCredential);

    try {
      defaultCredential.refreshToken();
      fail("Expected error refreshing token.");
    } catch (IOException expected) {
      String message = expected.getMessage();
      assertTrue(message.contains(Integer.toString(HttpStatusCodes.STATUS_CODE_NOT_FOUND)));
      assertTrue(message.contains("scope"));
    }
  }

  public void testDefaultCredentialComputeErrorUnexpected() throws IOException {
    MockMetadataServerTransport transport = new MockMetadataServerTransport(ACCESS_TOKEN);
    transport.setTokenRequestStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    Credential defaultCredential = testProvider.getDefaultCredential(transport, JSON_FACTORY);
    assertNotNull(defaultCredential);

    try {
      defaultCredential.refreshToken();
      fail("Expected error refreshing token.");
    } catch (IOException expected) {
      String message = expected.getMessage();
      assertTrue(message.contains(Integer.toString(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)));
      assertTrue(message.contains("Unexpected"));
    }
  }

  public void testDefaultCredentialComputeCachesFailureAfterFixedNumberOfRetries() {
    MockRequestUrlRecordingTransport transport = new MockRequestUrlRecordingTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    // First attempt to get credentials we retry.
    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("No credential expected for default test provider.");
    } catch (IOException expected) {
    }
    assertEquals(3, transport.getRequestCount());

    // Second attempt we get back the cached result.
    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("No credential expected for default test provider.");
    } catch (IOException expected) {
    }
    assertEquals(3, transport.getRequestCount());
  }

  public void testDefaultCredentialNoGceCheck() throws IOException {
    MockRequestUrlRecordingTransport transport = new MockRequestUrlRecordingTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.setEnv("NO_GCE_CHECK", "True");

    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("No credential expected for default test provider.");
    } catch (IOException expected) {
    }
    assertEquals(0, transport.getRequestCount());
  }

  public void testDefaultCredentialWithCustomMetadataServerAddress() throws IOException {
    MockRequestUrlRecordingTransport transport = new MockRequestUrlRecordingTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.setEnv("GCE_METADATA_HOST", "test.metadata.server.address");

    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("No credential expected for default test provider.");
    } catch (IOException expected) {
    }
    assertTrue(transport.urlWasRequested("http://test.metadata.server.address"));
  }

  public void testDefaultCredentialNonExistentFileThrows() throws Exception {
    File nonExistentFile = new java.io.File(getTempDirectory(), "DefaultCredentialBadFile.json");
    assertFalse(nonExistentFile.exists());

    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.setEnv(DefaultCredentialProvider.CREDENTIAL_ENV_VAR,
        nonExistentFile.getAbsolutePath());
    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail("Non existent user credential should throw exception.");
    } catch (IOException e) {
      String message = e.getMessage();
      assertTrue(message.contains(DefaultCredentialProvider.CREDENTIAL_ENV_VAR));
      assertTrue(message.contains(nonExistentFile.getAbsolutePath()));
    }
  }

  public void testDefaultCredentialNotFoundError() {
    HttpTransport transport = new MockHttpTransport();
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    try {
      testProvider.getDefaultCredential(transport, JSON_FACTORY);
      fail();
    } catch (IOException e) {
      String message = e.getMessage();
      assertTrue(message.contains(DefaultCredentialProvider.HELP_PERMALINK));
    }
  }

  public void testDefaultCredentialServiceAccount() throws IOException {
    File serviceAccountFile = new java.io.File(getTempDirectory(),
        "DefaultCredentialServiceAccount.json");
    if (serviceAccountFile.exists()) {
      serviceAccountFile.delete();
    }
    final String serviceAccountId =
        "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String serviceAccountEmail =
        "36680232662-vrd7ji19qe3nelgchdcsanun6bnr@developer.gserviceaccount.com";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, ACCESS_TOKEN);

    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    try {
      // Write out service account file
      GenericJson serviceAccountContents = new GenericJson();
      serviceAccountContents.setFactory(JSON_FACTORY);
      serviceAccountContents.put("client_id", serviceAccountId);
      serviceAccountContents.put("client_email", serviceAccountEmail);
      serviceAccountContents.put("private_key", SA_KEY_TEXT);
      serviceAccountContents.put("private_key_id", SA_KEY_ID);
      serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
      PrintWriter writer = new PrintWriter(serviceAccountFile);
      String json = serviceAccountContents.toPrettyString();
      writer.println(json);
      writer.close();

      // Point the default credential to the file
      testProvider.setEnv(DefaultCredentialProvider.CREDENTIAL_ENV_VAR,
          serviceAccountFile.getAbsolutePath());

      GoogleCredential credential = testProvider.getDefaultCredential(transport, JSON_FACTORY);
      assertNotNull(credential);
      credential = credential.createScoped(SCOPES);

      assertTrue(credential.refreshToken());
      assertEquals(ACCESS_TOKEN, credential.getAccessToken());
    } finally {
      if (serviceAccountFile.exists()) {
        serviceAccountFile.delete();
      }
    }
  }

  public void testDefaultCredentialUser() throws IOException {
    File userCredentialFile = new java.io.File(getTempDirectory(), "DefaultCredentialUser.json");
    if (userCredentialFile.exists()) {
      userCredentialFile.delete();
    }

    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    // Point the default credential to the file
    testProvider.setEnv(DefaultCredentialProvider.CREDENTIAL_ENV_VAR,
        userCredentialFile.getAbsolutePath());

    testDefaultCredentialUser(userCredentialFile, testProvider);
  }

  public void testDefaultCredentialWellKnownFileNonWindows() throws IOException {
    // Simulate where the SDK puts the well-known file on non-Windows platforms
    File homeDir = getTempDirectory();
    File configDir = new File(homeDir, ".config");
    if (!configDir.exists()) {
      configDir.mkdir();
    }
    File cloudConfigDir = new File(configDir, DefaultCredentialProvider.CLOUDSDK_CONFIG_DIRECTORY);
    if (!cloudConfigDir.exists()) {
      cloudConfigDir.mkdir();
    }
    File wellKnownFile = new File(
        cloudConfigDir, DefaultCredentialProvider.WELL_KNOWN_CREDENTIALS_FILE);
    if (wellKnownFile.exists()) {
      wellKnownFile.delete();
    }
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.addFile(wellKnownFile.getAbsolutePath());
    testProvider.setProperty("os.name", "linux");
    testProvider.setProperty("user.home", homeDir.getAbsolutePath());

    testDefaultCredentialUser(wellKnownFile, testProvider);
  }

  public void testDefaultCredentialWellKnownFileWindows() throws IOException {
    // Simulate where the SDK puts the well-known file on Windows
    File appDataDir = getTempDirectory();
    File cloudConfigDir = new File(appDataDir, DefaultCredentialProvider.CLOUDSDK_CONFIG_DIRECTORY);
    if (!cloudConfigDir.exists()) {
      cloudConfigDir.mkdir();
    }
    File wellKnownFile = new File(
        cloudConfigDir, DefaultCredentialProvider.WELL_KNOWN_CREDENTIALS_FILE);
    if (wellKnownFile.exists()) {
      wellKnownFile.delete();
    }
    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();
    testProvider.addFile(wellKnownFile.getAbsolutePath());
    testProvider.setProperty("os.name", "windows");
    testProvider.setEnv("APPDATA", appDataDir.getAbsolutePath());

    testDefaultCredentialUser(wellKnownFile, testProvider);
  }

  public void testDefaultCredentialEnvironmentVariableWinsOverWellKnownFile() throws IOException {
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxH8cruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshTokenEnv = "2/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";
    final String accessTokenEnv = "2/MkSJoj1xsli0AccessToken_NKPY2";
    final String refreshTokenWkf = "3/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";
    final String accessTokenWkf = "3/MkSJoj1xsli0AccessToken_NKPY2";

    TestDefaultCredentialProvider testProvider = new TestDefaultCredentialProvider();

    // Set up an environment variable file
    File environmentVariableFile = new java.io.File(getTempDirectory(), "EnvVarUser.json");
    if (environmentVariableFile.exists()) {
      environmentVariableFile.delete();
    }
    testProvider.setEnv(DefaultCredentialProvider.CREDENTIAL_ENV_VAR,
        environmentVariableFile.getAbsolutePath());

    // Also set up a well-known-location file
    File homeDir = getTempDirectory();
    File configDir = new File(homeDir, ".config");
    if (!configDir.exists()) {
      configDir.mkdir();
    }
    File cloudConfigDir = new File(configDir, DefaultCredentialProvider.CLOUDSDK_CONFIG_DIRECTORY);
    if (!cloudConfigDir.exists()) {
      cloudConfigDir.mkdir();
    }
    File wellKnownFile = new File(
        cloudConfigDir, DefaultCredentialProvider.WELL_KNOWN_CREDENTIALS_FILE);
    if (wellKnownFile.exists()) {
      wellKnownFile.delete();
    }
    testProvider.addFile(wellKnownFile.getAbsolutePath());
    testProvider.setProperty("os.name", "linux");
    testProvider.setProperty("user.home", homeDir.getAbsolutePath());

    // Define a transport that can simulate refreshing tokens
    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshTokenEnv, accessTokenEnv);
    transport.addRefreshToken(refreshTokenWkf, accessTokenWkf);

    String jsonEnv = GoogleCredentialTest.createUserJson(clientId, clientSecret, refreshTokenEnv);
    String jsonWkf = GoogleCredentialTest.createUserJson(clientId, clientSecret, refreshTokenWkf);

    try {
      // Write out user files
      PrintWriter writer = new PrintWriter(environmentVariableFile);
      writer.println(jsonEnv);
      writer.close();
      writer = new PrintWriter(wellKnownFile);
      writer.println(jsonWkf);
      writer.close();

      Credential credential = testProvider.getDefaultCredential(transport, JSON_FACTORY);

      assertNotNull(credential);
      assertEquals(refreshTokenEnv, credential.getRefreshToken());
      assertTrue(credential.refreshToken());
      assertEquals(accessTokenEnv, credential.getAccessToken());
    } finally {
      if (wellKnownFile.exists()) {
        wellKnownFile.delete();
      }
      if (environmentVariableFile.exists()) {
        environmentVariableFile.delete();
      }
    }
  }

  private void testDefaultCredentialUser(File userFile, TestDefaultCredentialProvider testProvider)
      throws IOException {
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxH8cruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    // Define a transport that can simulate refreshing tokens
    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, ACCESS_TOKEN);

    String json = GoogleCredentialTest.createUserJson(clientId, clientSecret, refreshToken);

    try {
      // Write out user file
      PrintWriter writer = new PrintWriter(userFile);
      writer.println(json);
      writer.close();

      Credential credential = testProvider.getDefaultCredential(transport, JSON_FACTORY);

      assertNotNull(credential);
      assertEquals(refreshToken, credential.getRefreshToken());

      assertTrue(credential.refreshToken());
      assertEquals(ACCESS_TOKEN, credential.getAccessToken());
    } finally {
      if (userFile.exists()) {
        userFile.delete();
      }
    }
  }

  private static File getTempDirectory() {
    lock.lock();
    try {
      if (tempDirectory == null) {
        String userHome = System.getProperty("user.home");
        File temp = new java.io.File(userHome, ".temptest");
        if (!temp.exists()) {
          temp.mkdir();
        } else if (!temp.isDirectory()) {
          fail("Temp directory is a file!");
        }
        tempDirectory = temp;
      }
    } finally {
      lock.unlock();
    }
    return tempDirectory;
  }

  public static class MockAppEngineCredential extends GoogleCredential {
    public MockAppEngineCredential(HttpTransport transport, JsonFactory jsonFactory) {
      super(new GoogleCredential.Builder().setTransport(transport).setJsonFactory(jsonFactory));
    }
  }

  /*
   * App Engine is detected by calling SystemProperty.environment.value() via Reflection.
   * The following mock types simulate the shape and behavior of that call sequence.
   */

  private static class MockAppEngineSystemProperty {

    @SuppressWarnings("unused")
    public static final MockEnvironment environment =
      new MockEnvironment(MockEnvironmentEnum.Production);
  }

  private static class MockOffAppEngineSystemProperty {

    @SuppressWarnings("unused")
    public static final MockEnvironment environment = new MockEnvironment(null);
  }

  private enum MockEnvironmentEnum {
    Production,
    Development;
  }

  public static class MockEnvironment {

    private MockEnvironmentEnum innerValue;

    MockEnvironment(MockEnvironmentEnum value) {
      this.innerValue = value;
    }

    public MockEnvironmentEnum value() {
      return innerValue;
    }
  }

  /*
   * End of types simulating SystemProperty.environment.value()
   */

  private static class MockRequestUrlRecordingTransport extends MockHttpTransport {
    List<String> requestUrls = new ArrayList<String>();

    MockRequestUrlRecordingTransport() {
    }

    int getRequestCount() {
      return requestUrls.size();
    }

    boolean urlWasRequested(String url) {
      return requestUrls.contains(url);
    }

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      MockLowLevelHttpRequest request = new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() throws IOException {
          requestUrls.add(getUrl());
          throw new IOException("MockRequestCountingTransport request failed.");
        }
      };
      return request;
    }
  }

  private static class TestDefaultCredentialProvider extends DefaultCredentialProvider  {

    private Map<String, Class<?>> types = new HashMap<String, Class<?>>();
    private Map<String, String> variables = new HashMap<String, String>();
    private Map<String, String> properties = new HashMap<String, String>();
    private Set<String> files = new HashSet<String>();
    private int forNameCallCount = 0;

    TestDefaultCredentialProvider() {
    }

    void addFile(String file) {
      files.add(file);
    }

    void addType(String className, Class<?> type) {
      types.put(className, type);
    }

    @Override
    String getEnv(String name) {
      return variables.get(name);
    }

    @Override
    boolean getEnvEquals(String name, String value) {
      return variables.containsKey(name) && variables.get(name).equals(value);
    }

    void setEnv(String name, String value) {
      variables.put(name, value);
    }

    @Override
    String getProperty(String property, String def) {
      String value = properties.get(property);
      return value == null ? def : value;
    }

    void setProperty(String property, String value) {
      properties.put(property, value);
    }

    @Override
    boolean fileExists(File file) {
      return files.contains(file.getAbsolutePath());
    }

    @Override
    Class<?> forName(String className) throws ClassNotFoundException {
      forNameCallCount++;
      Class<?> lookup = types.get(className);
      if (lookup != null) {
        return lookup;
      }
      throw new ClassNotFoundException("TestDefaultCredentialProvider: Class not found.");
    }

    int getForNameCallCount() {
      return forNameCallCount;
    }
  }
}
