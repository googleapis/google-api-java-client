package com.google.api.client.xml;

import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * XML utilities.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public class Xml {

  /** XML Parser factory. */
  public static XmlParserFactory parserFactory;

  /** Returns the parser factory. */
  private static XmlParserFactory getParserFactory()
      throws XmlPullParserException {
    XmlParserFactory parserFactory = Xml.parserFactory;
    if (parserFactory == null) {
      parserFactory = Xml.parserFactory = DefaultXmlParserFactory.getInstance();
    }
    return parserFactory;
  }

  /**
   * Returns a new XML serializer.
   * 
   * @throws IllegalArgumentException if encountered an
   *         {@link XmlPullParserException}
   */
  public static XmlSerializer createSerializer() {
    try {
      return getParserFactory().createSerializer();
    } catch (XmlPullParserException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** Returns a new XML pull parser. */
  public static XmlPullParser createParser() throws XmlPullParserException {
    XmlPullParser result = getParserFactory().createParser();
    if (!result.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES)) {
      throw new IllegalStateException("XML pull parser must have "
          + "namespace-aware feature");
    }
    // TODO: make the XML pull parser secure
    return result;
  }

  /**
   * Shows a debug string representation of an element data object of key/value
   * pairs.
   * <p>
   * It will make up something for the element name and XML namespaces. If those
   * are known, it is better to use
   * {@link XmlNamespaceDictionary#toStringOf(String, Object)}.
   * 
   * @param element element data object of key/value pairs ({@link GenericXml},
   *        {@link Map}, or any object with public fields)
   */
  public static String toStringOf(Object element) {
    return new XmlNamespaceDictionary().toStringOf(null, element);
  }

  private static void parseValue(String stringValue, Field field,
      Object destination, GenericXml genericXml,
      Map<String, Object> destinationMap, String name) {
    if (field == null) {
      if (genericXml != null) {
        genericXml.set(name, parseValue(stringValue, null));
      } else if (destinationMap != null) {
        destinationMap.put(name, parseValue(stringValue, null));
      }
    } else {
      Class<?> fieldClass = field.getType();
      if (Modifier.isFinal(field.getModifiers())
          && !FieldInfo.isPrimitive(fieldClass)) {
        throw new IllegalArgumentException(
            "final sub-element fields are not supported");
      }
      Object fieldValue = parseValue(stringValue, fieldClass);
      FieldInfo.setFieldValue(field, destination, fieldValue);
    }
  }

  /**
   * Customizes the behavior of XML parsing. Subclasses may override any methods
   * they need to customize behavior.
   */
  public static class CustomizeParser {
    /**
     * Returns whether to stop parsing when reaching the start tag of an XML
     * element before it has been processed. Only called if the element is
     * actually being processed. By default, returns {@code false}, but
     * subclasses may override.
     * 
     * @param namespace XML element's namespace URI
     * @param localName XML element's local name
     */
    public boolean stopBeforeStartTag(String namespace, String localName) {
      return false;
    }

    /**
     * Returns whether to stop parsing when reaching the end tag of an XML
     * element after it has been processed. Only called if the element is
     * actually being processed. By default, returns {@code false}, but
     * subclasses may override.
     * 
     * @param namespace XML element's namespace URI
     * @param localName XML element's local name
     */
    public boolean stopAfterEndTag(String namespace, String localName) {
      return false;
    }
  }

  /**
   * Parses an XML elment using the given XML pull parser into the given
   * destination object.
   * <p>
   * Requires the the current event be {@link XmlPullParser#START_TAG} (skipping
   * any initial {@link XmlPullParser#START_DOCUMENT}) of the element being
   * parsed. At normal parsing completion, the current event will either be
   * {@link XmlPullParser#END_TAG} of the element being parsed, or the
   * {@link XmlPullParser#START_TAG} of the requested {@code atom:entry}.
   * 
   * @param parser XML pull parser
   * @param destination optional destination object to parser into or {@code
   *        null} to ignore XML content
   * @param namespaceDictionary XML namespace dictionary to store unknown
   *        namespaces
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public static void parseElement(XmlPullParser parser, Object destination,
      XmlNamespaceDictionary namespaceDictionary,
      CustomizeParser customizeParser) throws IOException,
      XmlPullParserException {
    parseElementInternal(parser, destination, namespaceDictionary,
        customizeParser);
  }

  /**
   * Returns whether the customize parser has requested to stop or reached end
   * of document. Otherwise, identical to
   * {@link #parseElement(XmlPullParser, Object, XmlNamespaceDictionary, CustomizeParser)}
   * .
   */
  private static boolean parseElementInternal(XmlPullParser parser,
      Object destination, XmlNamespaceDictionary namespaceDictionary,
      CustomizeParser customizeParser) throws IOException,
      XmlPullParserException {
    Class<?> destinationClass =
        destination == null ? null : destination.getClass();
    GenericXml genericXml =
        destination instanceof GenericXml ? (GenericXml) destination : null;
    boolean isMap = genericXml == null && destination instanceof Map<?, ?>;
    @SuppressWarnings("unchecked")
    Map<String, Object> destinationMap =
        isMap ? (Map<String, Object>) destination : null;
    ClassInfo classInfo =
        isMap || destination == null ? null : ClassInfo.of(destinationClass);
    int eventType = parser.getEventType();
    if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
      eventType = parser.next();
    }
    if (eventType != XmlPullParser.START_TAG) {
      throw new IllegalArgumentException("wrong event type: " + eventType);
    }
    // generic XML
    String prefix = parser.getPrefix();
    String alias = prefix == null ? "" : prefix;
    namespaceDictionary.addNamespace(alias, parser.getNamespace());
    // TODO: can instead just look at the xmlns attributes?
    if (genericXml != null) {
      genericXml.namespaceDictionary = namespaceDictionary;
      String name = parser.getName();
      genericXml.name = prefix == null ? name : prefix + ":" + name;
    }
    // attributes
    if (destination != null) {
      int attributeCount = parser.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        String attributeName = parser.getAttributeName(i);
        String attributePrefix = parser.getAttributePrefix(i);
        String attributeNamespace = parser.getAttributeNamespace(i);
        if (attributePrefix != null) {
          namespaceDictionary.addNamespace(attributePrefix, attributeNamespace);
        }
        String fieldName =
            getFieldName(true, attributePrefix, attributeNamespace,
                attributeName);
        Field field = isMap ? null : classInfo.getField(fieldName);
        parseValue(parser.getAttributeValue(i), field, destination, genericXml,
            destinationMap, fieldName);
      }
    }
    Field field;
    while (true) {
      int event = parser.next();
      switch (event) {
        case XmlPullParser.END_DOCUMENT:
          return true;
        case XmlPullParser.END_TAG:
          return customizeParser != null
              && customizeParser.stopAfterEndTag(parser.getNamespace(), parser
                  .getName());
        case XmlPullParser.TEXT:
          // parse text content
          if (destination != null) {
            String textFieldName = "text()";
            field = isMap ? null : classInfo.getField(textFieldName);
            parseValue(parser.getText(), field, destination, genericXml,
                destinationMap, textFieldName);
          }
          break;
        case XmlPullParser.START_TAG:
          if (customizeParser != null
              && customizeParser.stopBeforeStartTag(parser.getNamespace(),
                  parser.getName())) {
            return true;
          }
          if (destination == null) {
            int level = 1;
            while (level != 0) {
              switch (parser.next()) {
                case XmlPullParser.END_DOCUMENT:
                  return true;
                case XmlPullParser.START_TAG:
                  level++;
                  break;
                case XmlPullParser.END_TAG:
                  level--;
                  break;
              }
            }
            continue;
          }
          // element
          String fieldName =
              getFieldName(false, parser.getPrefix(), parser.getNamespace(),
                  parser.getName());
          field = isMap ? null : classInfo.getField(fieldName);
          Class<?> fieldClass = field == null ? null : field.getType();
          boolean isStopped = false;
          if (field == null && !isMap && genericXml == null || field != null
              && FieldInfo.isPrimitive(fieldClass)) {
            int level = 1;
            while (level != 0) {
              switch (parser.next()) {
                case XmlPullParser.END_DOCUMENT:
                  return true;
                case XmlPullParser.START_TAG:
                  level++;
                  break;
                case XmlPullParser.END_TAG:
                  level--;
                  break;
                case XmlPullParser.TEXT:
                  if (level == 1) {
                    parseValue(parser.getText(), field, destination, genericXml,
                        destinationMap, fieldName);
                  }
                  break;
              }
            }
          } else if (field == null || Map.class.isAssignableFrom(fieldClass)) {
            // TODO: handle sub-field type
            Map<String, Object> mapValue = ClassInfo.newMapInstance(fieldClass);
            isStopped =
                parseElementInternal(parser, mapValue, namespaceDictionary,
                    customizeParser);
            if (isMap) {
              @SuppressWarnings("unchecked")
              List<Object> list = (List<Object>) destinationMap.get(fieldName);
              if (list == null) {
                list = new ArrayList<Object>(1);
                destinationMap.put(fieldName, list);
              }
              list.add(mapValue);
            } else if (field != null) {
              FieldInfo.setFieldValue(field, destination, mapValue);
            } else {
              GenericXml atom = (GenericXml) destination;
              @SuppressWarnings("unchecked")
              List<Object> list = (List<Object>) atom.get(fieldName);
              if (list == null) {
                list = new ArrayList<Object>(1);
                atom.set(fieldName, list);
              }
              list.add(mapValue);
            }
          } else if (Collection.class.isAssignableFrom(fieldClass)) {
            @SuppressWarnings("unchecked")
            Collection<Object> collectionValue =
                (Collection<Object>) FieldInfo
                    .getFieldValue(field, destination);
            if (collectionValue == null) {
              collectionValue = ClassInfo.newCollectionInstance(fieldClass);
              FieldInfo.setFieldValue(field, destination, collectionValue);
            }
            Object elementValue = null;
            // TODO: what about Collection<Object> or Collection<?> or
            // Collection<? extends X>?
            Class<?> subFieldClass = ClassInfo.getCollectionParameter(field);
            if (subFieldClass == null || FieldInfo.isPrimitive(subFieldClass)) {
              int level = 1;
              while (level != 0) {
                switch (parser.next()) {
                  case XmlPullParser.END_DOCUMENT:
                    return true;
                  case XmlPullParser.START_TAG:
                    level++;
                    break;
                  case XmlPullParser.END_TAG:
                    level--;
                    break;
                  case XmlPullParser.TEXT:
                    if (level == 1 && subFieldClass != null) {
                      elementValue =
                          parseValue(parser.getText(), subFieldClass);
                    }
                    break;
                }
              }
            } else {
              elementValue = ClassInfo.newInstance(subFieldClass);
              isStopped =
                  parseElementInternal(parser, elementValue,
                      namespaceDictionary, customizeParser);
            }
            collectionValue.add(elementValue);
          } else {
            Object value = ClassInfo.newInstance(fieldClass);
            isStopped =
                parseElementInternal(parser, value, namespaceDictionary,
                    customizeParser);
            FieldInfo.setFieldValue(field, destination, value);
          }
          if (isStopped) {
            return true;
          }
          break;
      }
    }
  }

  private static String getFieldName(boolean isAttribute, String alias,
      String namespace, String name) {
    alias = alias == null ? "" : alias;
    StringBuilder buf = new StringBuilder(2 + alias.length() + name.length());
    if (isAttribute) {
      buf.append('@');
    }
    if (alias != "") {
      buf.append(alias).append(':');
    }
    return buf.append(name).toString();
  }

  private static Object parseValue(String stringValue, Class<?> fieldClass) {
    if (fieldClass == Double.class || fieldClass == double.class) {
      if (stringValue.equals("INF")) {
        return new Double(Double.POSITIVE_INFINITY);
      }
      if (stringValue.equals("-INF")) {
        return new Double(Double.NEGATIVE_INFINITY);
      }
    }
    if (fieldClass == Float.class || fieldClass == float.class) {
      if (stringValue.equals("INF")) {
        return Float.POSITIVE_INFINITY;
      }
      if (stringValue.equals("-INF")) {
        return Float.NEGATIVE_INFINITY;
      }
    }
    return FieldInfo.parsePrimitiveValue(fieldClass, stringValue);
  }

  private Xml() {
  }
}
