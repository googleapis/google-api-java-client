/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.json;

import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;

import java.util.Map;

/**
 * Manages a JSON-formatted document from the experimental Google Discovery API version 0.1.
 *
 * <p>
 * Warning: this is based on an undocumented experimental Google API that may stop working or change
 * in behavior at any time. Beware of this risk if running this in production code.
 * </p>
 * <p>
 * Warning: in prior version 1.2 there was a {@code load(String)} method. Instead, you should now
 * use {@link GoogleApi#load()}.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class DiscoveryDocument {

  /**
   * Defines all versions of an API.
   *
   * @since 1.1
   */
  public static final class APIDefinition extends ArrayMap<String, ServiceDefinition> {
  }

  /** Defines a specific version of an API. */
  public static final class ServiceDefinition {
    /**
     * Base URL for service endpoint.
     *
     * @since 1.1
     */
    @Key
    public String baseUrl;

    /** Map from the resource name to the resource definition. */
    @Key
    public Map<String, ServiceResource> resources;

    /**
     * Returns {@link ServiceMethod} definition for given method name. Method identifier is of
     * format "resourceName.methodName".
     */
    public ServiceMethod getResourceMethod(String methodIdentifier) {
      int dot = methodIdentifier.indexOf('.');
      String resourceName = methodIdentifier.substring(0, dot);
      String methodName = methodIdentifier.substring(dot + 1);
      ServiceResource resource = resources.get(resourceName);
      return resource == null ? null : resource.methods.get(methodName);
    }

    /**
     * Returns url for requested method. Method identifier is of format "resourceName.methodName".
     */
    String getResourceUrl(String methodIdentifier) {
      return baseUrl + getResourceMethod(methodIdentifier).pathUrl;
    }
  }

  /** Defines a resource in a service definition. */
  public static final class ServiceResource {

    /** Map from method name to method definition. */
    @Key
    public Map<String, ServiceMethod> methods;
  }

  /** Defines a method of a service resource. */
  public static final class ServiceMethod {

    /**
     * Path URL relative to base URL.
     *
     * @since 1.1
     */
    @Key
    public String pathUrl;

    /** HTTP method name. */
    @Key
    public String httpMethod;

    /** Map from parameter name to parameter definition. */
    @Key
    public Map<String, ServiceParameter> parameters;

    /**
     * Method type.
     *
     * @since 1.1
     */
    @Key
    public final String methodType = "rest";
  }

  /** Defines a parameter to a service method. */
  public static final class ServiceParameter {

    /** Whether the parameter is required. */
    @Key
    public boolean required;
  }

  /**
   * Definition of all versions defined in this Google API.
   *
   * @since 1.1
   */
  public final APIDefinition apiDefinition = new APIDefinition();

  DiscoveryDocument() {
  }
}
