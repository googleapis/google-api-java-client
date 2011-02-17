/*
 *
 * Copyright (c) 2010 Google Inc. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.auth;

import com.google.api.client.util.Base64;
import com.google.api.client.util.Strings;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utility methods for {@code "RSA-SHA1"} signing method.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class RsaSha {

  private static final String BEGIN = "-----BEGIN PRIVATE KEY-----";
  private static final String END = "-----END PRIVATE KEY-----";

  private RsaSha() {
  }

  /**
   * Retrieves the private key from the specified key store.
   *
   * @param keyStream input stream to the key store file
   * @param storePass password protecting the key store file
   * @param alias alias under which the private key is stored
   * @param keyPass password protecting the private key
   * @return the private key from the specified key store
   * @throws GeneralSecurityException if the key store cannot be loaded
   * @throws IOException if the file cannot be accessed
   */
  public static PrivateKey getPrivateKeyFromKeystore(
      InputStream keyStream, String storePass, String alias, String keyPass)
      throws IOException, GeneralSecurityException {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try {
      keyStore.load(keyStream, storePass.toCharArray());
      return (PrivateKey) keyStore.getKey(alias, keyPass.toCharArray());
    } finally {
      keyStream.close();
    }
  }

  /**
   * Reads a {@code PKCS#8} format private key from a given file.
   *
   * @throws NoSuchAlgorithmException
   */
  public static PrivateKey getPrivateKeyFromPk8(File file)
      throws IOException, GeneralSecurityException {
    byte[] privKeyBytes = new byte[(int) file.length()];
    DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
    try {
      inputStream.readFully(privKeyBytes);
    } finally {
      inputStream.close();
    }
    String str = new String(privKeyBytes);
    if (str.startsWith(BEGIN) && str.endsWith(END)) {
      str = str.substring(BEGIN.length(), str.lastIndexOf(END));
    }
    KeyFactory fac = KeyFactory.getInstance("RSA");
    EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(Strings.toBytesUtf8(str)));
    return fac.generatePrivate(privKeySpec);
  }

  /**
   * Signs the given data using the given private key.
   *
   * @throws GeneralSecurityException general security exception
   */
  public static String sign(PrivateKey privateKey, String data) throws GeneralSecurityException {
    Signature signature = Signature.getInstance("SHA1withRSA");
    signature.initSign(privateKey);
    signature.update(Strings.toBytesUtf8(data));
    return Strings.fromBytesUtf8(Base64.encode(signature.sign()));
  }
}
