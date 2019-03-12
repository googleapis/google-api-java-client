# Google APIs Client Library for Java

- [Library Maintenance](#maintenance)
- [Overview](#Overview)
- [Highlighted Features](#Highlighted_Features)
- [Dependencies](#Dependencies)
- [Important Warnings](#Beta)
  - [@Beta](#Beta)
  - [Deprecations](#Deprecations)
- [Documentation](#Documentation)
- [Links](#Links)

## Library Maintenance
These client libraries are officially supported by Google. However, these libraries are considered complete and are in maintenance mode. This means that we will address critical bugs and security issues, but will not add any new features. If you're working with **Google Cloud Platform** APIs such as Datastore, Pub/Sub and many others,
consider using the [Cloud Client Libraries for Java](https://github.com/GoogleCloudPlatform/google-cloud-java)
instead. These are the new and idiomatic Java libraries targeted specifically at Google Cloud
Platform Services.

## Building locally

##### One time setup

```
mkdir /tmp/foo && cd /tmp/foo
wget https://dl.google.com/dl/android/maven2/com/google/android/gms/play-services-basement/8.3.0/play-services-basement-8.3.0.aar
unzip play-services-basement-8.3.0.aar
mvn install:install-file \
  -Dfile=classes.jar \
  -DgroupId=com.google.android.google-play-services \
  -DartifactId=google-play-services \
  -Dversion=1 \
  -Dpackaging=jar
cd -

# we need the google-http-java-client jar cached locally
git clone https://github.com/google/google-http-java-client.git
cd google-http-java-client && mvn compile && mvn install && cd ..

# we need the google-oauth-java-client jar cached locally
git clone https://github.com/google/google-oauth-java-client.git
cd google-oauth-java-client && mvn compile && mvn install
```

##### Building And Testing

```
mvn install
```

## Overview

The Google APIs Client Library for Java is a flexible, efficient, and powerful Java client library
for accessing any HTTP-based API on the web, not just Google APIs.

The library has the following features:
 - A powerful [OAuth 2.0](https://developers.google.com/api-client-library/java/google-api-java-client/oauth2) library with a consistent interface.
 - Lightweight, efficient XML and JSON data models that support any data schema.
 - Support for [protocol buffers](https://github.com/google/protobuf/).
 - A set of [generated libraries for Google APIs](https://developers.google.com/api-client-library/java/apis/).

### Accessing Google APIs

To use Google's Java client libraries to call any Google API, you need two libraries:

- The core Google APIs Client Library for Java (google-api-java-client), which is the generic
runtime library described here. This library provides functionality common to all APIs, for example
HTTP transport, error handling, authentication, JSON parsing, media download/upload, and batching.
- An auto-generated Java library for the API you are accessing, for example
the [generated Java library for the BigQuery API](https://github.com/google/google-api-java-client-samples/tree/master/bigquery-appengine-sample/src/main/java/com/google/api/client/sample/bigquery/appengine/dashboard).
These generated libraries include API-specific information such as the root URL, and classes that
represent entities in the context of the API. These classes are useful for making conversions
between JSON objects and Java objects.

To find the generated library for a Google API, visit [Google APIs Client Library for Java](https://developers.google.com/api-client-library/java/apis/).
The API-specific Java packages include both the core google-api-java-client and the client-specific
libraries.

If you are using the old GData library, you need to
[migrate](https://github.com/google/gdata-java-client/blob/wiki/MigratingToGoogleApiJavaClient.md).

### Developing for Android

If you are developing for Android and the Google API you want to use is included in the
[Google Play Services library](https://developer.android.com/google/play-services/index.html), use that library for the best performance and experience.

To access other Google APIs, use the Google APIs Client Library for Java, which supports
[Android 4.0 (Ice Cream Sandwich) (or higher)](https://developers.google.com/api-client-library/java/google-api-java-client/android).

### Other Java environments

- Java 6 (or higher)
- Android 1.6 (or higher)
- [Google App Engine](https://developers.google.com/api-client-library/java/google-api-java-client/app-engine)

## Highlighted Features

- **The library makes it simple to call Google APIs.**

You can call Google APIs using Google service-specific generated libraries with the Google APIs
Client Library for Java. Here's an example that makes a call to the
[Google Calendar API](https://developers.google.com/google-apps/calendar/):

```java
// Show events on user's calendar.
View.header("Show Calendars");
CalendarList feed = client.calendarList().list().execute();
View.display(feed);
```

- **The library makes authentication easier.**

The authentication library can reduce the amount of code needed to handle
[OAuth 2.0](https://developers.google.com/api-client-library/java/google-api-java-client/oauth2),
and sometimes a few lines is all you need. For example:

```java
/** Authorizes the installed application to access user's protected data. */
private static Credential authorize() throws Exception {
  // load client secrets
  GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
      new InputStreamReader(CalendarSample.class.getResourceAsStream("/client_secrets.json")));
  // set up authorization code flow
  GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, JSON_FACTORY, clientSecrets,
      Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory)
      .build();
  // authorize
  return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
}
```

- **The library makes batching and media upload/download easier.**

The library offers helper classes for
[batching](https://developers.google.com/api-client-library/java/google-api-java-client/batch),
[media upload](https://developers.google.com/api-client-library/java/google-api-java-client/media-upload),
and [media download](https://developers.google.com/api-client-library/java/google-api-java-client/media-download).

- **The library runs on Google App Engine.**

[App Engine-specific helpers](https://developers.google.com/api-client-library/java/google-api-java-client/app-engine)
make quick work of authenticated calls to APIs, and you do not need to worry about exchanging code for tokens.
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

- **The library runs on [Android (@Beta)](#@Beta).**

If you are developing for Android and the Google API you want to use is included in the
[Google Play Services library](https://developer.android.com/google/play-services/index.html),
use that library for the best performance and experience.

To access other Google APIs, use the Google Client Library for Java's Android-specific
helper classes, which are well-integrated with
[Android AccountManager](http://developer.android.com/reference/android/accounts/AccountManager.html).
For example:

```java
@Override
public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  // Google Accounts
  credential =
      GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
  SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
  credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
  // Tasks client
  service =
      new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential)
          .setApplicationName("Google-TasksAndroidSample/1.0").build();
}
```

- **The library is easy to install.**

The Google APIs Client Library for Java is easy to install, and you can download the binary
directly from the [Downloads page](https://developers.google.com/api-client-library/java/google-api-java-client/download),
or you can use Maven or Gradle.

To use Maven, add the following lines to your pom.xml file:
[//]: # ({x-version-update-start:google-api-client:released})

  ```maven
  <project>
    <dependencies>
      <dependency>
        <groupId>com.google.api-client</groupId>
        <artifactId>google-api-client</artifactId>
        <version>1.28.0</version>
      </dependency>
    </dependencies>
  </project>
  ```

To use Gradle, add the following lines to your build.gradle file:

```gradle
repositories {
    mavenCentral()
}
dependencies {
    compile 'com.google.api-client:google-api-client:1.28.0'
}
```
[//]: # ({x-version-update-end})

## CI Status

Java Version | Status
------------ | ------
Java 7 | [![Kokoro CI](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-api-java-client/java7.svg)](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-api-java-client/java7.html)
Java 8 | [![Kokoro CI](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-api-java-client/java8.svg)](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-api-java-client/java8.html)
Java 11 | [![Kokoro CI](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-api-java-client/java11.svg)](https://storage.googleapis.com/cloud-devrel-public/java/badges/google-api-java-client/java11.html)

## Dependencies
This library is built on top of two common libraries, also built by Google, and also designed to
work with any HTTP service on the web:

- [Google HTTP Client Library for Java](https://github.com/google/google-http-java-client)
- [Google OAuth Client Library for Java](https://github.com/google/google-oauth-java-client)

## Important Warnings

### @Beta

Features marked with the @Beta annotation at the class or method level are subject to change. They
might be modified in any way, or even removed, in any major release. You should not use beta features
if your code is a library itself (that is, if your code is used on the CLASSPATH of users outside
your own control).

### Deprecations

Deprecated non-beta features will be removed eighteen months after the release in which they are
first deprecated. You must fix your usages before this time. If you don't, any type of breakage
might result, and you are not guaranteed a compilation error.

## Documentation

- [Developer's Guide](https://developers.google.com/api-client-library/java/google-api-java-client/dev-guide)
- [Libraries and Samples](https://developers.google.com/api-client-library/java/apis/)
- [JavaDoc](https://developers.google.com/api-client-library/java/google-api-java-client/reference/index)
- [Get Help](https://developers.google.com/api-client-library/java/google-api-java-client/support)

## Links

- [API Announcements](http://googledevelopers.blogspot.com/)
- [Discuss](https://groups.google.com/forum/#!forum/google-api-java-client)

## Notice: Ending Java 6 Support

Please note: since Java 6 extended support is being ended this December by Oracle, we will begin
ending Java 6 support in early 2019, with release 1.28.0 as a tentative goal. Users may still
use these libraries in Java 6 projects for some time, but going forward we will not ensure that
these libraries work in such an environment. After 1.28.0, our supported versions will include Java
7 and onward.

For Android users, we will continue our 4.0 support.

For questions or concerns, please file an issue in the GitHub repository.
