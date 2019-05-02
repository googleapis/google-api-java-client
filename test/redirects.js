// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

'use strict';

const assert = require('assert');
const redirectUrl = require('../src/redirects.js').redirectUrl;

describe('redirectUrl', () => {

  it('should handle a versioned doc', () => {
    const url = 'https://googleapis.github.io/google-api-java-client/releases/1.25.0/javadoc/index.html';
    const expected = 'https://googleapis.dev/java/google-api-client/1.25.0/index.html';
    assert.equal(redirectUrl(url), expected);
  });

  it('should handle root page without index.html', () => {
    const url = 'https://googleapis.github.io/google-api-java-client/releases/1.25.0/javadoc/';
    const expected = 'https://googleapis.dev/java/google-api-client/1.25.0/';
    assert.equal(redirectUrl(url), expected);
  });

  it('should handle a deeplink', () => {
    const url = 'https://googleapis.github.io/google-api-java-client/releases/1.25.0/javadoc/index.html?com/google/api/client/http/HttpIOExceptionHandler.html';
    const expected = 'https://googleapis.dev/java/google-api-client/1.25.0/index.html?com/google/api/client/http/HttpIOExceptionHandler.html';
    assert.equal(redirectUrl(url), expected);
  });

  it('should handle anchor to method', () => {
    const url = 'https://googleapis.github.io/google-api-java-client/releases/1.25.0/javadoc/com/google/api/client/http/AbstractHttpContent.html#computeLength-com.google.api.client.http.HttpContent-';
    const expected = 'https://googleapis.dev/java/google-api-client/1.25.0/com/google/api/client/http/AbstractHttpContent.html#computeLength-com.google.api.client.http.HttpContent-';
    assert.equal(redirectUrl(url), expected);
  });

  it('defaults to the latest docs', () => {
    const path = 'http://example.com/abcd';
    const expected = null;
    assert.equal(redirectUrl(path), expected);
  });
});