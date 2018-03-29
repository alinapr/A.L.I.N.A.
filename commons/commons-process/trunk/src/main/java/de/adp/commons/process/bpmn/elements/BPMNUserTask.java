package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;


/**
 * Class representing a BPMN User Task.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNUserTask extends BPMNTask {
	public static final String ELEMENT_TYPE = "userTask";

	public BPMNUserTask(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	} 
}
