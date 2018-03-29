package de.adp.commons.process;

import java.util.Map;

/**
 * Annotation for a event generated during the processing of a process element.  
 * @author simon.schwantzer(at)im-c.de
 */
public interface EventAnnotation {
	/**
	 * Type of the event. A event is either generated when the element becomes active or when is completed. 
	 * @author simon.schwantzer(at)im-c.de
	 */
	public enum Type {
		START,
		END
	}
	
	/**
	 * Returns the ID of the event which is instantiated. 
	 * @return ID of a event model-
	 */
	public String getEventId();
	
	/**
	 * Returns the type of the event annotation.
	 * @return Type of the event annotation.
	 */
	public Type getType();
	
	/**
	 * Returns the properties for the event.
	 * @return (Recursive) map of keys and references.
	 */
	public Map<String, Object> getProperties();
}
