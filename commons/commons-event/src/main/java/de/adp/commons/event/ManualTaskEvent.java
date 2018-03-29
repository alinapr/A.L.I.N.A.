package de.adp.commons.event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Events generated when a manual task becomes active during process execution.
 * @author simon.schwantzer(at)im-c.de
 */
public class ManualTaskEvent extends TaskEvent {
	public static final String MODEL_ID = "processEvent:manualTask";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ManualTaskEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content);
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> task = (Map<String, Object>) getPayload().get("task");
			if (task != null) {
				if (!task.containsKey("title")) {
					throw new IllegalArgumentException("Missing field: payload.task.title");
				}
			} else {
				throw new IllegalArgumentException("Missing field: payload.task");
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Wrong field type: payload.task");
		}
	}
	
	/**
	 * Creates a process event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 * @param elementId ID of the related task element.
	 * @param taskTitle Title of the task.
	 */
	public ManualTaskEvent(String id, String processId, String processInstanceId, String elementId, String taskTitle) {
		super(id, MODEL_ID, processId, processInstanceId, elementId);
		Map<String, Object> task = new LinkedHashMap<String, Object>();
		task.put("title", taskTitle);
		getPayload().put("task", task);
	}
}