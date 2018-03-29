package de.adp.commons.process;

import java.util.List;
import java.util.Map;

/**
 * Generic interface for annotations associated with a {@link ProcessDefinition} or {@link ProcessElement},
 * 
 * @author MSchmidt
 */
public interface Annotations {

	/**
	 * Returns the local data stored with the element.
	 * @return Container for the local data stored with the element.
	 */
	Map<String, Object> getLocalDataAnnotations();
	
	/**
	 * Returns the list of events generated during the processing of the element.
	 * @return List of events. May be empty.
	 */
	List<? extends EventAnnotation> getEventAnnotations();
	
	/**
	 * Returns the list of service calls performed during the processing of the element.
	 * @return List of service calls. May be empty.
	 */
	List<? extends ServiceCallAnnotation> getServiceCallAnnotations();
	
	/**
	 * Returns the triggers for the element.
	 * @return List of element triggers. May be empty.
	 */
	List<? extends TriggerAnnotation> getTriggerAnnotations();
}
