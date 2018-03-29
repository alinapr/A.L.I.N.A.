package de.adp.commons.process;

/**
 * Interface for process element calling subprocesses.
 * @author simon.schwantzer(at)im-c.de
 */
public interface ProcessCallingElement extends ProcessElement {
	/**
	 * Returns the ID of the process called by this element. 
	 * @return ID of a process definition to be instantiated.
	 */
	public String getCalledProcess();
}
