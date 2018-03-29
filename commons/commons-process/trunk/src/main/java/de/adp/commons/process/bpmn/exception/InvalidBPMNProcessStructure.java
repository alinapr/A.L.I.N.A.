package de.adp.commons.process.bpmn.exception;

/**
 * Exception thrown if the process structure cannot be handled by the PKI.
 * @author simon.schwantzer(at)im-c.de
 */
public class InvalidBPMNProcessStructure extends Exception {
	private static final long serialVersionUID = 1L;
	
	public InvalidBPMNProcessStructure(String message) {
		super(message);
	}
	
	public InvalidBPMNProcessStructure(String message, Throwable t) {
		super(message, t);
	}
}
