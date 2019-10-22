---
title: Overview
---

# Overview

## Description

The Google APIs Client Library for Java is a flexible, efficient, and powerful
Java client library for accessing any HTTP-based API on the web, not just Google
APIs.

The library has the following features:

- A powerful [OAuth 2.0][oauth2] library with a consistent interface.
- Lightweight, efficient XML and JSON data models that support any data schema.
- Support for [protocol buffers][protobuf].
- A set of [generated libraries for Google APIs][service-clients].

The library supports the following Java environments:

- Java 7 (or higher)
- Android 1.6 (or higher)
- [Google App Engine][app-engine]

This library is built on top of two common libraries, also built by Google, and
also designed to work with any HTTP service on the web:

- [Google HTTP Client Library for Java][http-client]
- [Google OAuth Client Library for Java][oauth-client]

This is an open-source library, and [contributions][contributions] are welcome.

## Accessing Google APIs

To use Google's Java client libraries to call any Google API, you need two 
libraries:

- The core Google APIs Client Library for Java (`google-api-java-client`), which
  is the generic runtime library described here. This library provides
  functionality common to all APIs, for example HTTP transport, error handling,
  authentication, JSON parsing, media download/upload, and batching.
- An auto-generated Java library for the API you are accessing, for example the 
  [generated Java library for the BigQuery API][bigquery]. These generated
  libraries include API-specific information such as the root URL, and classes 
  that represent entities in the context of the API. These classes are useful
  for making conversions between JSON objects and Java objects.

To find the generated library for a Google API, visit
[Google APIs Client Library for Java][service-list].
The API-specific Java packages include both the core google-api-java-client and 
the client-specific libraries.

## Beta Features

Features marked with the `@Beta` annotation at the class or method level are
subject to change. They might be modified in any way, or even removed, in any
major release. You should not use beta features if your code is a library itself
(that is, if your code is used on the `CLASSPATH` of users outside your own
control).

## Deprecated Features

Deprecated non-beta features will be removed eighteen months after the release
in which they are first deprecated. You must fix your usages before this time.
If you don't, any type of breakage might result, and you are not guaranteed a
compilation error.

[oauth2]: https://googleapis.github.io/google-api-java-client/oauth-2.0.html
[protobuf]: https://github.com/google/protobuf/
[service-clients]: https://github.com/googleapis/google-api-java-client-services/
[app-engine]: https://googleapis.github.io/google-api-java-client/google-app-engine.html
[http-client]: https://github.com/googleapis/google-http-java-client
[oauth-client]: https://github.com/googleapis/google-oauth-java-client
[contributions]: https://github.com/googleapis/google-api-java-client/wiki/CONTRIBUTING.md
[bigquery]: https://github.com/google/google-api-java-client-samples/tree/master/bigquery-appengine-sample/src/main/java/com/google/api/client/sample/bigquery/appengine/dashboard
[service-list]: https://github.com/googleapis/google-api-java-client-services#supported-google-apis
