package de.adp.commons.process.bpmn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import de.adp.commons.process.Annotations;

/**
 * Wrapper for BPMN annotations specified in adp:bpmn:annotations.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNAnnotations implements Annotations {
	private Element extensionElementsElement, eventsElement, serviceCallsElement, triggersElement;
	private Map<String, Object> localData;
	private List<BPMNEventAnnotation> eventAnnotations;
	private List<BPMNServiceCallAnnotation> serviceCallAnnotations;
	private List<BPMNTriggerAnnotation> triggerAnnotations;
	
	/**
	 * Parses a BPMN <code>extensionElements</code> element to retrieve annotations.  
	 * @param extensionElements BPMN <code>extensionElements</code> XML element.
	 */
	public BPMNAnnotations(Element extensionElementsElement) {
		this.extensionElementsElement = extensionElementsElement;
		eventAnnotations = new ArrayList<>();
		serviceCallAnnotations = new ArrayList<>();
		triggerAnnotations = new ArrayList<>();
		
		for (Element extensionElement : extensionElementsElement.getChildren()) {
			if (extensionElement.getNamespace() != BPMNNamespace.ADP) {
				continue;
			}
			switch (extensionElement.getName()) {
			case "localData":
				localData = BPMNLocalDataUtil.parseObject(extensionElement);
				break;
			case "events":
				eventsElement = extensionElement;
				for (Element eventElement : eventsElement.getChildren("event", BPMNNamespace.ADP)) {
					eventAnnotations.add(new BPMNEventAnnotation(eventElement));
				}
				break;
			case "serviceCalls":
				serviceCallsElement = extensionElement;
				for (Element serviceCallElement : serviceCallsElement.getChildren("serviceCall", BPMNNamespace.ADP)) {
					serviceCallAnnotations.add(new BPMNServiceCallAnnotation(serviceCallElement));
				}
				break;
			case "triggers":
				triggersElement = extensionElement;
				for (Element triggerElement : triggersElement.getChildren("trigger", BPMNNamespace.ADP)) {
					triggerAnnotations.add(new BPMNTriggerAnnotation(triggerElement));
				}
				break;
			}
		}
		
		if (localData == null) {
			localData = new LinkedHashMap<>();
		}
		if (eventsElement == null) {
			eventsElement = new Element("events", BPMNNamespace.ADP);
		}
		if (serviceCallsElement == null) {
			serviceCallsElement = new Element("serviceCalls", BPMNNamespace.ADP);
		}
		if (triggersElement == null) {
			triggersElement = new Element("triggers", BPMNNamespace.ADP);
		}
		
	}
	
	@Override
	public List<BPMNEventAnnotation> getEventAnnotations() {
		return Collections.unmodifiableList(eventAnnotations);
	}
	
	/**
	 * Adds an event annotation.
	 * If the event annotation already exists, nothing is changed.
	 * @param eventAnnotation Event annotation to add.
	 */
	public void addEventAnnotation(BPMNEventAnnotation eventAnnotation) {
		if (eventAnnotations.contains(eventAnnotation)) return;
		if (eventAnnotations.isEmpty()) {
			eventsElement = new Element("events", BPMNNamespace.ADP);
			extensionElementsElement.addContent(eventsElement);
		}
		Element eventElement = eventAnnotation.getXMLElement(); 
		eventsElement.addContent(eventElement);
		eventAnnotations.add(eventAnnotation);
	}
	
	/**
	 * Removes an event annotation.
	 * @param eventAnnotation Event annotation to remove.
	 */
	public void removeEventAnnotation(BPMNEventAnnotation eventAnnotation) {
		boolean isRemoved = eventAnnotations.remove(eventAnnotation);
		if (isRemoved) {
			eventAnnotation.getXMLElement().detach();
			if (eventAnnotations.isEmpty()) {
				eventsElement.detach();
			}
		}
	}

	@Override
	public Map<String, Object> getLocalDataAnnotations() {
		return localData;
	}

	@Override
	public List<BPMNServiceCallAnnotation> getServiceCallAnnotations() {
		return Collections.unmodifiableList(serviceCallAnnotations);
	}
	
	public void addServiceCallAnnotation(BPMNServiceCallAnnotation serviceCallAnnotation) {
		if (serviceCallAnnotations.contains(serviceCallAnnotation)) return;
		if (serviceCallAnnotations.isEmpty()) {
			serviceCallsElement = new Element("serviceCalls", BPMNNamespace.ADP);
			extensionElementsElement.addContent(serviceCallsElement);
		}
		Element serviceCallElement = serviceCallAnnotation.getXMLElement(); 
		serviceCallsElement.addContent(serviceCallElement);
		serviceCallAnnotations.add(serviceCallAnnotation);
	}

	public void removeServiceCallAnnotation(BPMNServiceCallAnnotation serviceCallAnnotation) {
		boolean isRemoved = serviceCallAnnotations.remove(serviceCallAnnotation);
		if (isRemoved) {
			serviceCallAnnotation.getXMLElement().detach();
			if (serviceCallAnnotations.isEmpty()) {
				serviceCallsElement.detach();
			}
		}
	}

	@Override
	public List<BPMNTriggerAnnotation> getTriggerAnnotations() {
		return Collections.unmodifiableList(triggerAnnotations);
	}

	public void addTriggerAnnotation(BPMNTriggerAnnotation triggerAnnotation) {
		if (triggerAnnotations.contains(triggerAnnotation)) return;
		if (triggerAnnotations.isEmpty()) {
			triggersElement = new Element("triggers", BPMNNamespace.ADP);
			extensionElementsElement.addContent(triggersElement);
		}
		Element triggerElement = triggerAnnotation.getXMLElement(); 
		triggersElement.addContent(triggerElement);
		triggerAnnotations.add(triggerAnnotation);
	}

	public void removeTriggerAnnotation(BPMNTriggerAnnotation triggerAnnotation) {
		boolean isRemoved = triggerAnnotations.remove(triggerAnnotation);
		if (isRemoved) {
			triggerAnnotation.getXMLElement().detach();
			if (triggerAnnotations.isEmpty()) {
				triggersElement.detach();
			}
		}
	}
}
