package de.adp.commons.event;

import java.util.Map;

/**
 * Events generated when a user service becomes active during process execution.
 * @author simon.schwantzer(at)im-c.de
 */
public class ServiceTaskEvent extends TaskEvent {
	public static final String MODEL_ID = "processEvent:serviceTask";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ServiceTaskEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content);
	}
	
	/**
	 * Creates a process event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 * @param elementId ID of the related task element.
	 */
	public ServiceTaskEvent(String id, String processId, String processInstanceId, String elementId) {
		super(id, MODEL_ID, processId, processInstanceId, elementId);
	}
}