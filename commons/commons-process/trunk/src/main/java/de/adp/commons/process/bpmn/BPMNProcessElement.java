package de.adp.commons.process.bpmn;

import org.jdom2.Element;

import de.adp.commons.process.Annotations;
import de.adp.commons.process.ProcessElement;
/**
 * Abstract class for BPMN elements giving access  
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class BPMNProcessElement implements ProcessElement {
	protected Element processElement;
	protected BPMNAnnotations annotations;
	
	protected BPMNProcessElement(Element processElement) {
		this.processElement = processElement;
		Element extensionElementsElement = processElement.getChild("extensionElements", BPMNNamespace.BPMN);
		if (extensionElementsElement == null) {
			extensionElementsElement = new Element("extensionElements", BPMNNamespace.BPMN);
			processElement.addContent(0, extensionElementsElement);
		}
		annotations = new BPMNAnnotations(extensionElementsElement);
	}
	
	@Override
	public Annotations getAnnotations() {
		return annotations;
	}

	@Override
	public String getId() {
		return processElement.getAttributeValue("id");
	}

	@Override
	public String getLabel() {
		return processElement.getAttributeValue("name");
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(200);
		builder.append("<").append(getType()).append(":").append(getId());
		String label = getLabel();
		if (label != null) {
			builder.append(":").append(label);
		}
		builder.append(">");
		return builder.toString();
	}
}
