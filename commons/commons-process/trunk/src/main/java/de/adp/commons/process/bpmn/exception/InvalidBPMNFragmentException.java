package de.adp.commons.process.bpmn.exception;

/**
 * Exception signalizing that the underlying fragment of BPMN is beyond the
 * fragment supported by the prototype and thus cannot be interpreted.
 * 
 * @author MSchmidt
 */
public class InvalidBPMNFragmentException extends Exception {

	private static final long serialVersionUID = -550546359956987498L;
	
	public InvalidBPMNFragmentException(String message) {
		super(message);
	}
	
	public InvalidBPMNFragmentException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
