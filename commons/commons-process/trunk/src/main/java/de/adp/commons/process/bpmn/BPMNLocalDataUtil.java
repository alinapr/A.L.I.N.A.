package de.adp.commons.process.bpmn;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

public final class BPMNLocalDataUtil {
	private BPMNLocalDataUtil(){};
	
	/**
	 * Retrieves the string value from a XML element.
	 * @param stringElement Element to parse.
	 * @return String encoded in the text node of the element.
	 */
	public static String parseString(Element stringElement) {
		return stringElement.getText();
	}
	
	/**
	 * Encodes a string as XML element.
	 * @param s String to encode.
	 * @return XML element as specified in localData of "adp:bpmn:annotations".
	 */
	public static Element encodeString(String s) {
		Element element = new Element("string", BPMNNamespace.ADP);
		element.setText(s);
		return element;
	}
	
	/**
	 * Retrieves the number from a XML element.
	 * @param numberElement Element to parse.
	 * @return Number encoded in the text node of the element.
	 */
	public static Number parseNumber(Element numberElement) {
		String valueString = numberElement.getText();
		try {
			return NumberFormat.getInstance().parse(valueString);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Failed to parse number.", e);
		}
	}
	
	/**
	 * Encodes a number as XML element.
	 * @param number Number to encode.
	 * @return XML element as specified in localData of "adp:bpmn:annotations".
	 */
	public static Element encodeNumber(Number number) {
		Element element = new Element("number", BPMNNamespace.ADP);
		element.setText(number.toString());
		return element;
	}
	
	/**
	 * Retrieves the boolean value from a XML element.
	 * @param booleanElement Element to parse.
	 * @return Boolean encoded in the text node of the element.
	 */
	public static Boolean parseBoolean(Element booleanElement) {
		String valueString = booleanElement.getText();
		return Boolean.parseBoolean(valueString);
	}
	
	/**
	 * Encodes a boolean as XML element.
	 * @param b Boolean to encode.
	 * @return XML element as specified in localData of "adp:bpmn:annotations".
	 */
	public static Element encodeBoolean(Boolean b) {
		Element element = new Element("boolean", BPMNNamespace.ADP);
		element.setText(b.toString());
		return element;
	}
	
	/**
	 * Retrieves a list of objects from a XML element.
	 * The array may contain values of different types. Valid types are {@link String}, {@link Boolean}, {@link Number}, {@link DataStore}, and {@link List}.
	 * @param arrayElement XML <code>array</code> structure as specified for localData in adp:bpmn:annotations.
	 * @return List of objects encoded in the XML data structure.
	 */
	public static List<Object> parseArray(Element arrayElement) {
		List<Object> objectList = new ArrayList<>();
		for (Element entryElement : arrayElement.getChildren()) {
			switch (entryElement.getName()) {
			case "string":
				objectList.add(parseString(entryElement));
				break;
			case "number":
				objectList.add(parseNumber(entryElement));
				break;
			case "boolean":
				objectList.add(parseBoolean(entryElement));
				break;
			case "array":
				objectList.add(parseArray(entryElement));
				break;
			case "object":
				objectList.add(parseObject(entryElement));
			}
		}
		return objectList;
	}
	
	/**
	 * Encodes an array as XML element. 
	 * @param array List of objects to encode. Valid object types are {@link String}, {@link Number}, {@link Boolean}, {@link List} and {@link Map}.
	 * @return XML element as specified in localData of "adp:bpmn:annotations".
	 * @throws IllegalArgumentException At least one entry of the list has an invalid type.
	 */
	@SuppressWarnings("unchecked")
	public static Element encodeArray(List<Object> array) throws IllegalArgumentException {
		Element arrayElement = new Element("array", BPMNNamespace.ADP);
		for (Object object : array) {
			Element objectElement;
			if (object instanceof String) {
				objectElement = encodeString((String) object);
			} else if (object instanceof Number) {
				objectElement = encodeNumber((Number) object);
			} else if (object instanceof Boolean) {
				objectElement = encodeBoolean((Boolean) object);
			} else if (object instanceof List<?>) {
				objectElement = encodeArray((List<Object>) object);
			} else if (object instanceof Map<?,?>) {
				objectElement = encodeObject((Map<String, Object>) object);
			} else {
				throw new IllegalArgumentException("Unsupported type for data array.");
			}
			arrayElement.addContent(objectElement);
		}
		return arrayElement;
	}

	/**
	 * Retrieves a map from a XML element.
	 * @param objectElement XML element as specified in localData of "adp:bpmn:annotations".
	 * @return Map encoded in the XML element.
	 */
	public static Map<String, Object> parseObject(Element objectElement) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (Element entryElement : objectElement.getChildren()) {
			String key = entryElement.getAttributeValue("key");
			switch (entryElement.getName()) {
			case "string":
				map.put(key, parseString(entryElement));
				break;
			case "number":
				map.put(key, parseNumber(entryElement));
				break;
			case "boolean":
				map.put(key, parseBoolean(entryElement));
				break;
			case "array":
				map.put(key, parseArray(entryElement));
				break;
			case "object":
				map.put(key, parseObject(entryElement));
			}
		}
		return map;
	}
	
	/**
	 * Encodes a map as XML element.
	 * @param map Map with values of type {@link String}, {@link Boolean}, {@link Number}, {@link List}, or {@link Map}.
	 * @return XML element as specified in localData of "adp:bpmn:annotations".
	 * @throws IllegalArgumentException At least one of the map values has an invalid type.  
	 */
	@SuppressWarnings("unchecked")
	public static Element encodeObject(Map<String, Object> map) throws IllegalArgumentException {
		Element objectElement = new Element("object", BPMNNamespace.ADP);
		for (String key : map.keySet()) {
			Object object = map.get(key);
			Element childElement;
			if (object instanceof String) {
				childElement = encodeString((String) object);
			} else if (object instanceof Number) {
				childElement = encodeNumber((Number) object);
			} else if (object instanceof Boolean) {
				childElement = encodeBoolean((Boolean) object);
			} else if (object instanceof List<?>) {
				childElement = encodeArray((List<Object>) object);
			} else if (object instanceof Map<?,?>) {
				childElement = encodeObject((Map<String, Object>) object);
			} else {
				throw new IllegalArgumentException("Unsupported type for data array.");
			}
			childElement.setAttribute(key, "key");
			objectElement.addContent(childElement);
		}
		return objectElement;
	}
}
