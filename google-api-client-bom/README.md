# Google API Client Library Bill of Materials

The `google-api-client-bom` module is a pom that can be used to import consistent 
versions of `google-api-client` components.

To use it in Maven, add the following to your `pom.xml`:

[//]: # ({x-version-update-start:google-api-client:released})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.google.api-client</groupId>
      <artifactId>google-api-client-bom</artifactId>
      <version>1.28.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

## License

Apache 2.0 - See [LICENSE] for more information.

[LICENSE]: https://github.com/googleapis/google-api-java-client/blob/master/LICENSE
