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

package com.google.api.client.json.rpc2;

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

/**
 * JSON-RPC 2.0 request object.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class JsonRpcRequest extends GenericData {

  /**
   * A String specifying the version of the JSON-RPC protocol. MUST be exactly "2.0".
   */
  @Key
  public final String jsonrpc = "2.0";

  /**
   * An identifier established by the Client that MUST contain a String or a Number. If it is not
   * included it is assumed to be a notification, and will not receive a response.
   */
  @Key
  public Object id;

  /** A String containing the name of the method to be invoked. */
  @Key
  public String method;

  /**
   * A Structured value that holds the parameter values to be used during the invocation of the
   * method. This member MAY be omitted.
   */
  @Key
  public Object params;
}
