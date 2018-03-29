package de.adp.commons.event;

import java.util.Map;

/**
 * Events generated during the execution of a process.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class ProcessEvent extends ADPEvent {
	public static final String MODEL_ID = "processEvent";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	protected ProcessEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content, Type.SERVICE);
		Map<String, Object> payload = super.getPayload();
		if (payload == null) {
			throw new IllegalArgumentException("Missing field: payload");
		}
		if (payload.get("processInstanceId") == null) {
			throw new IllegalArgumentException("Missing field: payload.processInstanceId");
		}
		if (payload.get("processId") == null) {
			throw new IllegalArgumentException("Missing field: payload.processId");
		}
	}
	
	/**
	 * Creates a process event.
	 * @param id ID for the event instance.
	 * @param modelId ID of the concrete model.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 */
	public ProcessEvent(String id, String modelId, String processId, String processInstanceId) {
		super(id, modelId, Type.SERVICE);
		getPayload().put("processId", processId);
		getPayload().put("processInstanceId", processInstanceId);
	}

	/**
	 * Sets the user on whose behalf the process is being executed.
	 * @param userId ID of user.
	 */
	public void setUserId(String userId) {
		super.getPayload().put("userId", userId);
	}
	
	/**
	 * Returns the user on whose behalf the process is being executed.
	 * @return ID of the user.
	 */
	public String getUserId() {
		return (String) super.getPayload().get("userId");
	}
	
	/**
	 * Returns the instance of the executed process.
	 * @return ID of the process instance.
	 */
	public String getProcessInstanceId() {
		return (String) super.getPayload().get("processInstanceId");
	}
	
	/**
	 * Sets the element which was active when the event was created. 
	 * @param elementId ID of a process element.
	 */
	public void setElementId(String elementId) {
		super.getPayload().put("elementId", elementId);
	}
	
	/**
	 * Returns the element which was active when the event was created.
	 * @return ID of a process element.
	 */
	public String getElementId() {
		return (String) super.getPayload().get("elementId");
	}
	
	/**
	 * Returns the process related to this event.
	 * @return ID of a process definition.
	 */
	public String getProcessId() {
		return (String) super.getPayload().get("processId");
	}
	
	/**
	 * Returns the estimated progress of the process execution. 
	 * @return Value between 0.0 (not started) and 1.0 (completed).
	 */
	public double getProgress() {
		Number progress = (Number) super.getPayload().get("progress");
		return progress.doubleValue();
	}
	
	/**
	 * Sets the estimated progress of the process execution.
	 * @param progress Value between 0.0 (not started) and 1.0 (completed).
	 */
	public void setProgress(double progress) {
		super.getPayload().put("progress", Double.valueOf(progress));
	}
}
