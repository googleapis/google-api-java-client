package com.google.api.client.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Serializes MIME Multipart/Related content as specified by <a
 * href="http://tools.ietf.org/html/rfc2387">RFC 2387: The MIME Multipart/Related Content-type</a>.
 * <p>
 * Limitations:
 * <ul>
 * <li>No support of parameters other than {@code "boundary"}</li>
 * <li>No support for specifying headers for each content part</li>
 * </ul>
 * </p>
 * <p>
 * Sample usage:
 *
 * <pre><code>
  static void setMediaWithMetadataContent(HttpRequest request,
      AtomContent atomContent, InputStreamContent imageContent) {
    MultipartRelatedContent content =
        MultipartRelatedContent.forRequest(request);
    content.parts.add(atomContent);
    content.parts.add(imageContent);
  }
 * </code></pre>
 *
 * @since 1.1
 * @author Yaniv Inbar
 */
public final class MultipartRelatedContent implements HttpContent {

  /** Boundary string to use. By default, it is {@code "END_OF_PART"}. */
  public String boundary = "END_OF_PART";

  /**
   * Collection of HTTP content parts.
   * <p>
   * By default, it is an empty list. Note that the content type for each part is required, so
   * {@link HttpContent#getType()} must not be {@code null}.
   * </p>
   */
  public Collection<HttpContent> parts = new ArrayList<HttpContent>();

  private static final byte[] CR_LF = "\r\n".getBytes();
  private static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes();
  private static final byte[] CONTENT_TRANSFER_ENCODING =
      "Content-Transfer-Encoding: binary".getBytes();
  private static final byte[] TWO_DASHES = "--".getBytes();

  /**
   * Returns a new multi-part content serializer as the content for the given HTTP request.
   *
   * <p>
   * It also sets the {@link HttpHeaders#mimeVersion} of {@link HttpRequest#headers headers} to
   * {@code "1.0"}.
   * </p>
   *
   * @param request HTTP request
   * @return new multi-part content serializer
   */
  public static MultipartRelatedContent forRequest(HttpRequest request) {
    MultipartRelatedContent result = new MultipartRelatedContent();
    request.headers.mimeVersion = "1.0";
    request.content = result;
    return result;
  }

  public void writeTo(OutputStream out) throws IOException {
    byte[] END_OF_PART = boundary.getBytes();
    out.write(TWO_DASHES);
    out.write(END_OF_PART);
    for (HttpContent part : parts) {
      String contentType = part.getType();
      byte[] typeBytes = contentType.getBytes();
      out.write(CR_LF);
      out.write(CONTENT_TYPE);
      out.write(typeBytes);
      out.write(CR_LF);
      if (!LogContent.isTextBasedContentType(contentType)) {
        out.write(CONTENT_TRANSFER_ENCODING);
        out.write(CR_LF);
      }
      out.write(CR_LF);
      part.writeTo(out);
      out.write(CR_LF);
      out.write(TWO_DASHES);
      out.write(END_OF_PART);
    }
    out.write(TWO_DASHES);
    out.flush();
  }

  public String getEncoding() {
    return null;
  }

  public long getLength() {
    // TODO: compute this?
    return -1;
  }

  public String getType() {
    return "multipart/related; boundary=\"END_OF_PART\"";
  }

}
