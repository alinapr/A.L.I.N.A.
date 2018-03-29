package de.adp.commons.event;

import java.util.Map;

/**
 * Event generated when user logs out or is disconnected from the system. 
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class UserOfflineEvent extends ADPEvent {
	public static final String MODEL_ID = "userOffline";

	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public UserOfflineEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content, Type.USER);
		if (super.getSessionId() == null) {
			throw new IllegalArgumentException("Missing field: session");
		}		
		if (super.getPayload() == null) {
			throw new IllegalArgumentException("Missing field: payload");
		}
		if (!super.getPayload().containsKey("userId")) {
			throw new IllegalArgumentException("Missing field: payload.userId");
		}
		if (!super.getPayload().containsKey("deviceId")) {
			throw new IllegalArgumentException("Missing field: payload.deviceId");
		}
	}
	
	/**
	 * Creates the event.
	 * @param id ID for the event instance.
	 * @param sessionId ID of the user who logged out.
	 * @param userId ID of the user who logged out.
	 * @param deviceId ID of the device the user was logged in with. 
	 */
	public UserOfflineEvent(String id, String sessionId, String userId, String deviceId) {
		super(id, MODEL_ID, Type.USER);
		setSessionId(sessionId);
		getPayload().put("userId", userId);
		getPayload().put("deviceId", deviceId);
	}
	
	/**
	 * Returns the user who logged out.
	 * @return ID of the user.
	 */
	public String getUserId() {
		return (String) getPayload().get("userId");
	}
	
	/**
	 * Returns the device the user was logged in with.
	 * @return ID of the device.
	 */
	public String getDeviceId() {
		return (String) getPayload().get("deviceId");
	}
}
