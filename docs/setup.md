---
title: Setup Instructions
---

# Setup Instructions

You can download the Google API Client Library for Java and its dependencies in 
a zip file, or you can use a dependency manager such as Maven or gradle to
install the necessary jars from the Maven Central repository.

## Maven

The Google API Client Library for Java is in the central Maven repository. The
Maven `groupId` for all artifacts for this library is `com.google.api-client`.

To ensure all dependency versions work together and to avoid having to manually
choose and specify versions for each dependency, we recommend first importing
the `com.google.cloud:libraries-bom` in the `dependencyManagement` section of
your `pom.xml`:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.google.cloud</groupId>
      <artifactId>libraries-bom</artifactId>
      <version>2.2.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
 </dependencies>
</dependencyManagement>
```

Then you add the individual dependencies you need without version numbers to the
dependencies section:

```xml
<dependency>
  <groupId>com.google.api-client</groupId>
  <artifactId>google-api-client</artifactId>
</dependency>
```

On Android, you may need to explicitly exclude unused dependencies:

```xml
<dependency>
  <groupId>com.google.api-client</groupId>
  <artifactId>google-api-client</artifactId>
  <exclusions>
    <exclusion>
      <artifactId>xpp3</artifactId>
      <groupId>xpp3</groupId>
    </exclusion>
    <exclusion>
      <artifactId>httpclient</artifactId>
      <groupId>org.apache.httpcomponents</groupId>
    </exclusion>
    <exclusion>
      <artifactId>junit</artifactId>
      <groupId>junit</groupId>
    </exclusion>
    <exclusion>
      <artifactId>android</artifactId>
      <groupId>com.google.android</groupId>
    </exclusion>
  </exclusions>
</dependency>
```

## Download the library with dependencies

Download the latest assembly zip file from Maven Central and extract it on your
computer. This zip contains the client library class jar files and the
associated source jar files for each artifact and their dependencies. You can
find dependency graphs and licenses for the different libraries in the
dependencies folder. For more details about the contents of the download,
see the contained `readme.html` file.
