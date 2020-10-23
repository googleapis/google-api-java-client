/*
 * Copyright 2020 Google Inc.
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

package com.google.api.client.googleapis.util;

/**
 * Class for mutual TLS channel client certificate.
 */
public final class ClientCert {
    /** X.509 certificate string in PEM format for mutual TLS. */
    public String certificate;

    /** Private key string in PEM format for mutual TLS. */
    public String privateKey;

    /**
     * Constructor with the given certificate and private key.
     * @param certificate X.509 certificate string in PEM format for mutual TLS
     * @param privateKey Private key string in PEM format for mutual TLS
     */
    public ClientCert(String certificate, String privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }
}
