package de.adp.commons.process;

import java.util.List;
import java.util.Map;

import de.adp.commons.process.exception.AmbiguousFlowException;

/**
 * The class {@link ProcessInstance} represents a running process, i.e. it
 * basically consists of a {@link ProcessDefinition} plus a state (in form of
 * a vector of {@link ProcessElement}s). In addition, it provides methods
 * for querying and manipulating the state of the process instance.
 * 
 * @author MSchmidt
 */
public interface ProcessInstance {
	
	/**
	 * Retrieves the unique ID of the process.
	 * 
	 * @return the ID identifying the process uniquely
	 */
	public String getId();
	
	/**
	 * Returns the ID of the user who instantiated the process.
	 * @return User ID or <code>null</code> if the process was not instantiated in a context of a user.
	 */
	public String getUserId();
	
	/**
	 * Returns the ID of user session in whose context the process was instantiated.
	 * @return Session identifier.
	 */
	public String getSessionId();
	
	/**
	 * Retrieves the definition (i.e., schema) of the process.
	 * 
	 * @return the process definition underlying the process
	 */
	public ProcessDefinition getProcessDefinition();
	
	/**
	 * Returns the process instance in which this instance was created as a subprocess.
	 * @return Process instance if called as a subprocess, <code>null</code> otherwise. 
	 */
	public ProcessInstance getParent();
	
	/**
	 * Returns the current context of the process instance.
	 * 
	 * @return the process instance context
	 */
	public Map<String, Object> getProcessInstanceContext();
	
	/**
	 * Checks whether the process instance if currently running.
	 * 
	 * @return true iff the project is running
	 */
	public boolean isRunning();
	
	/**
	 * Retrieve the current step in the process. 
	 * 
	 * @return Instance of the process element currently active.
	 */
	public ProcessElementInstance getCurrentState();
	
	/**
	 * Returns the execution history of the process instance.
	 * The history is an chronologically reversed list of steps processed before the current element became active. 
	 * 
	 * @return List of process steps. If the list is empty, the instance is in the initial state.
	 */
	public List<? extends ProcessElementInstance> getHistory();
	
	/**
	 * Checks if the process has one or more elements succeeding the currently active one.
	 * 
	 * @return <code>true</code> of there is one or more steps following the current one, otherwise <code>false</code>.
	 */
	public boolean hasNextStep();
	
	/**
	 * Moves to the successor step in case the transition is unique, utherwise
	 * an exception is thrown. If the active element does not have an succeeding element, nothing will happen.
	 * 
	 * @return Step after the step forward has been performed. 
	 * @throws AmbiguousFlowException in case there is more than one successor.
	 */
	public ProcessElementInstance stepForward() throws AmbiguousFlowException;
	
	/**
	 * Moves to the specified predecessor step.
	 * 
	 * @param processElementId ID of the succeeding step to move to.
	 * @return Step after the step forward has been performed. 
	 * @throws IllegalArgumentException in case there is no succeeding step with the given ID.
	 */
	public ProcessElementInstance stepForward(String processElementId) throws IllegalArgumentException;
	
	/**
	 * Executes a subprocess.
	 * @return New instance of the subprocess.  
	 * @throws IllegalStateException in case the active task does not link to a subprocess.
	 */
	public ProcessInstance enterSubprocess() throws IllegalStateException;

	/**
	 * Moves to the predecessor step (i.e., the previously active process element).
	 * 
	 * @return Step after the step backward has been performed. 
	 * @throws IllegalArgumentException in case there is no predecessor.
	 */
	public ProcessElementInstance stepBackward();
	
	/**
	 * Terminates the process instance, i.e. once this method has been called
	 * method {@link #isRunning()} will always return false.
	 */
	public void terminate();
	
	/**
	 * Registers a listener to be notified when the state of the process instance changes.
	 * @param listener Update listener for process instance changes.
	 */
	public void addUpdateListener(ProcessInstanceUpdateListener listener);
	
	/**
	 * Removes a listener from the list of listeners notified about process instance state changes.
	 * @param listener Listener to remove.
	 */
	public void removeUpdateListener(ProcessInstanceUpdateListener listener);
}
