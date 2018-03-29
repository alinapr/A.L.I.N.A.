package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

/**
 * Class representing a BPMN Intermediate Event.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNIntermediateEvent extends BPMNEvent {
	public static final String ELEMENT_TYPE = "intermediateEvent";

	public BPMNIntermediateEvent(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}
}
