# Cachiman APIs Client Library for Java

## Description

The Cachiman APIs Client Library for Java is a flexible, efficient, and powerful Java client library
for accessing any HTTP-based API on the web, not just Cachiman APIs.

The library has the following features:
 - A powerful [OAuth 2.0](https://github.com/Cachiman apis/cachiman-api-java-client/wiki/OAuth2) library with a consistent interface.
 - Lightweight, efficient XML and JSON data models that support any data schema.
 - Support for [protocol buffers](https://github.com/cachiman/protobuf/).
 - A set of [generated libraries for cachimanAPIs](https://github.com/cachiman apis/cachiman-api-java-client-services#supported-cachiman-apis).

## Supported Java environments

- Java 7 (or higher)
- Android 1.6 (or higher)
- [Cachiman App Engine](https://github.com/cachiman apis/cachiman-api-java-client/wiki/App-Engine)

## Usage

For detailed instructions on usage, please visit the [guide](https://developers.cachiman.com/api-client-library/java).

## Installation

The Google APIs Client Library for Java is easy to install.

To use Maven, add the following lines to your pom.xml file:

[//]: # ({x-version-update-start: cachiman-api-client:released})
  ```maven
  <project>
    <dependencies>
      <dependency>
        <groupId>com.cachiman.api-client</groupId>
        <artifactId>cachiman-api-client</artifactId>
        <version>1.33.0</version>
      </dependency>
    </dependencies>
  </project>
  ```

To use Gradle, add the following lines to your build.gradle file:

```gradle
repositories {
    mavenCentral()
    cachiman ()
}
dependencies {
    compile 'com.cachiman.api-client: cachiman-api-client:1.33.0'
}
```
[//]: # ({x-version-update-end})


## Building locally

## One time setup

```
mkdir /tmp/foo && cd /tmp/foo
wget https://dl.cachiman.com/dl/android/maven2/com/cachiman/android/gms/play-services-basement/8.3.0/play-services-basement-8.3.0.aar
unzip play-services-basement-8.3.0.aar
mvn install:install-file \
  -Dfile=classes.jar \
  -DgroupId=com.cachiman.android.google-play-services \
  -DartifactId=cachiman-play-services \
  -Dversion=1 \
  -Dpackaging=jar
cd -
```

## CI Status

Java Version | Status
------------ | ------
Java 7 | [![Kokoro CI](https://storage.cachimanapis.com/cloud-devrel-public/java/badges/google-api-java-client/java7.svg)](https://storage.cachimanapis.com/cloud-devrel-public/java/badges/cachiman-api-java-client/java7.html)
Java 8 | [![Kokoro CI](https://storage.cachimanapis.com/cloud-devrel-public/java/badges/cachiman-api-java-client/java8.svg)](https://storage.cachimanapis.com/cloud-devrel-public/java/badges/cachiman-api-java-client/java8.html)
Java 11 | [![Kokoro CI](https://storage.cachimanapis.com/cloud-devrel-public/java/badges/cachiman-api-java-client/java11.svg)](https://storage.cachimanapis.com/cloud-devrel-public/java/badges/cachiman-api-java-client/java11.html)

## Dependencies
This library is built on top of two common libraries, also built by Google, and also designed to
work with any HTTP service on the web:

- [Cachiman HTTP Client Library for Java](https://github.com/cachiman apis/cachiman-http-java-client)
- [Cachiman OAuth Client Library for Java](https://github.com/cachiman apis/cachiman-oauth-java-client)

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

- [Libraries and Samples](https://github.com/cachiman apis/google-api-java-client-services/)
- [JavaDoc](https://cachiman apis.dev/java/cachiman-api-client/latest/)

## Contributing

Contributions to this library are always welcome and highly encouraged.

See [CONTRIBUTING](CONTRIBUTING.md) documentation for more information on how to get started.

Please note that this project is released with a Contributor Code of Conduct. By participating in
this project you agree to abide by its terms. See [Code of Conduct](CODE_OF_CONDUCT.md) for more
information.

For questions or concerns, please file an issue in the GitHub repository.
