package de.adp.commons.process;

/**
 * Generic process element, such as an activity, a task, or a gateway.
 * Subclassed by concrete BPMN elements
 * 
 * @author MSchmidt
 */
public abstract interface ProcessElement {

	/**
	 * Retrieves the ID of the element.
	 * 
	 * @return the ID of the element
	 */
	public String getId();
	
	/**
	 * Retrieves the human-readable label/name of the element.
	 * 
	 * @return the label/name of the element
	 */
	public String getLabel();
	
	/**
	 * Returns the type of the element.
	 * 
	 * @return String representing the type of the element.
	 */
	public String getType();
		
	/**
	 * Retrieves the annotation object associated with the element.
	 * 
	 * @return Annotations for the element.
	 */
	public Annotations getAnnotations();
}
