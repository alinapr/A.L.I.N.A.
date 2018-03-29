package de.adp.commons.process.bpmn;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

import de.adp.commons.process.TriggerAnnotation;

/**
 * Wrapper for BPMN annotations as specified in adp:bpmn:annotations for <code>trigger</code> elements. 
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNTriggerAnnotation implements TriggerAnnotation {
	private Element triggerElement;
	
	/**
	 * Wraps the given XML element. 
	 * @param triggerElement XML element as specified in adp:bpmn:annotations for <code>trigger</code> elements.
	 */
	public BPMNTriggerAnnotation(Element triggerElement) {
		this.triggerElement = triggerElement;
	}
	
	public BPMNTriggerAnnotation(String eventId, Map<String, Object> references) {
		triggerElement = new Element("trigger", BPMNNamespace.ADP);
		triggerElement.setAttribute("eventId", eventId);
		if (references != null && !references.isEmpty()) {
			Element referencesElement = BPMNAnnotationUtil.encodeObject(references, "references");
			triggerElement.addContent(referencesElement);
		}
	}
	
	/**
	 * Returns the XML element this wrapper is based on.
	 * @return XML data structure as specified for <code>trigger</code> in adp:bpmn:annotations.
	 */
	public Element getXMLElement() {
		return triggerElement;
	}

	@Override
	public String getEventId() {
		return triggerElement.getAttributeValue("eventId");
	}

	@Override
	public Map<String, Object> getReferences() {
		Map<String, Object> references;
		Element referencesElement = triggerElement.getChild("references", BPMNNamespace.ADP);
		if (referencesElement != null) {
			references = BPMNAnnotationUtil.decodeObject(referencesElement);
		} else {
			references = new HashMap<String, Object>();
		}
		return references;
	}

}
