// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.api.client.googleapis.batch;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.api.client.googleapis.batch.BatchRequest.RequestInfo;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.googleapis.testing.services.MockGoogleClient;
import com.google.api.client.googleapis.testing.services.MockGoogleClientRequest;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.protobuf.ProtoObjectParser;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.Key;
import com.google.api.client.util.ObjectParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests {@link BatchRequest}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class BatchRequestTest extends TestCase {

  private static final String ROOT_URL = "http://www.test.com/";
  private static final String SERVICE_PATH = "test/";
  private static final String TEST_BATCH_URL = "http://www.testgoogleapis.com/batch";
  private static final String URI_TEMPLATE1 = "uri/template/1";
  private static final String URI_TEMPLATE2 = "uri/template/2";
  private static final String METHOD1 = HttpMethods.GET;
  private static final String METHOD2 = HttpMethods.POST;
  private static final String ERROR_MSG = "Error message";
  private static final String ERROR_REASON = "notFound";
  private static final int ERROR_CODE = 503;
  private static final String ERROR_DOMAIN = "global";
  private static final String RESPONSE_BOUNDARY = "ABC=DE=F";
  private static final String TEST_ID = "Humpty Dumpty";
  private static final String TEST_KIND = "Big\nEgg\n"; // Newlines help test boundary detection
  private static final String TEST_NAME = "James Bond";
  private static final String TEST_NUM = "007";

  private TestCallback1 callback1;
  private TestCallback2 callback2;
  private TestCallback3 callback3;

  private MockTransport transport;

  private MockCredential credential;

  @Override
  protected void setUp() {
    callback1 = new TestCallback1();
    callback2 = new TestCallback2();
    callback3 = new TestCallback3();
  }

  public static class MockDataClass1 extends GenericJson {
    @Key
    String id;

    @Key
    String kind;
  }

  public static class MockDataClass2 extends GenericJson {
    @Key
    String name;

    @Key
    String number;
  }

  private static class TestCallback1
      implements
        BatchCallback<MockDataClass1, GoogleJsonErrorContainer> {

    int successCalls;

    TestCallback1() {
    }

    @Override
    public void onSuccess(MockDataClass1 dataClass, HttpHeaders responseHeaders) {
      successCalls++;
      assertEquals(TEST_ID, dataClass.id);
      assertEquals(TEST_KIND, dataClass.kind);
    }

    @Override
    public void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders) {
      fail("Should not be invoked in this test");
    }
  }

  private static class TestCallback2
      implements
        BatchCallback<MockDataClass2, GoogleJsonErrorContainer> {

    int successCalls;
    int failureCalls;

    TestCallback2() {
    }

    @Override
    public void onSuccess(MockDataClass2 dataClass, HttpHeaders responseHeaders) {
      successCalls++;
      assertEquals(TEST_NAME, dataClass.name);
      assertEquals(TEST_NUM, dataClass.number);
    }

    @Override
    public void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders) {
      failureCalls++;
      GoogleJsonError error = e.getError();
      ErrorInfo errorInfo = error.getErrors().get(0);
      assertEquals(ERROR_DOMAIN, errorInfo.getDomain());
      assertEquals(ERROR_REASON, errorInfo.getReason());
      assertEquals(ERROR_MSG, errorInfo.getMessage());
      assertEquals(ERROR_CODE, error.getCode());
      assertEquals(ERROR_MSG, error.getMessage());
    }
  }

  private static class TestCallback3 implements BatchCallback<Void, Void> {

    int successCalls;
    int failureCalls;

    TestCallback3() {
    }

    @Override
    public void onSuccess(Void dataClass, HttpHeaders responseHeaders) {
      successCalls++;
      assertNull(dataClass);
    }

    @Override
    public void onFailure(Void e, HttpHeaders responseHeaders) {
      failureCalls++;
      assertNull(e);
    }
  }

  /**
   * Base class for callback adapters to handle error conversion.
   *
   * @param <InputType> The input type
   * @param <OutputType> The output type
   */
  private abstract static class TestCallbackBaseAdapter<InputType, OutputType> implements
      BatchCallback<InputType, ErrorOutput.ErrorBody> {

    protected final BatchCallback<OutputType, GoogleJsonErrorContainer> callback;

    protected TestCallbackBaseAdapter(
        BatchCallback<OutputType, GoogleJsonErrorContainer> callback) {
      this.callback = callback;
    }

    @Override
    public void onFailure(ErrorOutput.ErrorBody e, HttpHeaders responseHeaders) throws IOException {
      GoogleJsonErrorContainer errorContainer = new GoogleJsonErrorContainer();

      if (e.hasError()) {
        ErrorOutput.ErrorProto errorProto = e.getError();

        GoogleJsonError error = new GoogleJsonError();
        if (errorProto.hasCode()) {
          error.setCode(errorProto.getCode());
        }
        if (errorProto.hasMessage()) {
          error.setMessage(errorProto.getMessage());
        }

        List<ErrorInfo> errorInfos = new ArrayList<ErrorInfo>(errorProto.getErrorsCount());
        for (ErrorOutput.IndividualError individualError : errorProto.getErrorsList()) {
          ErrorInfo errorInfo = new ErrorInfo();
          if (individualError.hasDomain()) {
            errorInfo.setDomain(individualError.getDomain());
          }
          if (individualError.hasMessage()) {
            errorInfo.setMessage(individualError.getMessage());
          }
          if (individualError.hasReason()) {
            errorInfo.setReason(individualError.getReason());
          }
          errorInfos.add(errorInfo);
        }
        error.setErrors(errorInfos);
        errorContainer.setError(error);
      }
      callback.onFailure(errorContainer, responseHeaders);
    }
  }

  private static class TestCallback1Adapter extends
      TestCallbackBaseAdapter<MockData.Class1, MockDataClass1> {

    public TestCallback1Adapter(TestCallback1 callback) {
      super(callback);
    }

    @Override
    public void onSuccess(MockData.Class1 message, HttpHeaders responseHeaders) throws IOException {
      MockDataClass1 dataClass = new MockDataClass1();
      dataClass.id = message.hasId() ? message.getId() : null;
      dataClass.kind = message.hasKind() ? message.getKind() : null;
      callback.onSuccess(dataClass, responseHeaders);
    }
  }

  private static class TestCallback2Adapter extends
      TestCallbackBaseAdapter<MockData.Class2, MockDataClass2> {

    public TestCallback2Adapter(TestCallback2 callback) {
      super(callback);
    }

    @Override
    public void onSuccess(MockData.Class2 message, HttpHeaders responseHeaders) throws IOException {
      MockDataClass2 dataClass = new MockDataClass2();
      dataClass.name = message.hasName() ? message.getName() : null;
      dataClass.number = message.hasNumber() ? message.getNumber() : null;
      callback.onSuccess(dataClass, responseHeaders);
    }
  }

  private static class MockUnsuccessfulResponseHandler implements HttpUnsuccessfulResponseHandler {

    MockTransport transport;
    boolean returnSuccessAuthenticatedContent;

    MockUnsuccessfulResponseHandler(
        MockTransport transport, boolean returnSuccessAuthenticatedContent) {
      this.transport = transport;
      this.returnSuccessAuthenticatedContent = returnSuccessAuthenticatedContent;
    }

    @Override
    public boolean handleResponse(
        HttpRequest request, HttpResponse response, boolean supportsRetry) {
      if (transport.returnErrorAuthenticatedContent) {
        // If transport has already been set to return error content do not handle response.
        return false;
      }
      if (returnSuccessAuthenticatedContent) {
        transport.returnSuccessAuthenticatedContent = true;
      } else {
        transport.returnErrorAuthenticatedContent = true;
      }
      return true;
    }

  }

  @Deprecated
  private static class MockExponentialBackOffPolicy extends ExponentialBackOffPolicy {
    public MockExponentialBackOffPolicy() {
    }

    @Override
    public long getNextBackOffMillis() {
      return 0;
    }
  }

  private static class MockTransport extends MockHttpTransport {

    final boolean testServerError;
    final boolean testAuthenticationError;
    boolean returnSuccessAuthenticatedContent;
    boolean returnErrorAuthenticatedContent;
    final boolean testRedirect;
    final boolean testBinary;
    final boolean testMissingLength;
    int actualCalls;
    int callsBeforeSuccess;

    MockTransport(boolean testServerError, boolean testAuthenticationError, boolean testRedirect, boolean testBinary,
        boolean testMissingLength) {
      this.testServerError = testServerError;
      this.testAuthenticationError = testAuthenticationError;
      this.testRedirect = testRedirect;
      this.testBinary = testBinary;
      this.testMissingLength = testMissingLength;
    }

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      actualCalls++;
      return new MockLowLevelHttpRequest() {
          @Override
        public LowLevelHttpResponse execute() throws IOException {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setStatusCode(200);
          response.addHeader("Content-Type", "multipart/mixed; boundary=" + RESPONSE_BOUNDARY);
          String contentType =
              testBinary ? "application/x-protobuf" : "application/json; charset=UTF-8";
          byte[] content1 = testBinary ? MockData.Class1.newBuilder()
              .setId(TEST_ID)
              .setKind(TEST_KIND)
              .build().toByteArray()
              : utf8Encode("{\n \"id\": \"" + TEST_ID + "\",\n \"kind\": \""
                  + TEST_KIND.replace("\n", "\\n") + "\"\n}");
          byte[] content2 = testBinary ? MockData.Class2.newBuilder()
              .setName(TEST_NAME)
              .setNumber(TEST_NUM)
              .build().toByteArray()
              : utf8Encode("{\"name\": \"" + TEST_NAME + "\", \"number\": \"" + TEST_NUM + "\"}");
          byte[] errorContent = testBinary ? ErrorOutput.ErrorBody.newBuilder()
              .setError(ErrorOutput.ErrorProto.newBuilder()
                  .setCode(ERROR_CODE)
                  .setMessage(ERROR_MSG)
                  .addErrors(ErrorOutput.IndividualError.newBuilder()
                      .setDomain(ERROR_DOMAIN)
                      .setReason(ERROR_REASON)
                      .setMessage(ERROR_MSG))
              ).build().toByteArray()
              : utf8Encode("{\"error\": { \"errors\": [{\"domain\": \"" + ERROR_DOMAIN + "\","
                  + "\"reason\": \"" + ERROR_REASON + "\", \"message\": \"" + ERROR_MSG + "\"}],"
                  + "\"code\": " + ERROR_CODE + ", \"message\": \"" + ERROR_MSG + "\"}}");
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          Writer responseContent = new OutputStreamWriter(outputStream, "ISO-8859-1");
          if (returnSuccessAuthenticatedContent || (testRedirect && actualCalls > 1)) {
            if (returnSuccessAuthenticatedContent || actualCalls == callsBeforeSuccess) {
              responseContent.append("--" + RESPONSE_BOUNDARY + "\n")
                  .append("Content-Type: application/http\n")
                  .append("Content-Transfer-Encoding: binary\n")
                  .append("Content-ID: response-1\n\n").append("HTTP/1.1 200 OK\n")
                  .append("Content-Type: " + contentType + "\n");
              if (!testMissingLength) {
                responseContent.append("Content-Length: " + content2.length + "\n");
              }
              responseContent.append("\n");
              responseContent.flush();
              outputStream.write(content2);
              responseContent.append("\n--" + RESPONSE_BOUNDARY + "--\n\n");
            } else {
              responseContent.append("--" + RESPONSE_BOUNDARY + "\n")
                  .append("Content-Type: application/http\n")
                  .append("Content-Transfer-Encoding: binary\n")
                  .append("Content-ID: response-1\n\n")
                  .append("HTTP/1.1 " + ERROR_CODE + " Not Found\n")
                  .append("Content-Type: " + contentType + "\n");
                  if (!testMissingLength) {
                    responseContent.append("Content-Length: " + errorContent.length + "\n");
                  }
                  responseContent.append("\n");
                  responseContent.flush();
                  outputStream.write(errorContent);
                  responseContent.append("\n--" + RESPONSE_BOUNDARY + "--\n\n");
            }
          } else if (returnErrorAuthenticatedContent) {
            responseContent.append("Content-Type: application/http\n")
                .append("Content-Transfer-Encoding: binary\n").append("Content-ID: response-1\n\n");
            responseContent.append("HTTP/1.1 " + ERROR_CODE + " Not Found\n")
                .append("Content-Type: " + contentType + "\n");
            if (!testMissingLength) {
              responseContent.append("Content-Length: " + errorContent.length + "\n");
            }
            responseContent.append("\n");
            responseContent.flush();
            outputStream.write(errorContent);
            responseContent.append("\n--" + RESPONSE_BOUNDARY + "--\n\n");
          } else {
            responseContent.append("--" + RESPONSE_BOUNDARY + "\n")
                .append("Content-Type: application/http\n")
                .append("Content-Transfer-Encoding: binary\n").append("Content-ID: response-1\n\n")
                .append("HTTP/1.1 200 OK\n")
                .append("Content-Type: " + contentType + "\n");
            if (!testMissingLength) {
              responseContent.append("Content-Length: " + content1.length + "\n");
            }
            responseContent.append("\n");
            responseContent.flush();
            outputStream.write(content1);
            responseContent
                .append("\n--" + RESPONSE_BOUNDARY + "\n")
                .append("Content-Type: application/http\n")
                .append("Content-Transfer-Encoding: binary\n")
                .append("Content-ID: response-2\n\n");

            if (testServerError) {
              responseContent.append("HTTP/1.1 " + ERROR_CODE + " Not Found\n")
                  .append("Content-Type: " + contentType + "\n");
              if (!testMissingLength) {
                responseContent.append("Content-Length: " + errorContent.length + "\n");
              }
              responseContent.append("\n");
              responseContent.flush();
              outputStream.write(errorContent);
              responseContent.append("\n--" + RESPONSE_BOUNDARY + "--\n\n");
            } else if (testAuthenticationError) {
              responseContent.append("HTTP/1.1 401 Unauthorized\n")
                  .append("Content-Type: application/json; charset=UTF-8\n\n")
                  .append("--" + RESPONSE_BOUNDARY + "--\n\n");
            } else if (testRedirect && actualCalls == 1) {
              responseContent.append("HTTP/1.1 301 MovedPermanently\n")
                  .append("Content-Type: " + contentType + "\n")
                  .append("Location: http://redirect/location\n\n")
                  .append("--" + RESPONSE_BOUNDARY + "--\n\n");
            } else {
              responseContent.append("HTTP/1.1 200 OK\n")
                  .append("Content-Type: " + contentType + "\n");
                  if (!testMissingLength) {
                    responseContent.append("Content-Length: " + content2.length + "\n");
                  }
                  responseContent.append("\n");
                  responseContent.flush();
                  outputStream.write(content2);
                  responseContent.append("\n--" + RESPONSE_BOUNDARY + "--\n\n");
            }
          }
          responseContent.flush();
          response.setContent(outputStream.toByteArray());
          return response;
        }

        // Short-hand to encode a String as a UTF-8 byte array
        private byte[] utf8Encode(String string) {
          return Charsets.UTF_8.encode(string).array();
        }
      };
    }
  }

  private static class MockCredential implements HttpRequestInitializer, HttpExecuteInterceptor {

    boolean initializerCalled = false;
    boolean interceptorCalled = false;

    MockCredential() {
    }

    @Override
    public void initialize(HttpRequest request) {
      request.setInterceptor(this);
      initializerCalled = true;
    }

    @Override
    public void intercept(HttpRequest request) {
      interceptorCalled = true;
    }

  }

  private BatchRequest getBatchPopulatedWithRequests(boolean testServerError,
      boolean testAuthenticationError,
      boolean returnSuccessAuthenticatedContent,
      boolean testRedirect,
      boolean testBinary,
      boolean testMissingLength) throws Exception {
    transport = new MockTransport(testServerError,
        testAuthenticationError,
        testRedirect,
        testBinary,
        testMissingLength);
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, null, null).setApplicationName("Test Application")
        .build();
    MockGoogleClientRequest<String> jsonHttpRequest1 =
        new MockGoogleClientRequest<String>(client, METHOD1, URI_TEMPLATE1, null, String.class);
    MockGoogleClientRequest<String> jsonHttpRequest2 =
        new MockGoogleClientRequest<String>(client, METHOD2, URI_TEMPLATE2, null, String.class);
    credential = new MockCredential();

    ObjectParser parser =
        testBinary ? new ProtoObjectParser() : new JsonObjectParser(new JacksonFactory());
    BatchRequest batchRequest =
        new BatchRequest(transport, credential).setBatchUrl(new GenericUrl(TEST_BATCH_URL));
    HttpRequest request1 = jsonHttpRequest1.buildHttpRequest();
    request1.setParser(parser);
    HttpRequest request2 = jsonHttpRequest2.buildHttpRequest();
    request2.setParser(parser);
    if (testAuthenticationError) {
      request2.setUnsuccessfulResponseHandler(
          new MockUnsuccessfulResponseHandler(transport, returnSuccessAuthenticatedContent));
    }

    if (testBinary) {
      batchRequest.queue(request1, MockData.Class1.class, ErrorOutput.ErrorBody.class,
          new TestCallback1Adapter(callback1));
      batchRequest.queue(request2, MockData.Class2.class, ErrorOutput.ErrorBody.class,
          new TestCallback2Adapter(callback2));
    } else {
      batchRequest.queue(request1, MockDataClass1.class, GoogleJsonErrorContainer.class, callback1);
      batchRequest.queue(request2, MockDataClass2.class, GoogleJsonErrorContainer.class, callback2);
    }
    return batchRequest;
  }

  public void testQueueDatastructures() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, false, false, false, false, false);
    List<RequestInfo<?, ?>> requestInfos = batchRequest.requestInfos;

    // Assert that the expected objects are queued.
    assertEquals(2, requestInfos.size());
    assertEquals(MockDataClass1.class, requestInfos.get(0).dataClass);
    assertEquals(callback1, requestInfos.get(0).callback);
    assertEquals(MockDataClass2.class, requestInfos.get(1).dataClass);
    assertEquals(callback2, requestInfos.get(1).callback);
    // Assert that the requests in the queue are as expected.
    assertEquals(
        ROOT_URL + SERVICE_PATH + URI_TEMPLATE1, requestInfos.get(0).request.getUrl().build());
    assertEquals(
        ROOT_URL + SERVICE_PATH + URI_TEMPLATE2, requestInfos.get(1).request.getUrl().build());
    assertEquals(METHOD1, requestInfos.get(0).request.getRequestMethod());
    assertEquals(METHOD2, requestInfos.get(1).request.getRequestMethod());
  }

  public void testExecute() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, false, false, false, false, false);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(1, callback2.successCalls);
    assertEquals(0, callback2.failureCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
  }

  public void testExecuteWithError() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(true, false, false, false, false, false);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(0, callback2.successCalls);
    assertEquals(1, callback2.failureCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
    // Assert transport called expected number of times.
    assertEquals(1, transport.actualCalls);
  }

  public void testExecuteWithVoidCallback() throws Exception {
    subTestExecuteWithVoidCallback(false);
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(1, callback3.successCalls);
    assertEquals(0, callback3.failureCalls);
  }

  public void testExecuteWithVoidCallbackError() throws Exception {
    subTestExecuteWithVoidCallback(true);
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(0, callback3.successCalls);
    assertEquals(1, callback3.failureCalls);
  }

  public void subTestExecuteWithVoidCallback(boolean testServerError) throws Exception {
    MockTransport transport = new MockTransport(testServerError, false,false, false, false);
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, null, null).setApplicationName("Test Application")
        .build();
    MockGoogleClientRequest<String> jsonHttpRequest1 =
        new MockGoogleClientRequest<String>(client, METHOD1, URI_TEMPLATE1, null, String.class);
    MockGoogleClientRequest<String> jsonHttpRequest2 =
        new MockGoogleClientRequest<String>(client, METHOD2, URI_TEMPLATE2, null, String.class);
    ObjectParser parser = new JsonObjectParser(new JacksonFactory());
    BatchRequest batchRequest =
        new BatchRequest(transport, null).setBatchUrl(new GenericUrl(TEST_BATCH_URL));
    HttpRequest request1 = jsonHttpRequest1.buildHttpRequest();
    request1.setParser(parser);
    HttpRequest request2 = jsonHttpRequest2.buildHttpRequest();
    request2.setParser(parser);
    batchRequest.queue(request1, MockDataClass1.class, GoogleJsonErrorContainer.class, callback1);
    batchRequest.queue(request2, Void.class, Void.class, callback3);
    batchRequest.execute();
    // Assert transport called expected number of times.
    assertEquals(1, transport.actualCalls);
  }

  public void testExecuteWithAuthenticationErrorThenSuccessCallback() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, true, true, false, false, false);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(1, callback2.successCalls);
    assertEquals(0, callback2.failureCalls);
    // Assert transport called expected number of times.
    assertEquals(2, transport.actualCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
  }

  public void testExecuteWithAuthenticationErrorThenErrorCallback() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, true, false, false, false, false);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(0, callback2.successCalls);
    assertEquals(1, callback2.failureCalls);
    // Assert transport called expected number of times.
    assertEquals(2, transport.actualCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
  }

  public void testInterceptor() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(true, false, false, false, false, false);
    batchRequest.execute();
    // Assert the top-level request initializer is called.
    assertTrue(credential.initializerCalled);
    assertTrue(credential.interceptorCalled);
  }

  public void testRedirect() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, false, false, true, false, false);
    transport.callsBeforeSuccess = 2;
    batchRequest.execute();
    // Assert transport called expected number of times.
    assertEquals(2, transport.actualCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
  }

  public void testExecute_checkWriteTo() throws Exception {
    String request1Method = HttpMethods.POST;
    String request1Url = "http://test/dummy/url1";
    String request1ContentType = "application/json";
    String request1Content = "{\"data\":{\"foo\":{\"v1\":{}}}}";

    String request2Method = HttpMethods.GET;
    String request2Url = "http://test/dummy/url2";

    final StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("--__END_OF_PART__\r\n");
    expectedOutput.append("Content-Length: 118\r\n");
    expectedOutput.append("Content-Type: application/http\r\n");
    expectedOutput.append("content-id: 1\r\n");
    expectedOutput.append("content-transfer-encoding: binary\r\n");
    expectedOutput.append("\r\n");
    expectedOutput.append("POST http://test/dummy/url1 HTTP/1.1\r\n");
    expectedOutput.append("Content-Length: 26\r\n");
    expectedOutput.append("Content-Type: " + request1ContentType + "\r\n");
    expectedOutput.append("\r\n");
    expectedOutput.append(request1Content + "\r\n");
    expectedOutput.append("--__END_OF_PART__\r\n");
    expectedOutput.append("Content-Length: 39\r\n");
    expectedOutput.append("Content-Type: application/http\r\n");
    expectedOutput.append("content-id: 2\r\n");
    expectedOutput.append("content-transfer-encoding: binary\r\n");
    expectedOutput.append("\r\n");
    expectedOutput.append("GET http://test/dummy/url2 HTTP/1.1\r\n");
    expectedOutput.append("\r\n");
    expectedOutput.append("\r\n");
    expectedOutput.append("--__END_OF_PART__--\r\n");
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request1 =
        transport
            .createRequestFactory()
            .buildRequest(
                request1Method,
                new GenericUrl(request1Url),
                new ByteArrayContent(request1ContentType, request1Content.getBytes(UTF_8)));
    HttpRequest request2 = transport.createRequestFactory()
        .buildRequest(request2Method, new GenericUrl(request2Url), null);
    subtestExecute_checkWriteTo(expectedOutput.toString(), request1, request2);
  }

  private void subtestExecute_checkWriteTo(final String expectedOutput, HttpRequest... requests)
      throws IOException {

    MockHttpTransport transport = new MockHttpTransport() {

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) {
        return new MockLowLevelHttpRequest(url) {

          @Override
          public LowLevelHttpResponse execute() throws IOException {
            assertEquals("multipart/mixed; boundary=__END_OF_PART__", getContentType());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            getStreamingContent().writeTo(out);
            assertEquals(expectedOutput, out.toString("UTF-8"));
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setStatusCode(200);
            response.addHeader("Content-Type", "multipart/mixed; boundary=" + RESPONSE_BOUNDARY);
            String content2 = "{\"name\": \"" + TEST_NAME + "\", \"number\": \"" + TEST_NUM + "\"}";
            StringBuilder responseContent = new StringBuilder();
            responseContent.append("--" + RESPONSE_BOUNDARY + "\n")
                .append("Content-Type: application/http\n")
                .append("Content-Transfer-Encoding: binary\n").append("Content-ID: response-1\n\n")
                .append("HTTP/1.1 200 OK\n")
                .append("Content-Type: application/json; charset=UTF-8\n")
                .append("Content-Length: " + content2.length() + "\n\n").append(content2 + "\n\n")
                .append("--" + RESPONSE_BOUNDARY + "--\n\n");
            response.setContent(responseContent.toString());
            return response;
          }
        };
      }
    };

    BatchRequest batchRequest = new BatchRequest(transport, null);
    BatchCallback<Void, Void> callback = new BatchCallback<Void, Void>() {

      @Override
      public void onSuccess(Void t, HttpHeaders responseHeaders) {
      }

      @Override
      public void onFailure(Void e, HttpHeaders responseHeaders) {
      }
    };
    for (HttpRequest request : requests) {
      batchRequest.queue(request, Void.class, Void.class, callback);
    }
    batchRequest.execute();
  }

  public void testExecute_checkWriteToNoHeaders() throws Exception {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request1 = transport.createRequestFactory()
        .buildPostRequest(HttpTesting.SIMPLE_GENERIC_URL, new HttpContent() {

          @Override
          public long getLength() {
            return -1;
          }

          @Override
          public String getType() {
            return null;
          }

          @Override
          public void writeTo(OutputStream out) {
          }

          @Override
          public boolean retrySupported() {
            return true;
          }
        });
    subtestExecute_checkWriteTo(new StringBuilder().append("--__END_OF_PART__\r\n")
        .append("Content-Length: 36\r\n").append("Content-Type: application/http\r\n")
        .append("content-id: 1\r\n").append("content-transfer-encoding: binary\r\n").append("\r\n")
        .append("POST http://google.com/ HTTP/1.1\r\n").append("\r\n").append("\r\n")
        .append("--__END_OF_PART__--\r\n").toString(), request1);
  }

  public void testProtoExecute() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, false, false, false, true, false);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(1, callback2.successCalls);
    assertEquals(0, callback2.failureCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
  }

  public void testProtoExecuteWithError() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(true, false, false, false, true, false);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(0, callback2.successCalls);
    assertEquals(1, callback2.failureCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
    // Assert transport called expected number of times.
    assertEquals(1, transport.actualCalls);
  }

  public void testProtoExecuteWithoutLength() throws Exception {
    BatchRequest batchRequest =
        getBatchPopulatedWithRequests(false, false, false, false, true, true);
    batchRequest.execute();
    // Assert callbacks have been invoked.
    assertEquals(1, callback1.successCalls);
    assertEquals(1, callback2.successCalls);
    assertEquals(0, callback2.failureCalls);
    // Assert requestInfos is empty after execute.
    assertTrue(batchRequest.requestInfos.isEmpty());
  }
}
