package de.adp.service.auth.connector.model;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a session.
 * The model is internally represented by a JSON object.
 * @author simon.schwantzer(at)im-c.de
 */
public class Session {
	private final JsonObject json;
	
	/**
	 * Creates a session based on the given JSON object.
	 * @param json JSON object to wrap.
	 * @throws IllegalArgumentException The given JSON object is not a valid session
	 */
	public Session(JsonObject json) throws IllegalArgumentException {
		this.json = json;
		validateJson(json);
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		if (json == null) throw new IllegalArgumentException("Field missing.");
		String id = json.getString("id");
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Missing identifier (id).");
		}
	}
	
	/**
	 * Returns the JSON object wrapped by this model.
	 * @return JSON representation of the session.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Creates a new session.
	 * @param id Identifier for the session.
	 */
	public Session(String id) {
		json = new JsonObject();
		json.putString("id", id);
	}
	
	/**
	 * Returns the identifier of the session.
	 * @return Session identifier (UUID).
	 */
	public String getId() {
		return json.getString("id");
	}
	
	/**
	 * Returns the user the session belongs to.
	 * @return User identifier or <code>null</code> if the session is no user session.
	 */
	public String getUserId() {
		return json.getString("userId");
	}
	
	/**
	 * Sets the user the sessions belongs to.
	 * @param userId User identifier.
	 */
	public void setUserId(String userId) {
		json.putString("userId", userId);
	}
	
	/**
	 * Registers a view for the session.
	 * @param view View to register.
	 */
	public void registerView(View view) {
		JsonArray views = json.getArray("views");
		if (views == null) {
			views = new JsonArray();
			json.putArray("views", views);
		}
		views.addObject(view.asJson());
	}
	
	/**
	 * Removes a view for the session.
	 * @param viewId ID of the view which should be removed.
	 */
	public void removeView(String viewId) {
		JsonArray views = json.getArray("views");
		
		if (views != null) {
			JsonArray newViews = new JsonArray();
			for (int i = 0; i < views.size(); i++) {
				View view = new View((JsonObject) views.get(i));
				if (!view.getId().equals(viewId)) {
					newViews.addObject(view.asJson());
				}
			}
			json.putArray("views", newViews);
		}
	}
	
	/**
	 * Checks if at least one view is registered for the session.
	 * @return <code>true</code> if one or more views are registered, otherwise <code>false</code>.
	 */
	public boolean hasView() {
		JsonArray views = json.getArray("views");
		return views != null && views.size() > 0;
	}
	
	/**
	 * Returns a list of all views registered for the session.
	 * @return List of views, may be empty.
	 */
	public List<View> getViews() {
		List<View> views = new ArrayList<View>();
		JsonArray viewsArray = json.getArray("views");
		
		if (viewsArray != null) for (Object entry : viewsArray) {
			View view = new View((JsonObject) entry);
			views.add(view);
		}
		
		return views;
	}
	
	/**
	 * Returns the last time the session has been updated.  
	 * @return Date time of the last activity as ISO 8601 string.
	 */
	public String getLastActivity() {
		String dateTimeString = json.getString("lastActivitiy");
		return dateTimeString;
	}
	
	/**
	 * Returns the data stored with the session.
	 */
	public JsonObject getData() {
		return json.getObject("data");
	}
}