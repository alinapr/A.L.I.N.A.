package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a popup window.
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class Popup {
	private final JsonObject json;
	private final ContentBody body;
	private final Action closeAction;
	
	/**
	 * Creates a new model wrapping the given JSON object.
	 * @param json JSON object representing a popup.
	 * @throws IllegalArgumentException The given JSON object is no valid representation of a popup.
	 */
	public Popup(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		
		body = ContentBody.fromJson(json.getObject("body"));
		JsonObject closeObject = json.getObject("close");
		closeAction = closeObject != null ? Action.fromJson(closeObject) : null;
		
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		if (json.getString("title") == null) {
			throw new IllegalArgumentException("Missing title [title].");
		}
		
		if (json.getObject("body") == null) {
			throw new IllegalArgumentException("Missing body [body].");
		}
	}
	
	/**
	 * Returns the JSON object wrapped by this model.
	 * @return JSON object representing the popup.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the title of the popup.
	 * @return Title to display for the popup.
	 */
	public String getTitle() {
		return json.getString("title");
	}
	
	/**
	 * Returns the body of the popup.
	 * @return Content body of the popup.
	 */
	public ContentBody getBody() {
		return body;
	}
	
	/**
	 * Returns the action to be performed when the close button is pressed.
	 * @return Action to be performed or <code>null</code>.
	 */
	public Action getCloseAction() {
		return closeAction;
	}
}
