package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

import de.adp.commons.process.ProcessCallingElement;

/**
 * Class representing a BPMN Call Activity.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNCallActivity extends BPMNActivity implements ProcessCallingElement {
	public static final String ELEMENT_TYPE = "callActivity";

	/**
	 * Wraps an <code>callActivity</code> element.
	 * @param callActivityElement XML element.
	 */
	public BPMNCallActivity(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}

	@Override
	public String getDescription() {
		return (String) annotations.getLocalDataAnnotations().get("activityDescription");
	}
	
	
	@Override
	public String getCalledProcess() {
		return processElement.getAttributeValue("calledElement");
	}
	
	/**
	 * Returns the name of the activity.
	 * Defaults to label of the process element. 
	 * @return Name of the activity. May be <code>null</code>.
	 */
	public String getName() {
		String name = (String) annotations.getLocalDataAnnotations().get("activityName");
		if (name == null) {
			name = super.getLabel();
		}
		return name;
	}
}
