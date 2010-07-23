/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.Json;

import java.io.IOException;

/**
 * Parses HTTP JSON-C response content into an data class of key/value pairs.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in version 1.1) Use {@link JsonCParser}
 */
@Deprecated
public final class JsonParser implements HttpParser {

  public String getContentType() {
    return Json.CONTENT_TYPE;
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass)
      throws IOException {
    return JsonHttp.parse(response, dataClass);
  }
}
