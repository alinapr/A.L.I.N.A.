package de.adp.commons.event;

import java.util.Map;

public class SupportRequestEvent extends ADPEvent {
	public static final String MODEL_ID = "supportRequest";

	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public SupportRequestEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content, Type.USER);
		if (getPayload() == null) {
			throw new IllegalArgumentException("Missing field: payload");
		}
		if (!getPayload().containsKey("supportId")) {
			throw new IllegalArgumentException("Missing field: payload.supportId");
		}
	}
	
	/**
	 * Creates the event.
	 * @param id ID for the event instance.
	 * @param supportId ID of the process definition specifying the support.
	 */
	public SupportRequestEvent(String id, String supportId) {
		super(id, MODEL_ID, Type.USER);
		getPayload().put("supportId", supportId);
	}
	
	/**
	 * Returns the ID of the related support process.
	 * @return ID of a process definition.
	 */
	public String getSupportId() {
		return (String) getPayload().get("supportId");
	}
}
