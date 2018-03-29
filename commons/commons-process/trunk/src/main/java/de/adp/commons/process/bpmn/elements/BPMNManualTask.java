package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;


/**
 * Class representing a BPMN Manual Task.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNManualTask extends BPMNTask {
	public static final String ELEMENT_TYPE = "manualTask";

	public BPMNManualTask(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}
}
