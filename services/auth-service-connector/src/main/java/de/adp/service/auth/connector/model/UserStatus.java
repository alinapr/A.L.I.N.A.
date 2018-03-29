package de.adp.service.auth.connector.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a user online status.
 * @author simon.schwantzer(at)im-c.de
 */
public class UserStatus {
	private final JsonObject json;
	private final List<View> views;
	
	/**
	 * Creates a new model by wrapping the given JSON object.
	 * @param json JSON object representing a users online status.
	 * @throws IllegalArgumentException The given JSON object does not represent a user online status.
	 */
	public UserStatus(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		
		views = new ArrayList<>();
		JsonArray viewsArray = json.getArray("views");
		if (viewsArray != null) for (Object entry : viewsArray) {
			views.add(new View((JsonObject) entry));
		}
	}
	
	private final static void validateJson(JsonObject json) throws IllegalArgumentException {
		if (json.getBoolean("isOnline") == null) {
			throw new IllegalArgumentException("Missing online status [isOnline].");
		}
	}

	/**
	 * Returns the JSON object wrapped.
	 * @return JSON representing a user status.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Checks if the user is online.
	 * @return <code>true</code> if the user has an active client session, otherwise <code>false</code>.
	 */
	public boolean isOnline() {
		return json.getBoolean("isOnline");
	}
	
	/**
	 * Returns the identifier of the users session.
	 * @return Session identifier. May be <code>null</code>.
	 */
	public String getSessionId() {
		return json.getString("sessionId");
	}
	
	/**
	 * Returns the last time the user was active.
	 * @return Date of last activity. May be <code>null</code>.
	 */
	public Date getLastActivity() {
		String dateString = json.getString("lastActivity");
		return dateString != null ? DatatypeConverter.parseDateTime(dateString).getTime() : null;
	}
	
	/**
	 * Returns the list of active views (clients) of the user.
	 * @return List of views. May be empty.
	 */
	public List<View> getViews() {
		return views;
	}
}
