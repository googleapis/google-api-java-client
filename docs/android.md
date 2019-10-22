---
title: Android
---

# Running on [Android (@Beta)](#@Beta)

If you are developing for Android and the Google API you want to use is included
in the [Google Play Services library][play-services], use that library for the
best performance and experience.

To access other Google APIs, use the Google Client Library for Java's 
Android-specific helper classes, which are well-integrated with
[Android AccountManager][account-manager].

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

## Getting started

Begin by reading the [Android development instructions][http-client-android] for
the Google HTTP Client Library for Java.

## Authentication

As described in the [Android development instructions][http-client-android], the
best practice on Android is to use the [`AccountManager`][account-manager] class
(`@Beta`) for centralized identity management and credential token storage.

For information about the OAuth 2.0 flow, see the
[OAuth 2.0 instructions for Android][oauth2-android].

## Partial response and update

Google APIs support a partial-response protocol that allows you to specify which
fields are returned to you in the HTTP response. This can significantly reduce
the size of the response, thereby reducing network usage, parsing response time,
and memory usage. It works with both JSON and XML.

The following snippet of code drawn from the Google+ Sample demonstrates how to
use the partial-response protocol:


```java
Plus.Activities.List listActivities = plus.activities().list("me", "public");
listActivities.setMaxResults(5L);
// Pro tip: Use partial responses to improve response time considerably
listActivities.setFields("nextPageToken,items(id,URL,object/content)");
ActivityFeed feed = listActivities.execute();
```

[play-services]: https://developer.android.com/google/play-services/index.html
[account-manager]: http://developer.android.com/reference/android/accounts/AccountManager.html
[http-client-android]: https://github.com/googleapis/google-http-java-client/wiki/Android
[oauth2-android]: https://github.com/googleapis/google-api-java-client#oauth2-android
