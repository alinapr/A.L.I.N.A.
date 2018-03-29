package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class PopupBuilder {
	private final JsonObject json;
	
	public PopupBuilder() {
		json = new JsonObject();
	}
	
	/**
	 * Sets the title of the popup. <b>Required.</b>
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public PopupBuilder setTitle(String title) {
		json.putString("title", title);
		return this;
	}
	
	/**
	 * Sets the body of the popup. <b>Required.</b>
	 * @param body Body to display.
	 * @return Builder instance.
	 */
	public PopupBuilder setBody(ContentBody body) {
		json.putObject("body", body.asJson());
		return this;
	}
	
	/**
	 * Sets a action to perform when the popup is closed. Optional. 
	 * @param action Action to perform.
	 * @return Builder instance.
	 */
	public PopupBuilder setCloseAction(Action action) {
		json.putObject("close", action.asJson());
		return this;
	}
	
	/**
	 * Adds an button to the navigation bar.
	 * @param text Text to display. May be <code>null</code> if an icon is set.
	 * @param iconUrl URL of the icon to display. May be <code>null</code> if a text is set.
	 * @param action Action to perform when the button is pressed.
	 * @return Builder instance.
	 */
	public PopupBuilder addActionButton(String text, String iconUrl, Action action) {
		JsonArray buttons = json.getArray("buttons");
		if (buttons == null) {
			buttons = new JsonArray();
			json.putArray("buttons", buttons);
		}
		
		JsonObject actionButton = new JsonObject();
		if (text != null) actionButton.putString("text", text);
		if (iconUrl != null) actionButton.putString("icon", iconUrl);
		actionButton.putObject("action", action.asJson());
		buttons.addObject(actionButton);
		return this;
	}
	
	/**
	 * Builds the popup.
	 * @return Popup object.
	 * @throws IllegalArgumentException The given information is invalid. 
	 */
	public Popup build() throws IllegalArgumentException {
		return new Popup(json);
	}
}
