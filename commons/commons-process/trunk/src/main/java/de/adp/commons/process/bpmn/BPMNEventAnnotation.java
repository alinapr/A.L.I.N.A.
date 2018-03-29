package de.adp.commons.process.bpmn;

import java.util.Map;

import org.jdom2.Element;

import de.adp.commons.process.EventAnnotation;

/**
 * Implementation for event annotations in a BPMN model.
 * It is realized as wrapped for the related XML element.
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class BPMNEventAnnotation implements EventAnnotation {
	private Element eventElement;
	/**
	 * Wraps the given event element.
	 * @param eventElement XML element as specified in adp:bpmn:annotations for events.
	 */
	public BPMNEventAnnotation(Element eventElement) {
		this.eventElement = eventElement;
	}
	
	/**
	 * Creates a new event annotation.
	 * @param eventId Identifier of the event to be instantiated.
	 * @param eventType Type of the event, i.e. when the event should be generated.
	 * @param properties Properties for the payload of the event.
	 */
	public BPMNEventAnnotation(String eventId, Type eventType, Map<String, Object> properties) {
		eventElement = new Element("event", BPMNNamespace.ADP);
		eventElement.setAttribute("eventId", eventId);
		switch (eventType) {
		case START:
			eventElement.setAttribute("type", "onStart");
			break;
		case END:
			eventElement.setAttribute("type", "onEnd");
			break;
		}
		eventElement.addContent(BPMNAnnotationUtil.encodeObject(properties, "properties"));
	}

	/**
	 * Returns the XML element this wrapper is based on.
	 * @return XML data structure as specified for <code>event</code> in adp:bpmn:annotations.
	 */
	public Element getXMLElement() {
		return eventElement;
	}

	@Override
	public String getEventId() {
		return eventElement.getAttributeValue("eventId");
	}

	@Override
	public Map<String, Object> getProperties() {
		Element propertiesElement = eventElement.getChild("properties", BPMNNamespace.ADP);
		return BPMNAnnotationUtil.decodeObject(propertiesElement);
	}

	@Override
	public Type getType() {
		String typeString = eventElement.getAttributeValue("type");
		switch (typeString) {
		case "onStart":
			return Type.START;
		case "onEnd":
			return Type.END;
		}
		return null;
	}

}
