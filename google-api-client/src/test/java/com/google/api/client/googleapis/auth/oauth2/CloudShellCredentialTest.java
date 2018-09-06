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

import static org.junit.Assert.assertEquals;

import com.google.api.client.json.gson.GsonFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for CloudShellCredential
 */
@RunWith(JUnit4.class)
public class CloudShellCredentialTest {

  @Test
  public void refreshAccessToken() throws IOException{
    final ServerSocket authSocket = new ServerSocket(0);
    Runnable serverTask =
        new Runnable() {
          @Override
          public void run() {
            try {
              Socket clientSocket = authSocket.accept();
              BufferedReader input =
                  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
              String lines = input.readLine();
              lines += '\n' + input.readLine();
              assertEquals(CloudShellCredential.GET_AUTH_TOKEN_REQUEST, lines);

              PrintWriter out =
                  new PrintWriter(clientSocket.getOutputStream(), true);
              out.println("32\n[\"email\", \"project-id\", \"token\", 1234]");
            } catch (Exception reThrown) {
              throw new RuntimeException(reThrown);
            }
          }
        };
    Thread serverThread = new Thread(serverTask);
    serverThread.start();

    GoogleCredential creds = new CloudShellCredential(
        authSocket.getLocalPort(), GsonFactory.getDefaultInstance());
    assertEquals("token", creds.executeRefreshToken().getAccessToken());
  }
}
