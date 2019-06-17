package com.google.api.client.googleapis.services;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

/**
 * This class.
 */
@AutoValue
public abstract class CommonGoogleOptions {
  @Nullable
  abstract String getKey();

  @Nullable
  abstract String getRequestReason();

  @Nullable
  abstract String getUserAgent();

  @Nullable
  abstract String getUserIp();

  @Nullable
  abstract String getUserProject();

  static CommonGoogleOptions.Builder newBuilder() {
    return new AutoValue_CommonGoogleOptions.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setKey(String key);
    abstract Builder setRequestReason(String requestReason);
    abstract Builder setUserAgent(String userAgent);
    abstract Builder setUserIp(String userIp);
    abstract Builder setUserProject(String userProject);

    abstract CommonGoogleOptions build();
  }
}
