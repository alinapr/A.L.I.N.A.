package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

/**
 * Class representing a BPMN End Event.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNEndEvent extends BPMNEvent {
	public static final String ELEMENT_TYPE = "endEvent";
	private final Element element;
	
	public BPMNEndEvent(Element element) {
		super(element);
		this.element = element;
	}
	
	public boolean isError() {
		return element.getChild("errorEventDefinition", element.getNamespace()) != null;
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}
}
