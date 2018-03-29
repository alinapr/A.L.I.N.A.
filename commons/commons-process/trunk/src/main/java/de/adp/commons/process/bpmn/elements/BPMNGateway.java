package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

import de.adp.commons.process.bpmn.BPMNProcessElement;

/**
 * Class representing a BPMN Gateway.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class BPMNGateway extends BPMNProcessElement {

	protected BPMNGateway(Element element) {
		super(element);
	}
}
