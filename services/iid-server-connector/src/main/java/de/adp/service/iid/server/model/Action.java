package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a action triggered by the client.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class Action {
	public enum Type {
		POST,
		ADP_EVENT, 
		MESSAGE
	}
	
	protected final JsonObject json;
			
	protected Action(JsonObject json) {
		this.json = json;
	}
	
	/**
	 * Parses the given JSON object to a typed action.
	 * @param json JSON object representing a action.
	 * @return Typed action object. See {@link Action#getType()} for details.
	 * @throws IllegalArgumentException The given JSON object does not encode a valid action.
	 */
	public static Action fromJson(JsonObject json) throws IllegalArgumentException {
		String type = json.getString("type");
		if (type == null) {
			throw new IllegalArgumentException("Missing action type.");
		}
		switch (type) {
		case "post":
			return new HttpPostAction(json);
		case "adp-event":
			return new ADPEventAction(json);
		case "event":
			return new SendMessageAction(json);
		default:
			throw new IllegalArgumentException("Unknown action type.");
		}
	}
	
	/**
	 * Returns the action as JSON object.
	 * @return JSON object representing the action.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the type of the action.
	 * @return Action type.
	 */
	public abstract Type getType();
	
	/**
	 * Returns the identifier of the session in which the event was triggered.
	 * @return Session identifier, may be <code>null</code>.
	 */
	public abstract String getSessionId();
	
	/**
	 * Sets the session in which the event was triggered.
	 * @param sessionId Session identifier.
	 */
	public abstract void setSessionId(String sessionId);
	
	/**
	 * Returns an authorization token for the action.
	 * @return Authorization token. May be <code>null</code>.
	 */
	public abstract String getToken();
	
	/**
	 * Sets an authorization token for the action.
	 * @param token Token to authorize the action.
	 */
	public abstract void setToken(String token);
}
