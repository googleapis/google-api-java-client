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
consider using the [Cloud Client Libraries for Java](https://github.com/googleapis/google-cloud-java)
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

- Java 7 (or higher)
- Android 1.6 (or higher)
- [Google App Engine](https://developers.google.com/api-client-library/java/google-api-java-client/app-engine)

## Highlighted Features

### Simple to call Google APIs

You can call Google APIs using Google service-specific generated libraries with the Google APIs
Client Library for Java. Here's an example that makes a call to the
[Google Calendar API](https://developers.google.com/google-apps/calendar/):

```java
// Show events on user's calendar.
View.header("Show Calendars");
CalendarList feed = client.calendarList().list().execute();
View.display(feed);
```

### Authentication

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

### Batching

Each HTTP connection that your client makes results in overhead. To reduce overhead, you can batch multiple API calls
together into a single HTTP request.

The main classes of interest are [BatchRequest][batch-request] and [JsonBatchCallback][json-batch-callback]. The
following example shows how to use these classes with service-specific generated libraries:

```java
JsonBatchCallback<Calendar> callback = new JsonBatchCallback<Calendar>() {

  public void onSuccess(Calendar calendar, HttpHeaders responseHeaders) {
    printCalendar(calendar);
    addedCalendarsUsingBatch.add(calendar);
  }

  public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
    System.out.println("Error Message: " + e.getMessage());
  }
};

...

Calendar client = Calendar.builder(transport, jsonFactory, credential)
  .setApplicationName("BatchExample/1.0").build();
BatchRequest batch = client.batch();

Calendar entry1 = new Calendar().setSummary("Calendar for Testing 1");
client.calendars().insert(entry1).queue(batch, callback);

Calendar entry2 = new Calendar().setSummary("Calendar for Testing 2");
client.calendars().insert(entry2).queue(batch, callback);

batch.execute();
```

### Media upload

#### Resumable media upload

When you upload a large media file to a server, use resumable media upload to send the file chunk by chunk. The Google 
API generated libraries contain convenience methods for interacting with resumable media upload, which was introduced in 
the 1.7.0-beta version of the Google API Client Library for Java.

The resumable media upload protocol is similar to the resumable media upload protocol described in the [Google Drive 
API documentation][google-drive-documentation].

#### Protocol design

The following sequence diagram shows how the resumable media upload protocol works:
![Resumable Media Upload Protocol Diagram][resumable-media-upload-protocol-diagram]

#### Implementation details

The main classes of interest are [MediaHttpUploader][media-http-uploader] and 
[MediaHttpProgressListener][media-http-progress-listener].

If methods in the service-specific generated libraries contain the `mediaUpload` parameter in the Discovery document, 
then a convenience method is created for these methods that takes an [InputStreamContent][input-stream-content] as a 
parameter. (For more about using media upload with the Google APIs Discovery Service, see [Media upload][media-upload].)

For example, the `insert` method of the Drive API supports `mediaUpload`, and you can use the following code to upload a 
file:

```java
class CustomProgressListener implements MediaHttpUploaderProgressListener {
  public void progressChanged(MediaHttpUploader uploader) throws IOException {
    switch (uploader.getUploadState()) {
      case INITIATION_STARTED:
        System.out.println("Initiation has started!");
        break;
      case INITIATION_COMPLETE:
        System.out.println("Initiation is complete!");
        break;
      case MEDIA_IN_PROGRESS:
        System.out.println(uploader.getProgress());
        break;
      case MEDIA_COMPLETE:
        System.out.println("Upload is complete!");
    }
  }
}

File mediaFile = new File("/tmp/driveFile.jpg");
InputStreamContent mediaContent =
    new InputStreamContent("image/jpeg",
        new BufferedInputStream(new FileInputStream(mediaFile)));
mediaContent.setLength(mediaFile.length());

Drive.Files.Insert request = drive.files().insert(fileMetadata, mediaContent);
request.getMediaHttpUploader().setProgressListener(new CustomProgressListener());
request.execute();
```

You can also use the resumable media upload feature without the service-specific generated libraries. Here is an 
example:

```java
File mediaFile = new File("/tmp/Test.jpg");
InputStreamContent mediaContent =
    new InputStreamContent("image/jpeg",
        new BufferedInputStream(new FileInputStream(mediaFile)));
mediaContent.setLength(mediaFile.length());


MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, transport, httpRequestInitializer);
uploader.setProgressListener(new CustomProgressListener());
HttpResponse response = uploader.upload(requestUrl);
if (!response.isSuccessStatusCode()) {
  throw GoogleJsonResponseException(jsonFactory, response);
}
```

#### Direct media upload
Resumable media upload is enabled by default, but you can disable it and use direct media upload instead, for example if 
you are uploading a small file. Direct media upload was introduced in the 1.9.0-beta version of the Google API Client 
Library for Java.

Direct media upload uploads the whole file in one HTTP request, as opposed to the resumable media upload protocol, which
uploads the file in multiple requests. Doing a direct upload reduces the number of HTTP requests but increases the 
chance of failures (such as connection failures) that can happen with large uploads.

The usage for direct media upload is the same as what is described above for resumable media upload, plus the following 
call that tells [MediaHttpUploader][media-http-uploader] to only do direct uploads:

```java
mediaHttpUploader.setDirectUploadEnabled(true);
```

### Media download

#### Resumable media downloads

When you download a large media file from a server, use resumable media download to download the file chunk by chunk. 
The Google API generated libraries contain convenience methods for interacting with resumable media download, which was
introduced in the 1.9.0-beta version of the Google API Client Library for Java.
    
The resumable media download protocol is similar to the resumable media upload protocol, which is described in the 
[Google Drive API documentation][google-drive-documentation].

#### Implementation details
The main classes of interest are [MediaHttpDownloader][media-http-downloader] and 
[MediaHttpDownloaderProgressListener][media-http-downloader-progress-listener]. Media content is downloaded in chunks, 
and chunk size is configurable. If a server error is encountered in a request, then the request is retried.

If methods in the service-specific generated libraries support download in the Discovery document, then a convenient 
download method is created for these methods that takes in an [OutputStream][output-stream]. (For more about using media
download with the Google APIs Discovery Service, see [Media download][media-download].)

For example:

```java
class CustomProgressListener implements MediaHttpDownloaderProgressListener {
  public void progressChanged(MediaHttpDownloader downloader) {
    switch (downloader.getDownloadState()) {
      case MEDIA_IN_PROGRESS:
        System.out.println(downloader.getProgress());
        break;
      case MEDIA_COMPLETE:
        System.out.println("Download is complete!");
    }
  }
}

OutputStream out = new FileOutputStream("/tmp/driveFile.jpg");

DriveFiles.Get request = drive.files().get(fileId);
request.getMediaHttpDownloader().setProgressListener(new CustomProgressListener());
request.executeMediaAndDownloadTo(out);
```

You can also use this feature without service-specific generated libraries. Here is an example:

```java
OutputStream out = new FileOutputStream("/tmp/Test.jpg");

MediaHttpDownloader downloader = new MediaHttpDownloader(transport, httpRequestInitializer);
downloader.setProgressListener(new CustomProgressListener());
downloader.download(requestUrl, out);
```

#### Direct media download

Resumable media download is enabled by default, but you can disable it and use direct media download instead, for 
example if you are downloading a small file. Direct media download was introduced in the 1.9.0-beta version of the 
Google API Client Library for Java.

Direct media download downloads the whole media content in one HTTP request, as opposed to the resumable media download 
protocol, which can download in multiple requests. Doing a direct download reduces the number of HTTP requests but 
increases the chance of failures (such as connection failures) that can happen with large downloads.

The usage is the same as what is described above, plus the following call that tells 
[MediaHttpDownloader][media-http-downloader] to do direct downloads:

```java
mediaHttpDownloader.setDirectDownloadEnabled(true);
```

### Running on Google App Engine

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

### Running on [Android (@Beta)](#@Beta)

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

### Installation

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
        <version>1.30.1</version>
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
    compile 'com.google.api-client:google-api-client:1.30.1'
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

- [Google HTTP Client Library for Java](https://github.com/googleapis/google-http-java-client)
- [Google OAuth Client Library for Java](https://github.com/googleapis/google-oauth-java-client)

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
- [JavaDoc](https://googleapis.dev/java/google-api-client/latest/)
- [Get Help](https://developers.google.com/api-client-library/java/google-api-java-client/support)

## Links

- [Discuss](https://groups.google.com/forum/#!forum/google-api-java-client)

For questions or concerns, please file an issue in the GitHub repository.

[batch-request]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/batch/BatchRequest.html
[json-batch-callback]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/batch/json/JsonBatchCallback.html
[google-drive-documentation]: https://developers.google.com/drive/web/manage-uploads#resumable
[media-http-uploader]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpUploader.html
[media-http-progress-listener]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpUploaderProgressListener.html
[input-stream-content]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/http/InputStreamContent.html
[media-upload]: https://developers.google.com/discovery/v1/using#discovery-doc-methods-mediaupload
[resumable-media-upload-protocol-diagram]: ./Resumable-Media-Upload-Sequence-Diagram.png
[media-http-downloader]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpDownloader.html
[media-http-downloader-progress-listener]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/media/MediaHttpDownloaderProgressListener.html
[output-stream]: https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
[media-download]: https://developers.google.com/discovery/v1/using#discovery-doc-methods-mediadownload