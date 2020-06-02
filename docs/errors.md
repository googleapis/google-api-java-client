---
title: Timeout and Errors
---

# Timeout and Errors

## Setting timeouts

In the following example, which uses the
[Google Analytics API][google-analytics-api], the `setConnectTimeout` and
`setReadTimeout` methods are used to set the connect and read timeouts to three
minutes (in milliseconds) for all requests:

```java
private HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
  return new HttpRequestInitializer() {
    @Override
    public void initialize(HttpRequest httpRequest) throws IOException {
    requestInitializer.initialize(httpRequest);
    httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
    httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
  }
};

GoogleCredential credential = ....

final Analytics analytics = Analytics.builder(new NetHttpTransport(), jsonFactory, setHttpTimeout(credential)).build();
```

## Handling HTTP error responses from Google APIs

When an error status code is detected in an HTTP response to a Google API that
uses the JSON format, the generated libraries throw a 
[`GoogleJsonResponseException`][google-json-response-exception].

The following example shows one way that you can handle these exceptions:

```java
Drive.Files.List listFiles = drive.files.list();
try {
  FileList response = listFiles.execute();
  ...
} catch (GoogleJsonResponseException e) {
  System.err.println(e.getDetails());
}
```

[google-analytics-api]: https://developers.google.com/analytics/
[google-json-response-exception]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/json/GoogleJsonResponseException.html
