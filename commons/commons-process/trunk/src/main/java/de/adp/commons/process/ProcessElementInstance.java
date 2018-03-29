package de.adp.commons.process;

import java.util.Date;

import de.adp.commons.event.ADPEvent;

/**
 * Interface for a instance of a process element.
 * The instance adds runtime information to the process element.
 * @author simon.schwantzer(at)im-c.de
 */
public interface ProcessElementInstance {
	/**
	 * Returns the instance of the related processes.
	 * @return Process instance.
	 */
	public ProcessInstance getProcessInstance();
	
	/**
	 * Returns the process element instantiated.
	 * @return Process element.
	 */
	public ProcessElement getProcessElement();
	
	/**
	 * Returns the step executed before this one.
	 * @return Process element instance or <code>null</code> if this was the first step.
	 */
	public ProcessElementInstance getPredecessor();
	
	/**
	 * Returns the time the element was instantiated.  
	 * @return Time of instantiation. 
	 */
	public Date getStart();
	
	/**
	 * Returns the time the execution of the element was completed.
	 * @return Time of completion. <code>null</code> if the element has not been completed.
	 */
	public Date getEnd();
	
	/**
	 * Returns the event which triggered the completion of the element.
	 * @return Event or <code>null</code> if the completion has not been triggered by an event.
	 */
	public ADPEvent getTrigger();
	
}
