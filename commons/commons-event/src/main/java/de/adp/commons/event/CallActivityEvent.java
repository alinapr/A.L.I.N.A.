package de.adp.commons.event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Event generated in the context of activities during process execution.
 * @author simon.schwantzer(at)im-c.de
 */
public class CallActivityEvent extends ProcessEvent {
	public static final String MODEL_ID = "processEvent:callActivity";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public CallActivityEvent(Map<String, Object> content) {
		super(content);
		if (!getPayload().containsKey("elementId")) {
			throw new IllegalArgumentException("Missing field: payload.elementId");
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> activity = (Map<String, Object>) getPayload().get("activity");
			if (activity != null) {
				if (!activity.containsKey("processId")) {
					throw new IllegalArgumentException("Missing field: payload.activity.processId");
				}
			} else {
				throw new IllegalArgumentException("Missing field: payload.activity");
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Wrong field type: payload.activity");
		}
	}
	
	/**
	 * Creates the event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the process instance. 
	 * @param elementId ID of the element which was active when the event has been created.
	 * @param activityProcessId ID of the process connected with the activity.
	 */
	public CallActivityEvent(String id, String processId, String processInstanceId, String elementId, String activityProcessId) {
		super(id, MODEL_ID, processId, processInstanceId);
		super.setElementId(elementId);
		Map<String, Object> activity = new LinkedHashMap<String, Object>();
		activity.put("processId", activityProcessId);
		super.getPayload().put("activity", activity);
	}
	
	/**
	 * Sets the title for the activity.
	 * @param activityTitle Activity title as stored in the related process element.
	 */
	public void setActvitiyTitle(String activityTitle) {
		@SuppressWarnings("unchecked")
		Map<String, Object> activity = (Map<String, Object>) getPayload().get("activity");
		activity.put("title", activityTitle);
	}
	
	/**
	 * Returns the activity title.
	 * @return Activity title as stored in the related process element. May be <code>null</code>.
	 */
	public String getActivityTitle() {
		@SuppressWarnings("unchecked")
		Map<String, Object> activity = (Map<String, Object>) getPayload().get("activity");
		return (String) activity.get("title");
	}
	
	/**
	 * Returns the process which is connected with the activity.
	 * @return ID of a process definition.
	 */
	public String getActivityProcessId() {
		@SuppressWarnings("unchecked")
		Map<String, Object> activity = (Map<String, Object>) getPayload().get("activity");
		return (String) activity.get("processId");
	}
}
