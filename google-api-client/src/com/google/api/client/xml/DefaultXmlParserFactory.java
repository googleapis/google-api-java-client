package com.google.api.client.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * Default XML parser factory that uses the default specified in {@link XmlPullParserFactory}.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class DefaultXmlParserFactory implements XmlParserFactory {

  /**
   * Only instance or {@code null} if {@link #getInstance()} has not been called.
   */
  private static DefaultXmlParserFactory INSTANCE;

  /** Returns the only instance of the default XML parser factory. */
  public static DefaultXmlParserFactory getInstance() throws XmlPullParserException {
    if (INSTANCE == null) {
      INSTANCE = new DefaultXmlParserFactory();
    }
    return INSTANCE;
  }

  /** XML pull parser factory. */
  private final XmlPullParserFactory factory;

  private DefaultXmlParserFactory() throws XmlPullParserException {
    factory = XmlPullParserFactory.newInstance(
        System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
    factory.setNamespaceAware(true);
  }

  public XmlPullParser createParser() throws XmlPullParserException {
    return factory.newPullParser();
  }

  public XmlSerializer createSerializer() throws XmlPullParserException {
    return factory.newSerializer();
  }
}
