/*
 * Copyright 2010 Google Inc.
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

/**
 * Google's JSON support (see detailed package specification).
 *
 * <h2>Package Specification</h2>
 *
 * <p>
 * User-defined Partial JSON data models allow you to defined Plain Old Java
 * Objects (POJO's) to define how the library should parse/serialize JSON. Each
 * field that should be included must have
 * an @{@link com.google.api.client.util.Key} annotation. The field can be of
 * any visibility (private, package private, protected, or public) and must not
 * be static. By default, the field name is used as the JSON key. To override
 * this behavior, simply specify the JSON key use the optional value parameter
 * of the annotation, for example {@code @Key("name")}. Any unrecognized keys
 * from the JSON are normally simply ignored and not stored. If the ability to
 * store unknown keys is important, use
 * {@link com.google.api.client.json.GenericJson}.
 *
 * <p>
 * Let's take a look at a typical partial JSON-C video feed from the YouTube
 * Data API (as specified in <a href=
 * "https://developers.google.com/youtube/v3/code_samples/?csw=1">YouTube
 * Developer's Guide: JSON-C / JavaScript</a>)
 *
 * <pre>{@code
 * "data":{
 *  "updated":"2010-01-07T19:58:42.949Z",
 *                  "totalItems":800,
 *                  "startIndex":1,
 *                  "itemsPerPage":1,
 *                  "items":[
 *                          {"id":"hYB0mn5zh2c",
 *                                  "updated":"2010-01-07T13:26:50.000Z",
 *                                  "title":"Google Developers Day US - Maps API Introduction",
 *                                  "description":"Google Maps API Introduction ...",
 *                                  "tags":[
 *                                          "GDD07","GDD07US","Maps"],
 *                              "player":{
 *                              "default":"http://www.youtube.com/watch?v\u003dhYB0mn5zh2c" },
 *                              ...
 *                          }]}
 * }</pre>
 *
 * <p>
 * Here's one possible way to design the Java data classes for this (each class
 * in its own Java file):
 *
 * <pre>{@code
 * import com.google.api.client.util.*;
 * import java.util.List;
 *
 * public class VideoFeed {
 *   &#64;Key public int itemsPerPage;
 *   &#64;Key public int startIndex;
 *   &#64;Key public int totalItems;
 *   &#64;Key public DateTime updated;
 *   &#64;Key public List&lt;Video&gt; items;
 * }
 *
 * public class Video {
 *   &#64;Key public String id;
 *   &#64;Key public String title;
 *   &#64;Key public DateTime updated;
 *   &#64;Key public String description;
 *   &#64;Key public List&lt;String&gt; tags;
 *   &#64;Key public Player player;
 * }
 *
 * public class Player {
 *   // "default" is a Java keyword, so need to specify the JSON key manually
 *   &#64;Key("default")
 *   public String defaultUrl;
 * }
 * }</pre>
 *
 * <p>
 * You can also use the @{@link com.google.api.client.util.Key} annotation to
 * defined query parameters for a URL. For example:
 *
 * <pre>{@code
 * public class YouTubeUrl extends GoogleUrl {
 *
 *   &#64;Key
 *   public String author;
 *
 *   &#64;Key("max-results")
 *   public Integer maxResults;
 *
 *   public YouTubeUrl(String encodedUrl) {
 *     super(encodedUrl);
 *     this.alt = "jsonc";
 *   }
 * }</pre>
 *
 * <p>
 * To work with the YouTube API, you first need to set up the {@link
 * com.google.api.client.http.HttpTransport}. For example:
 *
 * <pre>{@code
 * private static HttpTransport setUpTransport() throws IOException {
 *   HttpTransport result = new NetHttpTransport();
 *   GoogleUtils.useMethodOverride(result);
 *   HttpHeaders headers = new HttpHeaders();
 *   headers.setApplicationName("Google-YouTubeSample/1.0");
 *   headers.gdataVersion = "2";
 *   JsonCParser parser = new JsonCParser();
 *   parser.jsonFactory = new GsonFactory();
 *   transport.addParser(parser);
 *   // insert authentication code...
 *   return transport;
 * }
 * }</pre>
 *
 * <p>
 * Now that we have a transport, we can execute a request to the YouTube API and
 * parse the result:
 *
 * <pre>{@code
 * public static VideoFeed list(HttpTransport transport, YouTubeUrl url) throws IOException {
 *   HttpRequest request = transport.buildGetRequest();
 *   request.url = url;
 *   return request.execute().parseAs(VideoFeed.class);
 * }
 * }</pre>
 *
 * <p>
 * If the server responds with an error the {@link
 * com.google.api.client.http.HttpRequest#execute} method will throw an {@link
 * com.google.api.client.http.HttpResponseException}, which has an {@link
 * com.google.api.client.http.HttpResponse} field which can be parsed the same
 * way as a success response inside of a catch block. For example:
 *
 * <pre>{@code
 * try {
 *   ...
 * } catch (HttpResponseException e) {
 *   if (e.response.getParser() != null) {
 *     Error error = e.response.parseAs(Error.class);
 *     // process error response
 *   } else {
 *     String errorContentString = e.response.parseAsString();
 *     // process error response as string
 *   }
 *   throw e;
 * }
 * }</pre>
 *
 * <p>
 * NOTE: As you might guess, the library uses reflection to populate the
 * user-defined data model. It's not quite as fast as writing the wire format
 * parsing code yourself can potentially be, but it's a lot easier.
 *
 * <p>
 * NOTE: If you prefer to use your favorite JSON parsing library instead (there
 * are many of them listed for example on
 * <a href="http://json.org">json.org</a>), that's supported as well. Just call
 * {@link com.google.api.client.http.HttpRequest#execute()} and parse the
 * returned byte stream.
 *
 * @since 1.0
 *
 * @author Yaniv Inbar
 */
package com.google.api.client.googleapis.json;
