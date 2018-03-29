package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

import de.adp.commons.process.bpmn.BPMNNamespace;
import de.adp.commons.process.bpmn.BPMNProcessElement;

/**
 * Class representing a BPMN Event.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class BPMNEvent extends BPMNProcessElement {
	protected BPMNEvent(Element element) {
		super(element);
	}

	/**
	 * Checks if the event throws an error. 
	 * @return <code>true</code> if an error is thrown, otherwise false.
	 */
	public boolean trowsError() {
		Element errorDefinition = processElement.getChild("errorEventDefinition", BPMNNamespace.BPMN);
		return errorDefinition != null;
	}
	
	/**
	 * If set, the error thrown by the event is returned.
	 * @return Error message or <code>null</code> if no event does not throw an error.
	 */
	public String getErrorMessage() {
		return trowsError() ? super.getLabel() : null;
	}
}
