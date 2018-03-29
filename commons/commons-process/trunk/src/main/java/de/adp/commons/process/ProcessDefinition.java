package de.adp.commons.process;

import java.util.List;
import java.util.Set;


/**
 * Class representing the process itself, i.e. including the schema information. 
 * We represent processes by ID and label, plus a pointer to the process start
 * element. In addition, the class contains some convenience methods.
 * 
 * @author MSchmidt
 */
public interface ProcessDefinition {
	
	/**
	 * Retrieves the unique ID of the process.
	 * 
	 * @return the id identifying the process uniquely
	 */
	public String getId();

	/**
	 * Returns the label of the process.
	 * 
	 * @return the human-readable label/name of the process
	 */
	public String getLabel();
	
	/**
	 * Returns all process elements of the process definition.
	 * @return Set with all elements of the process definition. 
	 */
	public Set<ProcessElement> getAllProcessElements();

	/**
	 * Gets the start element of the process
	 * 
	 * @return the start element of the process
	 */
	public ProcessElement getProcessStartElement();
	
	/**
	 * Searches all elements in the process and returns the element with the given id.
	 * 
	 * @param processElementId the ID of the process element to retrieve
	 * @return the element inside the process matching the id, null if not exists
	 * @throws IllegalArgumentException in case the process does not have an element with the given ID
	 */
	public ProcessElement getProcessElementById(String processElementId)	
	throws IllegalArgumentException;
	
	/**
	 * Returns the element in the process which refers to the given one.
	 *  
	 * @param element Element to determine predecessor for.
	 * @return Predecessor or <code>null</code> if the element has no predecessor in the process.
	 * @throws IllegalArgumentException in case the process does not have an element with the given ID
	 */
	public ProcessElement getPredecessor(ProcessElement element) throws IllegalArgumentException;
	
	/**
	 * Returns all elements which are direct successors to the give one.
	 * 
	 * @param element Element to determine successors for.
	 * @return List of succeeding element. May be empty.
	 * @throws IllegalArgumentException in case the process does not have an element with the given ID
	 */
	public List<? extends ProcessElement> getSuccessors(ProcessElement element) throws IllegalArgumentException;
	
	/**
	 * Returns the annotations for the process.
	 * @return Annotations stored with the process.
	 */
	public Annotations getAnnotations();
	
	/**
	 * Returns the distance from the given element to the start element.
	 * @param element Element to determine distance for.
	 * @return Number of elements on the shortest path between both elements.
	 */
	public int getDistanceFromStart(ProcessElement element);
	
	/**
	 * Returns the distance from the given element to the most far end.  
	 * @param element Element to determine distance for.
	 * @return Number of elements on the shortest path between the given element and the most far end.
	 */
	public int getMaxDistanceToEnd(ProcessElement element);
}