package de.adp.commons.event;

import java.util.Map;

/**
 * Event generated when a process instance has reached a end event.
 * @author simon.schwantzer(at)im-c.de
 */
public class ProcessCancelledEvent extends ProcessEvent {
	public static final String MODEL_ID = "processEvent:processCancelled";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ProcessCancelledEvent(Map<String, Object> content) {
		super(content);
	}
	
	/**
	 * Creates the event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 */
	public ProcessCancelledEvent(String id, String processId, String processInstanceId) {
		super(id, MODEL_ID, processId, processInstanceId);
	}
	
	/**
	 * Sets the ID of the parent process instance.
	 * @param parentInstance Identifier of the process instance which created this one as a subprocess. 
	 */
	public void setParentInstance(String parentInstance) {
		super.getPayload().put("parentInstance", parentInstance);
	}
	
	/**
	 * Returns the ID of the parent process instance.
	 * @return Identifier of the process instance which created this one as a subprocess. Returns <code>null</code> if this is the top level process instance.
	 */
	public String getParentInstance() {
		return (String) super.getPayload().get("parentInstance");
	}
}
