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

package com.google.api.client.xml;

import com.google.api.client.util.ArrayValueMap;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Types;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

  /** Text content. */
  static final String TEXT_CONTENT = "text()";

  /** XML pull parser factory. */
  private static XmlPullParserFactory factory;

  private static synchronized XmlPullParserFactory getParserFactory()
      throws XmlPullParserException {
    if (factory == null) {
      factory = XmlPullParserFactory.newInstance(
          System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
      factory.setNamespaceAware(true);
    }
    return factory;

  }

  /**
   * Returns a new XML serializer.
   *
   * @throws IllegalArgumentException if encountered an {@link XmlPullParserException}
   */
  public static XmlSerializer createSerializer() {
    try {
      return getParserFactory().newSerializer();
    } catch (XmlPullParserException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** Returns a new XML pull parser. */
  public static XmlPullParser createParser() throws XmlPullParserException {
    return getParserFactory().newPullParser();
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   * <p>
   * It will make up something for the element name and XML namespaces. If those are known, it is
   * better to use {@link XmlNamespaceDictionary#toStringOf(String, Object)}.
   *
   * @param element element data object of key/value pairs ({@link GenericXml}, {@link Map}, or any
   *        object with public fields)
   */
  public static String toStringOf(Object element) {
    return new XmlNamespaceDictionary().toStringOf(null, element);
  }

  /**
   * Parses the string value of an attribute value or text content.
   *
   * @param stringValue string value
   * @param field field to set or {@code null} if not applicable
   * @param valueType value type (class, parameterized type, or generic array type) or {@code null}
   *        for none
   * @param context context list, going from least specific to most specific type context, for
   *        example container class and its field
   * @param destination destination object or {@code null} for none
   * @param genericXml generic XML or {@code null} if not applicable
   * @param destinationMap destination map or {@code null} if not applicable
   * @param name key name
   */
  private static void parseAttributeOrTextContent(String stringValue,
      Field field,
      Type valueType,
      List<Type> context,
      Object destination,
      GenericXml genericXml,
      Map<String, Object> destinationMap,
      String name) {
    if (field != null || genericXml != null || destinationMap != null) {
      valueType = field == null ? valueType : field.getGenericType();
      Object value = parseValue(valueType, context, stringValue);
      setValue(value, field, destination, genericXml, destinationMap, name);
    }
  }

  /**
   * Sets the value of a given field or map entry.
   *
   * @param value value
   * @param field field to set or {@code null} if not applicable
   * @param destination destination object or {@code null} for none
   * @param genericXml generic XML or {@code null} if not applicable
   * @param destinationMap destination map or {@code null} if not applicable
   * @param name key name
   */
  private static void setValue(Object value,
      Field field,
      Object destination,
      GenericXml genericXml,
      Map<String, Object> destinationMap,
      String name) {
    if (field != null) {
      FieldInfo.setFieldValue(field, destination, value);
    } else if (genericXml != null) {
      genericXml.set(name, value);
    } else {
      destinationMap.put(name, value);
    }
  }

  /**
   * Customizes the behavior of XML parsing. Subclasses may override any methods they need to
   * customize behavior.
   */
  public static class CustomizeParser {
    /**
     * Returns whether to stop parsing when reaching the start tag of an XML element before it has
     * been processed. Only called if the element is actually being processed. By default, returns
     * {@code false}, but subclasses may override.
     *
     * @param namespace XML element's namespace URI
     * @param localName XML element's local name
     */
    public boolean stopBeforeStartTag(String namespace, String localName) {
      return false;
    }

    /**
     * Returns whether to stop parsing when reaching the end tag of an XML element after it has been
     * processed. Only called if the element is actually being processed. By default, returns {@code
     * false}, but subclasses may override.
     *
     * @param namespace XML element's namespace URI
     * @param localName XML element's local name
     */
    public boolean stopAfterEndTag(String namespace, String localName) {
      return false;
    }
  }

  /**
   * Parses an XML element using the given XML pull parser into the given destination object.
   *
   * <p>
   * Requires the the current event be {@link XmlPullParser#START_TAG} (skipping any initial
   * {@link XmlPullParser#START_DOCUMENT}) of the element being parsed. At normal parsing
   * completion, the current event will either be {@link XmlPullParser#END_TAG} of the element being
   * parsed, or the {@link XmlPullParser#START_TAG} of the requested {@code atom:entry}.
   * </p>
   *
   * @param parser XML pull parser
   * @param destination optional destination object to parser into or {@code null} to ignore XML
   *        content
   * @param namespaceDictionary XML namespace dictionary to store unknown namespaces
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public static void parseElement(XmlPullParser parser, Object destination,
      XmlNamespaceDictionary namespaceDictionary, CustomizeParser customizeParser)
      throws IOException, XmlPullParserException {
    ArrayList<Type> context = new ArrayList<Type>();
    context.add(destination.getClass());
    parseElementInternal(parser, context, destination, null, namespaceDictionary, customizeParser);
  }

  /**
   * Returns whether the customize parser has requested to stop or reached end of document.
   * Otherwise, identical to
   * {@link #parseElement(XmlPullParser, Object, XmlNamespaceDictionary, CustomizeParser)} .
   */
  private static boolean parseElementInternal(XmlPullParser parser,
      ArrayList<Type> context,
      Object destination,
      Type valueType,
      XmlNamespaceDictionary namespaceDictionary,
      CustomizeParser customizeParser) throws IOException, XmlPullParserException {
    // TODO(yanivi): method is too long; needs to be broken down into smaller methods and comment
    // better
    GenericXml genericXml = destination instanceof GenericXml ? (GenericXml) destination : null;
    @SuppressWarnings("unchecked")
    Map<String, Object> destinationMap =
        genericXml == null && destination instanceof Map<?, ?> ? Map.class.cast(destination) : null;
    ClassInfo classInfo =
        destinationMap != null || destination == null ? null : ClassInfo.of(destination.getClass());
    int eventType = parser.getEventType();
    if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
      eventType = parser.next();
    }
    if (eventType != XmlPullParser.START_TAG) {
      throw new IllegalArgumentException(
          "expected start of XML element, but got something else (event type " + eventType + ")");
    }
    // read namespaces declared on this XML element
    int depth = parser.getDepth();
    int nsStart = parser.getNamespaceCount(depth - 1);
    int nsEnd = parser.getNamespaceCount(depth);
    for (int i = nsStart; i < nsEnd; i++) {
      String namespace = parser.getNamespaceUri(i);
      // if namespace isn't already in our dictionary, add it now
      if (namespaceDictionary.getAliasForUri(namespace) == null) {
        String prefix = parser.getNamespacePrefix(i);
        String originalAlias = prefix == null ? "" : prefix;
        // find an available alias
        String alias = originalAlias;
        int suffix = 1;
        while (namespaceDictionary.getUriForAlias(alias) != null) {
          suffix++;
          alias = originalAlias + suffix;
        }
        namespaceDictionary.set(alias, namespace);
      }
    }
    // generic XML
    if (genericXml != null) {
      genericXml.namespaceDictionary = namespaceDictionary;
      String name = parser.getName();
      String namespace = parser.getNamespace();
      String alias = namespaceDictionary.getAliasForUri(namespace);
      genericXml.name = alias.length() == 0 ? name : alias + ":" + name;
    }
    // attributes
    if (destination != null) {
      int attributeCount = parser.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        // TODO(yanivi): can have repeating attribute values, e.g. "@a=value1 @a=value2"?
        String attributeName = parser.getAttributeName(i);
        String attributeNamespace = parser.getAttributeNamespace(i);
        String attributeAlias =
            attributeNamespace.length() == 0 ? "" : namespaceDictionary.getAliasForUri(
                attributeNamespace);
        String fieldName = getFieldName(true, attributeAlias, attributeNamespace, attributeName);
        Field field = classInfo == null ? null : classInfo.getField(fieldName);
        parseAttributeOrTextContent(parser.getAttributeValue(i),
            field,
            valueType,
            context,
            destination,
            genericXml,
            destinationMap,
            fieldName);
      }
    }
    Field field;
    ArrayValueMap arrayValueMap = new ArrayValueMap(destination);
    boolean isStopped = false;
    main: while (true) {
      int event = parser.next();
      switch (event) {
        case XmlPullParser.END_DOCUMENT:
          isStopped = true;
          break main;
        case XmlPullParser.END_TAG:
          isStopped = customizeParser != null
              && customizeParser.stopAfterEndTag(parser.getNamespace(), parser.getName());
          break main;
        case XmlPullParser.TEXT:
          // parse text content
          if (destination != null) {
            field = classInfo == null ? null : classInfo.getField(TEXT_CONTENT);
            parseAttributeOrTextContent(parser.getText(),
                field,
                valueType,
                context,
                destination,
                genericXml,
                destinationMap,
                TEXT_CONTENT);
          }
          break;
        case XmlPullParser.START_TAG:
          if (customizeParser != null
              && customizeParser.stopBeforeStartTag(parser.getNamespace(), parser.getName())) {
            isStopped = true;
            break main;
          }
          if (destination == null) {
            parseTextContentForElement(parser, context, true, null);
          } else {
            // element
            String namespace = parser.getNamespace();
            String alias = namespaceDictionary.getAliasForUri(namespace);
            String fieldName = getFieldName(false, alias, namespace, parser.getName());
            field = classInfo == null ? null : classInfo.getField(fieldName);
            Type fieldType = field == null ? valueType : field.getGenericType();
            fieldType = Data.resolveWildcardTypeOrTypeVariable(context, fieldType);
            // field type is now class, parameterized type, or generic array type
            // resolve a parameterized type to a class
            Class<?> fieldClass = fieldType instanceof Class<?> ? (Class<?>) fieldType : null;
            if (fieldType instanceof ParameterizedType) {
              fieldClass = Types.getRawClass((ParameterizedType) fieldType);
            }
            boolean isArray = Types.isArray(fieldType);
            // text content
            boolean ignore = field == null && destinationMap == null && genericXml == null;
            if (ignore || Data.isPrimitive(fieldType)) {
              int level = 1;
              while (level != 0) {
                switch (parser.next()) {
                  case XmlPullParser.END_DOCUMENT:
                    isStopped = true;
                    break main;
                  case XmlPullParser.START_TAG:
                    level++;
                    break;
                  case XmlPullParser.END_TAG:
                    level--;
                    break;
                  case XmlPullParser.TEXT:
                    if (!ignore && level == 1) {
                      parseAttributeOrTextContent(parser.getText(),
                          field,
                          valueType,
                          context,
                          destination,
                          genericXml,
                          destinationMap,
                          fieldName);
                    }
                    break;
                }
              }
            } else if (fieldType == null || fieldClass != null
                && Types.isAssignableToOrFrom(fieldClass, Map.class)) {
              // store the element as a map
              Map<String, Object> mapValue = Data.newMapInstance(fieldClass);
              int contextSize = context.size();
              if (fieldType != null) {
                context.add(fieldType);
              }
              Type subValueType = fieldType != null && Map.class.isAssignableFrom(fieldClass)
                  ? Types.getMapValueParameter(fieldType) : null;
              subValueType = Data.resolveWildcardTypeOrTypeVariable(context, subValueType);
              isStopped = parseElementInternal(parser,
                  context,
                  mapValue,
                  subValueType,
                  namespaceDictionary,
                  customizeParser);
              if (fieldType != null) {
                context.remove(contextSize);
              }
              if (destinationMap != null) {
                // map but not GenericXml: store as ArrayList of elements
                @SuppressWarnings("unchecked")
                Collection<Object> list = (Collection<Object>) destinationMap.get(fieldName);
                if (list == null) {
                  list = new ArrayList<Object>(1);
                  destinationMap.put(fieldName, list);
                }
                list.add(mapValue);
              } else if (field != null) {
                // not a map: store in field value
                FieldInfo fieldInfo = FieldInfo.of(field);
                if (fieldClass == Object.class) {
                  // field is an Object: store as ArrayList of element maps
                  @SuppressWarnings("unchecked")
                  Collection<Object> list = (Collection<Object>) fieldInfo.getValue(destination);
                  if (list == null) {
                    list = new ArrayList<Object>(1);
                    fieldInfo.setValue(destination, list);
                  }
                  list.add(mapValue);
                } else {
                  // field is a Map: store as a single element map
                  fieldInfo.setValue(destination, mapValue);
                }
              } else {
                // GenericXml: store as ArrayList of elements
                GenericXml atom = (GenericXml) destination;
                @SuppressWarnings("unchecked")
                Collection<Object> list = (Collection<Object>) atom.get(fieldName);
                if (list == null) {
                  list = new ArrayList<Object>(1);
                  atom.set(fieldName, list);
                }
                list.add(mapValue);
              }
            } else if (isArray || Types.isAssignableToOrFrom(fieldClass, Collection.class)) {
              // TODO(yanivi): some duplicate code here; isolate into reusable methods
              FieldInfo fieldInfo = FieldInfo.of(field);
              Object elementValue = null;
              Type subFieldType =
                  isArray ? Types.getArrayComponentType(fieldType) : Types.getIterableParameter(
                      fieldType);
              Class<?> rawArrayComponentType =
                  Types.getRawArrayComponentType(context, subFieldType);
              subFieldType = Data.resolveWildcardTypeOrTypeVariable(context, subFieldType);
              Class<?> subFieldClass =
                  subFieldType instanceof Class<?> ? (Class<?>) subFieldType : null;
              if (subFieldType instanceof ParameterizedType) {
                subFieldClass = Types.getRawClass((ParameterizedType) subFieldType);
              }
              if (Data.isPrimitive(subFieldType)) {
                elementValue = parseTextContentForElement(parser, context, false, subFieldType);
              } else if (subFieldType == null || subFieldClass != null
                  && Types.isAssignableToOrFrom(subFieldClass, Map.class)) {
                elementValue = Data.newMapInstance(subFieldClass);
                int contextSize = context.size();
                if (subFieldType != null) {
                  context.add(subFieldType);
                }
                Type subValueType =
                    subFieldType != null && Map.class.isAssignableFrom(subFieldClass)
                        ? Types.getMapValueParameter(subFieldType) : null;
                subValueType = Data.resolveWildcardTypeOrTypeVariable(context, subValueType);
                isStopped = parseElementInternal(parser,
                    context,
                    elementValue,
                    subValueType,
                    namespaceDictionary,
                    customizeParser);
                if (subFieldType != null) {
                  context.remove(contextSize);
                }
              } else {
                elementValue = Types.newInstance(rawArrayComponentType);
                int contextSize = context.size();
                context.add(fieldType);
                isStopped = parseElementInternal(parser,
                    context,
                    elementValue,
                    null,
                    namespaceDictionary,
                    customizeParser);
                context.remove(contextSize);
              }
              if (isArray) {
                // array field: add new element to array value map
                if (field == null) {
                  arrayValueMap.put(fieldName, rawArrayComponentType, elementValue);
                } else {
                  arrayValueMap.put(field, rawArrayComponentType, elementValue);
                }
              } else {
                // collection: add new element to collection
                @SuppressWarnings("unchecked")
                Collection<Object> collectionValue = (Collection<Object>) (field == null
                    ? destinationMap.get(fieldName) : fieldInfo.getValue(destination));
                if (collectionValue == null) {
                  collectionValue = Data.newCollectionInstance(fieldType);
                  setValue(collectionValue,
                      field,
                      destination,
                      genericXml,
                      destinationMap,
                      fieldName);
                }
                collectionValue.add(elementValue);
              }
            } else {
              // not an array/iterable or a map, but we do have a field
              Object value = Types.newInstance(fieldClass);
              int contextSize = context.size();
              context.add(fieldType);
              isStopped = parseElementInternal(parser,
                  context,
                  value,
                  null,
                  namespaceDictionary,
                  customizeParser);
              context.remove(contextSize);
              setValue(value, field, destination, genericXml, destinationMap, fieldName);
            }
          }
          if (isStopped || parser.getEventType() == XmlPullParser.END_DOCUMENT) {
            isStopped = true;
            break main;
          }
          break;
      }
    }
    arrayValueMap.setValues();
    return isStopped;
  }

  private static String getFieldName(
      boolean isAttribute, String alias, String namespace, String name) {
    if (!isAttribute && alias.length() == 0) {
      return name;
    }
    StringBuilder buf = new StringBuilder(2 + alias.length() + name.length());
    if (isAttribute) {
      buf.append('@');
    }
    if (alias != "") {
      buf.append(alias).append(':');
    }
    return buf.append(name).toString();
  }

  private static Object parseTextContentForElement(
      XmlPullParser parser, List<Type> context, boolean ignoreTextContent, Type textContentType)
      throws XmlPullParserException, IOException {
    Object result = null;
    int level = 1;
    while (level != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_DOCUMENT:
          level = 0;
          break;
        case XmlPullParser.START_TAG:
          level++;
          break;
        case XmlPullParser.END_TAG:
          level--;
          break;
        case XmlPullParser.TEXT:
          if (!ignoreTextContent && level == 1) {
            result = parseValue(textContentType, context, parser.getText());
          }
          break;
      }
    }
    return result;
  }

  private static Object parseValue(Type valueType, List<Type> context, String value) {
    valueType = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    if (valueType == Double.class || valueType == double.class) {
      if (value.equals("INF")) {
        return new Double(Double.POSITIVE_INFINITY);
      }
      if (value.equals("-INF")) {
        return new Double(Double.NEGATIVE_INFINITY);
      }
    }
    if (valueType == Float.class || valueType == float.class) {
      if (value.equals("INF")) {
        return Float.POSITIVE_INFINITY;
      }
      if (value.equals("-INF")) {
        return Float.NEGATIVE_INFINITY;
      }
    }
    return Data.parsePrimitiveValue(valueType, value);
  }

  private Xml() {
  }
}
