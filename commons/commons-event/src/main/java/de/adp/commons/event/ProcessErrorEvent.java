package de.adp.commons.event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Event generated when a process ended in an error state.
 * @author simon.schwantzer(at)im-c.de
 */
public class ProcessErrorEvent extends ProcessEvent {
	public static final String MODEL_ID = "processEvent:processError";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ProcessErrorEvent(Map<String, Object> content) {
		super(content);
		if (!getPayload().containsKey("elementId")) {
			throw new IllegalArgumentException("Missing field: payload.elementId");
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> error = (Map<String, Object>) getPayload().get("error");
			if (error != null) {
				if (!error.containsKey("code")) {
					throw new IllegalArgumentException("Missing field: payload.error.code");
				}
				if (!error.containsKey("message")) {
					throw new IllegalArgumentException("Missing field: payload.error.message");
				}
			} else {
				throw new IllegalArgumentException("Missing field: payload.error");
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Wrong field type: payload.error");
		}
	}
	
	/**
	 * Creates a process start event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 * @param elementId 
	 */
	public ProcessErrorEvent(String id, String processId, String processInstanceId, String elementId, int errorCode, String errorMessage) {
		super(id, MODEL_ID, processId, processInstanceId);
		setElementId(elementId);
		Map<String, Object> error = new LinkedHashMap<String, Object>();
		error.put("code", errorCode);
		error.put("message", errorMessage);
		getPayload().put("error", error);
	}
	
	/**
	 * Returns the code for this error. 
	 * @return Error code.
	 */
	public int getErrorCode() {
		@SuppressWarnings("unchecked")
		Map<String, Object> error = (Map<String, Object>) getPayload().get("error");
		return (Integer) error.get("code");
	}
	
	/**
	 * Returns the error message.
	 * @return Error message.
	 */
	public String getErrorMessage() {
		@SuppressWarnings("unchecked")
		Map<String, Object> error = (Map<String, Object>) getPayload().get("error");
		return (String) error.get("message");
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
