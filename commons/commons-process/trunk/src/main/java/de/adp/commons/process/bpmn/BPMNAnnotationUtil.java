package de.adp.commons.process.bpmn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

/**
 * Utility class for annotating BPMN process elements.
 * @author simon.schwantzer(at)im-c.de
 */
public final class BPMNAnnotationUtil {
	private BPMNAnnotationUtil(){};
	
	/**
	 * Encodes a (recursive) map into a XML element.
	 * @param fields Map of keys and values. Valid values are of type {@link String}, {@link List}, or {@link Map}. These restrictions hold recursively.
	 * @param elementName Name for the XML element generated.
	 * @return XML element as specified in adp:bpmn:annotations to be used as event properties or trigger references.
	 */
	protected static Element encodeObject(Map<String, Object> fields, String elementName) {
		Element objectElement = new Element(elementName, BPMNNamespace.ADP);
		for (String key : fields.keySet()) {
			Object reference = fields.get(key);
			Element entryElement;
			if (reference instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String,Object>) reference;
				entryElement = encodeObject(map, "object");
			} else if (reference instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) reference;
				entryElement = encodeArray(list);
			} else {
				entryElement = new Element("entry", BPMNNamespace.ADP);
				entryElement.setAttribute("reference", reference.toString());
			}
			entryElement.setAttribute("key", key);
			objectElement.addContent(entryElement);
		}
		
		return objectElement;
	}
	
	/**
	 * Decodes an XML element to a map of keys and references.
	 * @param objectElement XML element as specified in adp:bpmn:annotations as part of event properties and trigger references.
	 * @return Map of keys and references of type {@link String}, {@link List}, or {@link Map}. This restrictions hold recursively.
	 */
	protected static Map<String, Object> decodeObject(Element objectElement) {
		Map<String, Object> object = new LinkedHashMap<>();
		for (Element entryElement : objectElement.getChildren()) {
			String key = entryElement.getAttributeValue("key");
			switch (entryElement.getName()) {
			case "entry":
				object.put(key, entryElement.getAttributeValue("reference"));
				break;
			case "object":
				object.put(key, decodeObject(entryElement));
				break;
			case "array":
				object.put(key, decodeArray(entryElement));
				break;
			}
		}
		
		return object;
	}
	
	/**
	 * Decodes a XML element representing a list of references-
	 * @param arrayElement XML element as specified in adp:bpmn:annotations as part of event properties and trigger references.
	 * @return List of references of type {@link String}, {@link List}, or {@link Map}. This restrictions  hold recursively.
	 */
	protected static List<Object> decodeArray(Element arrayElement) {
		List<Object> list = new ArrayList<>();
		for (Element entryElement : arrayElement.getChildren()) {
			switch (entryElement.getName()) {
			case "entry":
				list.add(entryElement.getAttributeValue("reference"));
				break;
			case "object":
				list.add(decodeObject(entryElement));
				break;
			case "array":
				list.add(decodeArray(entryElement));
				break;
			}
		}
		return list;
	}
	
	/**
	 * Encodes an array as XML element.
	 * @param entries List of objects of the type {@link String}, {@link List}, or {@link Map}. These restrictions hold recursively.
	 * @return XML element as specified in adp:bpmn:annotations to be used as event properties or trigger references.
	 */
	protected static Element encodeArray(List<Object> entries) {
		Element arrayElement = new Element("array", BPMNNamespace.ADP);
		for (Object entry : entries) {
			Element entryElement;
			if (entry instanceof Map<?,?>) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) entry;
				entryElement = encodeObject(map, "object");
			} else if (entry instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) entry;
				entryElement = encodeArray(list);
			} else {
				entryElement = new Element("entry", BPMNNamespace.ADP);
				entryElement.setAttribute("reference", entry.toString());
			}
			arrayElement.addContent(entryElement);
		}
		
		return arrayElement;
	}
}