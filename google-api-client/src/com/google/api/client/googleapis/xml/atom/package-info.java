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

/**
 * Google's Atom XML implementation (see detailed package specification).
 *
 * <h2>Package Specification</h2>
 *
 * <p>
 * User-defined Partial XML data models allow you to defined Plain Old Java Objects (POJO's) to
 * define how the library should parse/serialize XML. Each field that should be included must have
 * an @{@link com.google.api.client.util.Key} annotation. The field can be of any visibility
 * (private, package private, protected, or public) and must not be static.
 * </p>
 *
 * <p>
 * The optional value parameter of this @{@link com.google.api.client.util.Key} annotation specifies
 * the XPath name to use to represent the field. For example, an XML attribute <code>a</code> has an
 * XPath name of <code>@a</code>, an XML element <code>&lt;a&gt;</code> has an XPath name of <code>
 * a</code>, and an XML text content has an XPath name of <code>text()</code>. These are named based
 * on their usage with the <a
 * href="http://code.google.com/apis/gdata/docs/2.0/reference.html#PartialResponse">partial
 * response/update syntax</a> for Google API's. If the @{@link com.google.api.client.util.Key}
 * annotation is missing, the default is to use the Atom XML namespace and the Java field's name as
 * the local XML name. By default, the field name is used as the JSON key. Any unrecognized XML is
 * normally simply ignored and not stored. If the ability to store unknown keys is important, use
 * {@link com.google.api.client.xml.GenericXml}.
 * </p>
 *
 * <p>
 * Let's take a look at a typical partial Atom XML album feed from the Picasa Web Albums Data API:
 * </p>
 *
 * <pre><code>
&lt;?xml version='1.0' encoding='utf-8'?&gt;
&lt;feed xmlns='http://www.w3.org/2005/Atom'
    xmlns:openSearch='http://a9.com/-/spec/opensearch/1.1/'
    xmlns:gphoto='http://schemas.google.com/photos/2007'&gt;
  &lt;link rel='http://schemas.google.com/g/2005#post'
    type='application/atom+xml'
    href='http://picasaweb.google.com/data/feed/api/user/liz' /&gt;
  &lt;author&gt;
    &lt;name&gt;Liz&lt;/name&gt;
  &lt;/author&gt;
  &lt;openSearch:totalResults&gt;1&lt;/openSearch:totalResults&gt;
  &lt;entry gd:etag='"RXY8fjVSLyp7ImA9WxVVGE8KQAE."'&gt;
    &lt;category scheme='http://schemas.google.com/g/2005#kind'
      term='http://schemas.google.com/photos/2007#album' /&gt;
    &lt;title&gt;lolcats&lt;/title&gt;
    &lt;summary&gt;Hilarious Felines&lt;/summary&gt;
    &lt;gphoto:access&gt;public&lt;/gphoto:access&gt;
  &lt;/entry&gt;
&lt;/feed&gt;
</code></pre>
 *
 * <p>
 * Here's one possible way to design the Java data classes for this (each class in its own Java
 * file):
 * </p>
 *
 * <pre><code>
import com.google.api.client.util.*;
import java.util.List;

  public class Link {

    &#64;Key("&#64;href")
    public String href;

    &#64;Key("&#64;rel")
    public String rel;

    public static String find(List&lt;Link&gt; links, String rel) {
      if (links != null) {
        for (Link link : links) {
          if (rel.equals(link.rel)) {
            return link.href;
          }
        }
      }
      return null;
    }
  }

  public class Category {

    &#64;Key("&#64;scheme")
    public String scheme;

    &#64;Key("&#64;term")
    public String term;

    public static Category newKind(String kind) {
      Category category = new Category();
      category.scheme = "http://schemas.google.com/g/2005#kind";
      category.term = "http://schemas.google.com/photos/2007#" + kind;
      return category;
    }
  }

  public class AlbumEntry {

    &#64;Key
    public String summary;

    &#64;Key
    public String title;

    &#64;Key("gphoto:access")
    public String access;

    public Category category = newKind("album");
    
    private String getEditLink() {
      return Link.find(links, "edit");
    }
  }

  public class Author {

    &#64;Key
    public String name;
  }

  public class AlbumFeed {

    &#64;Key
    public Author author;

    &#64;Key("openSearch:totalResults")
    public int totalResults;

    &#64;Key("entry")
    public List&lt;AlbumEntry&gt; photos;

    &#64;Key("link")
    public List&lt;Link&gt; links;

    private String getPostLink() {
      return Link.find(links, "http://schemas.google.com/g/2005#post");
    }
  }
</code></pre>
 *
 * <p>
 * You can also use the @{@link com.google.api.client.util.Key} annotation to defined query
 * parameters for a URL. For example:
 * </p>
 *
 * <pre><code>
public class PicasaUrl extends GoogleUrl {

  &#64;Key("max-results")
  public Integer maxResults;

  &#64;Key
  public String kinds;

  public PicasaUrl(String url) {
    super(url);
  }

  public static PicasaUrl fromRelativePath(String relativePath) {
    PicasaUrl result = new PicasaUrl(PicasaWebAlbums.ROOT_URL);
    result.path += relativePath;
    return result;
  }
}
</code></pre>
 *
 * <p>
 * To work with a Google API, you first need to set up the
 * {@link com.google.api.client.http.HttpTransport}. For example:
 * </p>
 *
 * <pre><code>
  private static GoogleTransport setUpGoogleTransport() throws IOException {
    GoogleTransport transport = new GoogleTransport();
    transport.applicationName = "google-picasaatomsample-1.0";
    transport.setVersionHeader(PicasaWebAlbums.VERSION);
    AtomParser parser = new AtomParser();
    parser.namespaceDictionary = PicasaWebAlbumsAtom.NAMESPACE_DICTIONARY;
    transport.addParser(parser);
    // insert authentication code...
    return transport;
  }
</code></pre>
 *
 * <p>
 * Now that we have a transport, we can execute a partial GET request to the Picasa Web Albums API
 * and parse the result:
 * </p>
 *
 * <pre><code>
  public static AlbumFeed executeGet(GoogleTransport transport, PicasaUrl url)
      throws IOException {
    url.fields = GData.getFieldsFor(AlbumFeed.class);
    url.kinds = "photo";
    url.maxResults = 5;
    HttpRequest request = transport.buildGetRequest();
    request.url = url;
    return request.execute().parseAs(AlbumFeed.class);
  }
</code></pre>
 *
 * <p>
 * If the server responds with an error the {@link com.google.api.client.http.HttpRequest#execute}
 * method will throw an {@link com.google.api.client.http.HttpResponseException}, which has an
 * {@link com.google.api.client.http.HttpResponse} field which can be parsed the same way as a
 * success response inside of a catch block. For example:
 * </p>
 *
 * <pre><code>
    try {
...
    } catch (HttpResponseException e) {
      if (e.response.getParser() != null) {
        Error error = e.response.parseAs(Error.class);
        // process error response
      } else {
        String errorContentString = e.response.parseAsString();
        // process error response as string
      }
      throw e;
    }
</code></pre>
 *
 * <p>
 * To update an album, we use the transport to execute an efficient partial update request using the
 * PATCH method to the Picasa Web Albums API:
 * </p>
 *
 * <pre><code>
  public AlbumEntry executePatchRelativeToOriginal(GoogleTransport transport,
      AlbumEntry original) throws IOException {
    HttpRequest request = transport.buildPatchRequest();
    request.setUrl(getEditLink());
    request.headers.ifMatch = etag;
    PatchRelativeToOriginalContent content =
        new PatchRelativeToOriginalContent();
    content.namespaceDictionary = PicasaWebAlbumsAtom.NAMESPACE_DICTIONARY;
    content.originalEntry = original;
    content.patchedEntry = this;
    request.content = content;
    return request.execute().parseAs(AlbumEntry.class);
  }

  private static AlbumEntry updateTitle(GoogleTransport transport,
      AlbumEntry album) throws IOException {
    AlbumEntry patched = album.clone();
    patched.title = "An alternate title";
    return patched.executePatchRelativeToOriginal(transport, album);
  }
</code></pre>
 *
 * <p>
 * To insert an album, we use the transport to execute a POST request to the Picasa Web Albums API:
 * </p>
 *
 * <pre><code>
  public AlbumEntry insertAlbum(GoogleTransport transport, AlbumEntry entry)
      throws IOException {
    HttpRequest request = transport.buildPostRequest();
    request.setUrl(getPostLink());
    AtomContent content = new AtomContent();
    content.namespaceDictionary = PicasaWebAlbumsAtom.NAMESPACE_DICTIONARY;
    content.entry = entry;
    request.content = content;
    return request.execute().parseAs(AlbumEntry.class);
  }
</code></pre>
 *
 * <p>
 * To delete an album, we use the transport to execute a DELETE request to the Picasa Web Albums
 * API:
 * </p>
 *
 * <pre><code>
  public void executeDelete(GoogleTransport transport) throws IOException {
    HttpRequest request = transport.buildDeleteRequest();
    request.setUrl(getEditLink());
    request.headers.ifMatch = etag;
    request.execute().ignore();
  }
</code></pre>
 *
 * <p>
 * NOTE: As you might guess, the library uses reflection to populate the user-defined data model.
 * It's not quite as fast as writing the wire format parsing code yourself can potentially be, but
 * it's a lot easier.
 * </p>
 *
 * <p>
 * NOTE: If you prefer to use your favorite XML parsing library instead (there are many of them),
 * that's supported as well. Just call {@link com.google.api.client.http.HttpRequest#execute()} and
 * parse the returned byte stream.
 * </p>
 *
 *
 * <p>
 * <b>Warning: this package is experimental, and its content may be changed in incompatible ways or
 * possibly entirely removed in a future version of the library</b>
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */

package com.google.api.client.googleapis.xml.atom;

