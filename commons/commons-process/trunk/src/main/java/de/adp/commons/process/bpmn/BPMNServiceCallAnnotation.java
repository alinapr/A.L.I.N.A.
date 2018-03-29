package de.adp.commons.process.bpmn;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

import de.adp.commons.process.ServiceCallAnnotation;
import de.adp.commons.process.TriggerAnnotation;
/**
 * Implementation for service call annotations in a BPMN model.
 * It is realized as wrapped for the related XML element.
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class BPMNServiceCallAnnotation implements ServiceCallAnnotation {
	private Element serviceCallElement;
	private BPMNTriggerAnnotation triggerAnnotation;
	
	/**
	 * Creates a new service call annotation wrapping the given XML element.
	 * @param serviceCallElement XML element for a <code>seviceCall</cde> as specified in adp:bpmn:annotations.
	 */
	public BPMNServiceCallAnnotation(Element serviceCallElement) {
		this.serviceCallElement = serviceCallElement;
		Element triggerElement = serviceCallElement.getChild("trigger", BPMNNamespace.ADP);
		if (triggerElement != null) {
			triggerAnnotation = new BPMNTriggerAnnotation(triggerElement);
		}
	}
	
	/**
	 * Creates a new service call annotation.
	 * @param serviceId Identifier for the service to call.
	 * @param methodId Method to call.
	 * @param type Type of the service call, i.e. trigger for the call of the service. 
	 */
	public BPMNServiceCallAnnotation(String serviceId, String methodId, Type type) {
		serviceCallElement = new Element("serviceCall", BPMNNamespace.ADP);
		serviceCallElement.setAttribute("service", serviceId);
		serviceCallElement.setAttribute("method", methodId);
		setType(type);
	}
	
	/**
	 * Returns the XML element this wrapper is based on.
	 * @return XML data structure as specified for <code>event</code> in adp:bpmn:annotations.
	 */
	public Element getXMLElement() {
		return serviceCallElement;
	}
	
	/**
	 * Sets a reference for the response of the service call.
	 * The complete response will be stored in the reference. 
	 * @param outputReference Key for an entry in the process context data store to store the data in.
	 */
	public void setOutputReference(String outputReference) {
		serviceCallElement.setAttribute("output", outputReference);
	}
	
	/**
	 * Sets the input mapping for the service call.
	 * @param mapping Map of parameters which are used in the method call, and references to entries in local data stores. 
	 */
	public void setInputMapping(Map<String, String> mapping) {
		Element inputElement = serviceCallElement.getChild("input", BPMNNamespace.ADP);
		if (inputElement != null) {
			inputElement.removeChildren("entry", BPMNNamespace.ADP);
		} else {
			inputElement = new Element("input", BPMNNamespace.ADP);
			Element triggerElement = serviceCallElement.getChild("trigger", BPMNNamespace.ADP);
			int index = triggerElement != null ? 1 : 0;
			serviceCallElement.addContent(index, inputElement);
		}
		for (String key : mapping.keySet()) {
			String reference = mapping.get(key);
			Element entryElement = new Element("entry", BPMNNamespace.ADP);
			entryElement.setAttribute("key", key);
			entryElement.setAttribute("reference", reference);
			inputElement.addContent(entryElement);
		}
	}
	
	/**
	 * Sets the output mapping for the service call.
	 * This allows different fields of the service call response to be mapped to different entries in the process context data store.
	 * @param mapping Map of field names (of the response object) and keys for the process context data store (to store values in). 
	 */
	public void setOutputMapping(Map<String, String> mapping) {
		Element outputElement = serviceCallElement.getChild("output", BPMNNamespace.ADP);
		if (outputElement != null) {
			outputElement.removeChildren("entry", BPMNNamespace.ADP);
		} else {
			outputElement = new Element("output", BPMNNamespace.ADP);
			serviceCallElement.addContent(outputElement);
		}
		for (String key : mapping.keySet()) {
			String reference = mapping.get(key);
			Element entryElement = new Element("entry", BPMNNamespace.ADP);
			entryElement.setAttribute("key", key);
			entryElement.setAttribute("reference", reference);
			outputElement.addContent(entryElement);
		}
	}
	
	/**
	 * Sets the trigger for the service call.
	 * The trigger will replace any existent trigger. The type of the annotation will be changed to {@link Type#TRIGGER}.
	 * @param triggerAnnotation Trigger annotation to set.
	 */
	public void setTrigger(BPMNTriggerAnnotation triggerAnnotation) {
		if (this.triggerAnnotation != null) {
			this.triggerAnnotation.getXMLElement().detach();
		} else {
			this.setType(Type.TRIGGER);
		}
		this.triggerAnnotation = triggerAnnotation;
		serviceCallElement.addContent(0, triggerAnnotation.getXMLElement());
	}

	@Override
	public Map<String, String> getInputMapping() {
		Map<String, String> inputMapping = new HashMap<>();
		Element inputElement = serviceCallElement.getChild("input", BPMNNamespace.ADP);
		if (inputElement != null) {
			for (Element entryElement : inputElement.getChildren("entry", BPMNNamespace.ADP)) {
				String key = entryElement.getAttributeValue("key");
				String reference = entryElement.getAttributeValue("reference");
				inputMapping.put(key, reference);
			}
		}
		return inputMapping;
	}

	@Override
	public String getMethod() {
		return serviceCallElement.getAttributeValue("method");
	}

	@Override
	public Map<String, String> getOutputMapping() {
		Map<String, String> outputMapping = new HashMap<>();
		Element outputElement = serviceCallElement.getChild("output", BPMNNamespace.ADP);
		if (outputElement != null) {
			for (Element entryElement : outputElement.getChildren("entry", BPMNNamespace.ADP)) {
				String key = entryElement.getAttributeValue("key");
				String reference = entryElement.getAttributeValue("reference");
				outputMapping.put(key, reference);
			}
		}
		return outputMapping;
	}

	@Override
	public String getOutputReference() {
		return serviceCallElement.getAttributeValue("output");
	}

	@Override
	public String getService() {
		return serviceCallElement.getAttributeValue("service");
	}

	@Override
	public TriggerAnnotation getTrigger() {
		return triggerAnnotation;
	}
	
	/**
	 * Sets the type of the service call.
	 * A service call can be performed when
	 * - the annotated element is called
	 * - the annotated element is completed
	 * - a event occurs during the execution of the element 
	 * @param type Type to set.
	 */
	private void setType(Type type) {
		switch (type) {
		case START:
			serviceCallElement.setAttribute("type", "onStart");
			break;
		case END:
			serviceCallElement.setAttribute("type", "onEnd");
			break;
		case TRIGGER:
			serviceCallElement.setAttribute("type", "onTrigger");
			break;
		}
	}

	@Override
	public Type getType() {
		switch (serviceCallElement.getAttributeValue("type")) {
		case "onStart":
			return Type.START;
		case "onEnd":
			return Type.END;
		case "onTrigger":
			return Type.TRIGGER;
		}
		return null;
	}
}