package de.adp.commons.process;

import java.util.Map;

/**
 * Annotation for event triggering an action.
 * The triggered action depends on the element the annotation is placed:
 * - In a process, it triggers the instantiation and execution of the process instance.
 * - In a process element, it triggers the completion of the related process step.
 * - In a service call, it triggers the execution of the call. 
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public interface TriggerAnnotation {
	/**
	 * ID of the event activating the trigger.
	 * @return ID as stored in the event registry. 
	 */
	public String getEventId();
	
	/**
	 * Returns the reference for the event.
	 * An incoming event must match the given entries in order to be accepted as trigger.
	 * @return (Recursive) map of keys and references.
	 */
	public Map<String, Object> getReferences();
}
