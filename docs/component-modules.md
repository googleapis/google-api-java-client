---
title: Component Modules
---

# Component Modules

This libraries is composed of several modules:

## google-api-client

The Google API Client Library for Java (`google-api-client`) is designed to be
compatible with all supported Java platforms, including Android.

## google-api-client-android

Extensions to the Google API Client Library for Java
(`google-api-client-android`) support Java Google Android (only for SDK >= 2.1)
applications. This module depends on `google-api-client` and
`google-http-client-android`.

## google-api-client-servlet

Servlet and JDO extensions to the Google API Client Library for Java
(`google-api-client-servlet`) support Java servlet web applications. This module
depends on `google-api-client` and `google-oauth-client-servlet`.

## google-api-client-appengine

Google App Engine extensions to the Google API Client Library for Java
(`google-api-client-appengine`) support Java Google App Engine applications.
This module depends on `google-api-client`, `google-api-client-servlet`,
`google-oauth-client-appengine`, and `google-http-client-appengine`.

## google-api-client-gson

GSON extensions to the Google API Client Library for Java
(`google-api-client-gson`). This module depends on `google-api-client` and
`google-http-client-gson`.

## google-api-client-jackson2

Jackson2 extensions to the Google API Client Library for Java
(`google-api-client-jackson2`). This module depends on `google-api-client` and
`google-http-client-jackson2`.

## google-api-client-java6

Java 6 (and higher) extensions to the Google API Client Library for Java
(`google-api-client-java6`). This module depends on `google-api-client` and
`google-oauth-client-java6`.

## google-api-client-protobuf

[Protocol buffer][protobuf] extensions to the Google API Client Library for Java
(`google-api-client-protobuf`). This module depends on
`google-http-client-protobuf` and `google-api-client`.

## google-api-client-xml

XML extensions to the Google API Client Library for Java
(`google-api-client-xml`). This module depends on `google-api-client` and
`google-http-client-xml`.

[protobuf]: https://developers.google.com/protocol-buffers/docs/overview
