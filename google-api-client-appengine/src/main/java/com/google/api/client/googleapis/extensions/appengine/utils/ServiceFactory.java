/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis.extensions.appengine.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.OAuthContext;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author ngmiceli@google.com (Your Name Here)
 *
 */
public class ServiceFactory {

  public static <T extends AbstractGoogleJsonClient> T createService(
      Class<T> service, OAuthContext context)
      throws IOException {
    for (Class<?> builderClass : service.getDeclaredClasses()) {
      if (AbstractGoogleJsonClient.Builder.class.isAssignableFrom(builderClass)) {
        try {
          Constructor<?> constructor = builderClass.getConstructor(
              HttpTransport.class, JsonFactory.class, HttpRequestInitializer.class);
          Credential credential = context.getFlow()
              .loadCredential(UserServiceFactory.getUserService().getCurrentUser().getUserId());
          // TODO(NOW): Do we want to allow the user to pass their own HttpRequestInitializer?
          AbstractGoogleJsonClient.Builder builder = (Builder) constructor.newInstance(
              context.getTransport(), context.getJsonFactory(), credential);
          builder.setApplicationName(context.getUserAgent());
          @SuppressWarnings("unchecked")
          T t = (T) builder.build();
          return t;
        } catch (ClassCastException exception) {
        } catch (NoSuchMethodException exception) {
        } catch (SecurityException exception) {
        } catch (InstantiationException exception) {
        } catch (IllegalAccessException exception) {
        } catch (IllegalArgumentException exception) {
        } catch (InvocationTargetException exception) {
        }
      }
    }
    // TODO(NOW): Figure out the right way to handle all these exceptions.
    throw new IllegalArgumentException();
  }

}
