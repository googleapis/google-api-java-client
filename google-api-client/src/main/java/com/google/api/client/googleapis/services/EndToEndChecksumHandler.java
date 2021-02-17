/*
 * Copyright 2021 Google LLC
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

package com.google.api.client.googleapis.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/** This class provides End-to-End Checksum API for http protocol. */
public class EndToEndChecksumHandler {
  /** The checksum http header on http requests */
  public static final String HTTP_REQUEST_CHECKSUM_HEADER = "x-request-checksum-348659783";
  /** The checksum http header on http responses */
  public static final String HTTP_RESPONSE_CHECKSUM_HEADER = "x-response-checksum-348659783";

  /**
   * Create and return checksum as a string value for the input 'bytes'.
   *
   * @param bytes raw message for which the checksum is being computed
   * @return computed checksum as a {@code byte array}
   * @throws RuntimeException if MD5 Algorithm is not found in the VM
   */
  public static byte[] computeChecksum(byte[] bytes) {
    if (bytes == null || (bytes.length == 0)) {
      return null;
    }

    try {
      return MessageDigest.getInstance("MD5").digest(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm is not found when computing checksum!");
    }
  }

  /**
   * Validates the checksum for the given input 'bytes' and returns true if valid, false otherwise.
   *
   * @param checksum the checksum value
   * @param bytes the raw message for which the checksum was sent
   * @return {@code true} if input checksum is valid for the input bytes; {@code false} otherwise
   */
  public static boolean validateChecksum(byte[] checksum, byte[] bytes) {
    return checksum != null
        && checksum.length > 0
        && bytes != null
        && bytes.length > 0
        && Arrays.equals(computeChecksum(bytes), checksum);
  }
}
