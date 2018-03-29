package de.adp.commons.process.bpmn;

import org.jdom2.Namespace;

/**
 * List of namespaces in the context of BPMN processing.
 * @author simon.schwantzer(at)im-c.de
 */
public interface BPMNNamespace {
	public Namespace BPMN = Namespace.getNamespace("http://www.omg.org/spec/BPMN/20100524/MODEL"); 
	public Namespace ADP = Namespace.getNamespace("adp:bpmn:annotations");
}
