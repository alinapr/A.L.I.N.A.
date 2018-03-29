package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a button triggering an action.
 * @author simon.schwantzer(at)im-c.de
 */
public class ActionButton {
	public static class Builder {
		private final JsonObject json;
		
		public Builder() {
			json = new JsonObject();
		}
		
		public Builder setText(String text) {
			json.putString("text", text);
			return this;
		}
		
		public Builder setIconUrl(String iconUrl) {
			json.putString("icon", iconUrl);
			return this;
		}
		
		public Builder setAction(Action action) {
			json.putObject("action", action.asJson());
			return this;
		}
		
		public ActionButton build() throws IllegalArgumentException {
			return new ActionButton(json);
		}
	}
	
	private final JsonObject json;
	private final Action action;
	
	public ActionButton(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		
		JsonObject actionObject = json.getObject("action");
		action = Action.fromJson(actionObject);
	}
	
	private static final void validateJson(JsonObject json) throws IllegalArgumentException {
		String text = json.getString("text");
		String icon = json.getString("icon");
		if (text == null && icon == null) {
			throw new IllegalArgumentException("Either a display text [text] or an icon url [icon] is required.");
		}
		if (json.getObject("action") == null) {
			throw new IllegalArgumentException("Missing action to perform when the button is clicked [action].");
		}
	}
	
	/**
	 * Returns the JSON object wrapped by this model.
	 * @return JSON object representing an action button.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the display text for the button.
	 * @return Display text. May be <code>null</code> if an icon url is provided. 
	 */
	public String getText() {
		return json.getString("text");
	}
	
	/**
	 * Returns the icon to display.
	 * @return Icon url. May be <code>null</code> of a display text is provided.
	 */
	public String getIconUrl() {
		return json.getString("icon");
	}
	
	/**
	 * Action to perform when the button is clicked.
	 * @return Action to perform.
	 */
	public Action getAction() {
		return action;
	}
}
