package de.adp.commons.event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Events generated when a process task is executed.s
 * @author simon.schwantzer(at)im-c.de
 *
 */
public abstract class TaskEvent extends ProcessEvent {

	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public TaskEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content);
		if (!getPayload().containsKey("elementId")) {
			throw new IllegalArgumentException("Missing field: payload.elementId");
		}
	}
	
	/**
	 * Creates a process event.
	 * @param id ID for the event instance.
	 * @param modelId ID of the concrete model.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 * @param elementId ID of the related task element.
	 */
	public TaskEvent(String id, String modelId, String processId, String processInstanceId, String elementId) {
		super(id, modelId, processId, processInstanceId);
		super.setElementId(elementId);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getOrCreateTask() {
		Map<String, Object> payload = super.getPayload();
		Map<String, Object> task;
		if (payload.containsKey("task")) {
			task = (Map<String, Object>) payload.get("task");
		} else {
			task = new LinkedHashMap<String, Object>();
			payload.put("task", task);
		}
		return task;
	}
	
	/**
	 * Sets the title of the task.
	 * @param taskTitle Task title.
	 */
	public void setTaskTitle(String taskTitle) {
		Map<String, Object> task = getOrCreateTask();
		task.put("title", taskTitle);
	}
	
	/**
	 * Returns the title of the task.
	 * @return Task title. May be <code>null</code>.
	 */
	public String getTaskTitle() {
		@SuppressWarnings("unchecked")
		Map<String, Object> task = (Map<String, Object>) super.getPayload().get("task");
		if (task != null) {
			return (String) task.get("title");
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the description of the task.
	 * @param taskDescription Task description.
	 */
	public void setTaskDescription(String taskDescription) {
		Map<String, Object> task = getOrCreateTask();
		task.put("description", taskDescription);
	}
	
	/**
	 * Returns the description of the task.
	 * @return Task description. May be <code>null</code>.
	 */
	public String getTaskDescription() {
		@SuppressWarnings("unchecked")
		Map<String, Object> task = (Map<String, Object>) super.getPayload().get("task");
		if (task != null) {
			return (String) task.get("description");
		} else {
			return null;
		}
	}
}