# https://github.com/googleapis/google-api-java-client/issues/1450
-keep public class com.google.api.client.googleapis.GoogleUtils

# Data's magic null values rely on reference identity. Keep R8 from replacing
# wrapper instances with canonical values such as Boolean.TRUE.
# https://github.com/googleapis/google-api-java-client/issues/2603
-keepclassmembers class com.google.api.client.util.Data {
  public static final *** NULL_*;
}
