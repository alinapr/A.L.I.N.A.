package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

/**
 * Class representing a BPMN Start Event.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNStartEvent extends BPMNEvent {
	public static final String ELEMENT_TYPE = "startEvent";

	public BPMNStartEvent(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}
}
