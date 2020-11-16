/*
 * Copyright 2020 Google LLC
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

package com.google.api.client.googleapis.apache.v2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.googleapis.apache.v2.GoogleApacheHttpTransport.SocketFactoryRegistryHandler;
import com.google.api.client.googleapis.mtls.MtlsProvider;
import com.google.api.client.googleapis.mtls.MtlsTransportBaseTest;
import com.google.api.client.http.HttpTransport;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.Test;

public class GoogleApacheHttpTransportTest extends MtlsTransportBaseTest {
  @Override
  protected HttpTransport buildTrustedTransport(MtlsProvider mtlsProvider)
      throws GeneralSecurityException, IOException {
    return GoogleApacheHttpTransport.newTrustedTransport(mtlsProvider);
  }

  @Test
  public void socketFactoryRegistryHandlerTest() throws GeneralSecurityException, IOException {
    MtlsProvider mtlsProvider = new TestMtlsProvider(true, createTestMtlsKeyStore(), "", false);
    SocketFactoryRegistryHandler handler = new SocketFactoryRegistryHandler(mtlsProvider);
    assertNotNull(handler.getSocketFactoryRegistry().lookup("http"));
    assertNotNull(handler.getSocketFactoryRegistry().lookup("https"));
    assertTrue(handler.isMtls());
  }
}
