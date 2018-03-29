package de.adp.commons.process;

import java.util.Map;

/**
 * Annotation for a service call performed when a process element is active.
 * @author simon.schwantzer(at)im-c.de
 */
public interface ServiceCallAnnotation {
	/**
	 * Type for the service call.
	 * A service call can either be performed when the element becomes active, when it becomes inactive,
	 * or when a event is received. 
	 * @author simon.schwantzer(at)im-c.de
	 */
	public enum Type {
		START,
		END,
		TRIGGER
	}
	
	/**
	 * Returns the ID of the service called. 
	 * @return ID of the service as listed in the service registry.
	 */
	public String getService();
	
	/**
	 * Returns the ID of the service method called. 
	 * @return ID of the service method as listed in the service registry.
	 */
	public String getMethod();
	
	/**
	 * Returns the type of the service call, i.e. when it is called.
	 * @return Service call type.
	 */
	public Type getType();
	
	/**
	 * Returns the name of the context variable the response is stored.
	 * @return Variable name. May be <code>null</code>.
	 */
	public String getOutputReference();
	
	/**
	 * Returns the input parameters for the call. 
	 * @return Map with parameter names (key) and local variable names (values) to be used when the service method is called.
	 */
	public Map<String, String> getInputMapping();
	
	/**
	 * Returns the output mapping for the call.
	 * @return Map with result object keys (key) and names of context variables to store the object in (values).
	 */
	public Map<String, String> getOutputMapping();
	
	/**
	 * Return the trigger for this service call.
	 * A trigger is only used if the service call type is <code>Type.TRIGGER</code>.
	 * @return Trigger for the service call.
	 */
	public TriggerAnnotation getTrigger();
}
