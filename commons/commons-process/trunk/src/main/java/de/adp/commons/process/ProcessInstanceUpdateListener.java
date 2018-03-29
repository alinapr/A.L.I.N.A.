package de.adp.commons.process;


/**
 * Interface for handling process events.
 * @author simon.schwantzer(at)im-c.de
 */
public interface ProcessInstanceUpdateListener {
	/**
	 * Called when the start event of the process becomes active.
	 */
	public void start();
	
	/**
	 * Called when the end event of the process becomes active.
	 */
	public void end();
	
	/**
	 * Called when the process is terminated manually.
	 */
	public void cancel();
	
	/**
	 * Called when a call activity is reached.
	 * @param caller Element calling the sub process.
	 */
	public void activityCalled(ProcessCallingElement caller);
	
	/**
	 * Called when a process step is performed.
	 * This also includes start/stop events and call activities, although separate notifications are triggered.
	 * @param oldState The element instance which was active before the change happened. May be <code>null</code>.
	 * @param currentState Currently active element.
	 */
	public void stepPerformed(ProcessElementInstance oldState, ProcessElementInstance currentState);

	/**
	 * Called when an error state is reached during the process execution.
	 * @param element Process element representing the error state.
	 * @param code Error code related to HTTP status code table.
	 * @param message Human readable message describing the error.
	 */
	void error(ProcessElement element, int code, String message);
}
