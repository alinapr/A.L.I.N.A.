package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

import de.adp.commons.process.bpmn.BPMNProcessElement;

/**
 * Class representing a BPMN Activity.
 * 
 * @author MSchmidt, simon.schwantzer(at)im-c.de
 */
public abstract class BPMNActivity extends BPMNProcessElement {
	public BPMNActivity(Element element) {
		super(element);
	}
		
	/**
	 * Returns a description for the activity.
	 * @return Description for the activity. May be null;
	 */
	public abstract String getDescription();
}
