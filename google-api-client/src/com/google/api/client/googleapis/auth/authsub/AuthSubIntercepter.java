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

package com.google.api.client.googleapis.auth.authsub;

import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

/**
 * @author Yaniv Inbar
 */
final class AuthSubIntercepter implements HttpExecuteIntercepter {

  private final String token;
  private final PrivateKey privateKey;

  AuthSubIntercepter(String token, PrivateKey privateKey) {
    this.token = token;
    this.privateKey = privateKey;
  }

  public void intercept(HttpRequest request) throws IOException {
    try {
      String header;
      String token = this.token;
      PrivateKey privateKey = this.privateKey;
      if (token == null) {
        header = null;
      } else if (privateKey == null) {
        header = AuthSub.getAuthorizationHeaderValue(token);
      } else {
        header = AuthSub.getAuthorizationHeaderValue(
            token, privateKey, request.method.name(), request.url.build());
      }
      request.headers.authorization = header;
    } catch (GeneralSecurityException e) {
      IOException wrap = new IOException();
      wrap.initCause(e);
      throw wrap;
    }
  }
}
