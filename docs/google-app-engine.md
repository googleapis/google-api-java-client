---
title: Running on Google App Engine
---

# Running on Google App Engine

App Engine-specific helpers make quick work of authenticated calls to APIs, and
you do not need to worry about exchanging code for tokens.

For example:

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
  AppIdentityCredential credential =
      new AppIdentityCredential(Arrays.asList(UrlshortenerScopes.URLSHORTENER));
  Urlshortener shortener =
      new Urlshortener.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
      .build();
  UrlHistory history = shortener.URL().list().execute();
  ...
}
```

## Auth helpers

If you are building a web app that interacts with a user's data via an OAuth 
2.0-enabled API, we've created some helpers to assist you with the process. The
helpers aim to:

* Simplify the process of obtaining access tokens
  ([`AuthorizationCodeFlow`][authorization-code-flow]).
* Manage tokens, after they are obtained, by marking them as
  [`PersistenceCapable`][persistence-capable].
* Simplify the process of making authenticated calls using the access token's
  [credential][credential].
* Insulate you from the details of authentication when writing servlets.

## Getting started

1. Install the Google API Client Library for Java:
   * Follow the [download instructions][setup] and put the library jar files
     into your war/WEB-INF/lib directory.
   * Alternatively, you can use [Maven][setup].
1. Learn about using [OAuth 2.0 with the authorization code flow for Google App Engine applications][oauth2].
1. Learn about using [OAuth 2.0 with the Google App Engine Identity API][oauth2-gae].
1. Take a look at the [Calendar App Engine sample][calendar-sample]. This sample
   combines our Java library and auth helpers to show you how to access end-user
   data from within a Google App Engine web app. The sample also uses [GWT][gwt]
   for the user interface.

[authorization-code-flow]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html
[persistence-capable]: https://cloud.google.com/appengine/docs/java/datastore/
[credential]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/Credential.html
[setup]: https://github.com/googleapis/google-api-java-client/wiki/Setup
[oauth2]: https://github.com/googleapis/google-api-java-client/wiki/OAuth2
[oauth2-gae]: https://github.com/googleapis/google-api-java-client/wiki/OAuth2#gae
[calendar-sample]: https://github.com/google/google-api-java-client-samples/tree/master/calendar-appengine-sample
[gwt]: http://www.gwtproject.org/gle-http-java-client/http-transport.html