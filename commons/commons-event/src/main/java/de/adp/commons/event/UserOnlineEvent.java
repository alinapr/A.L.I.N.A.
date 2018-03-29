package de.adp.commons.event;

import java.util.Map;

/**
 * Event generated when user logs in successfully. 
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class UserOnlineEvent extends ADPEvent {
	public static final String MODEL_ID = "userOnline";

	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public UserOnlineEvent(Map<String, Object> content) throws IllegalArgumentException {
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
	 * @param sessionId ID of the user who logged in.
	 * @param userId ID of the user who logged in.
	 * @param deviceId ID of the device the user logged in with. 
	 */
	public UserOnlineEvent(String id, String sessionId, String userId, String deviceId) {
		super(id, MODEL_ID, Type.USER);
		setSessionId(sessionId);
		getPayload().put("userId", userId);
		getPayload().put("deviceId", deviceId);
	}
	
	/**
	 * Returns the user who logged in.
	 * @return ID of the user.
	 */
	public String getUserId() {
		return (String) getPayload().get("userId");
	}
	
	/**
	 * Returns the device the user logged in with.
	 * @return ID of the device.
	 */
	public String getDeviceId() {
		return (String) getPayload().get("deviceId");
	}
}
