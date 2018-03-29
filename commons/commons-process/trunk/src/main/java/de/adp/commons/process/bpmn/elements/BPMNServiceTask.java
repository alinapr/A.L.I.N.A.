package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

/**
 * Class representing a BPMN Service Task.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNServiceTask extends BPMNTask {
	public static final String ELEMENT_TYPE = "serviceTask";

	public BPMNServiceTask(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}
}
