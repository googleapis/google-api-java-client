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

package com.google.api.client.http;

import java.io.IOException;

/**
 * HTTP request execute intercepter invoked at the start of {@link HttpRequest#execute()}.
 * <p>
 * Useful for example for signing HTTP requests during authentication. Care should be taken to
 * ensure that intercepters not interfere with each other since there are no guarantees regarding
 * their independence. In particular, the order in which the intercepters are invoked is important.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public interface HttpExecuteIntercepter {

  /**
   * Invoked at the start of {@link HttpRequest#execute()}.
   *
   * @throws IOException any I/O exception
   */
  void intercept(HttpRequest request) throws IOException;
}
