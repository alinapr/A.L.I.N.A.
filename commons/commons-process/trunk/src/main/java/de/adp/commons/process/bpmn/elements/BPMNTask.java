package de.adp.commons.process.bpmn.elements;

import org.jdom2.Element;

/**
 * Class representing a BPMN Task.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class BPMNTask extends BPMNActivity {

	protected BPMNTask(Element element) {
		super(element);
	}
	
	/**
	 * Returns the title for the task.
	 * Default to process element label.
	 * @return Title for the task. May be <code>null</code>.
	 */
	public String getTitle() {
		String title = (String) annotations.getLocalDataAnnotations().get("taskTitle");
		if (title == null) {
			title = super.getLabel();
		}
		return title;
	}
	
	/**
	 * Returns the description for the task.
	 * @return Description for the task. May be <code>null</code>.
	 */
	public String getDescription() {
		return (String) annotations.getLocalDataAnnotations().get("taskDescription");
	}
}
