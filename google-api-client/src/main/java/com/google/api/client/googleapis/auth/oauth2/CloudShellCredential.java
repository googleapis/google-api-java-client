/* 
 * Copyright 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.JsonFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * OAuth2 credentials representing the built-in service account for Google Cloud Shell.
 *
 * @since 1.21.0
 * @deprecated Please use <a href="https://github.com/googleapis/google-auth-library-java">
 *   google-auth-library</a> for handling authentication and authorization from Cloud Shell.
 */
@Deprecated
public class CloudShellCredential extends GoogleCredential {

  private static final int ACCESS_TOKEN_INDEX = 2;
  private static final int READ_TIMEOUT_MS = 5000;

  /**
   * The Cloud Shell back authorization channel uses serialized
   * Javascript Protobufers, preceeded by the message lengeth and a
   * new line character. However, the request message has no content,
   * so a token request consists of an empty JsPb, and its 2 character
   * lenth prefix.
   */
  protected static final String GET_AUTH_TOKEN_REQUEST = "2\n[]";

  private final int authPort;
  private final JsonFactory jsonFactory;

  public CloudShellCredential(int authPort, JsonFactory jsonFactory) {
    this.authPort = authPort;
    this.jsonFactory = jsonFactory;
  }

  protected int getAuthPort() {
    return this.authPort;
  }

  @Override
  protected TokenResponse executeRefreshToken()
      throws IOException {
    Socket socket = new Socket("localhost", this.getAuthPort());
    socket.setSoTimeout(READ_TIMEOUT_MS);
    TokenResponse token = new TokenResponse();
    try {
      PrintWriter out =
        new PrintWriter(socket.getOutputStream(), true);
      out.println(GET_AUTH_TOKEN_REQUEST);

      BufferedReader input =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // Ignore the size line
      input.readLine();

      Collection<Object> messageArray = jsonFactory.createJsonParser(input)
        .parseArray(LinkedList.class, Object.class);
      String accessToken = ((List<Object>) messageArray).get(ACCESS_TOKEN_INDEX).toString();
      token.setAccessToken(accessToken);
    } finally {
      socket.close();
    }
    return token;
  }
}
