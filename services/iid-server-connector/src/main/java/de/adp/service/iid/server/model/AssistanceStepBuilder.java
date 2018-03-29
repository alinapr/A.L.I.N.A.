package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Builder for assistance steps.
 * @author simon.schwantzer(at)im-c.de
 */
public class AssistanceStepBuilder {
	protected JsonObject json;
	
	/**
	 * Creates a new builder instance.
	 */
	public AssistanceStepBuilder() {
		json = new JsonObject();
		JsonObject navigation = new JsonObject();
		json.putObject("navigation", navigation);
		navigation.putArray("buttons", new JsonArray());
	}
	
	private JsonObject getOrCreateTitle() {
		JsonObject title = json.getObject("title");
		if (title == null) {
			title = new JsonObject();
			json.putObject("title", title);
		}
		return title;
	}
	
	/**
	 * Sets a title to display. Optional.
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setTitle(String title) {
		getOrCreateTitle().putString("current", title);
		return this;
	}
	
	/**
	 * Sets the previous title to display. Optional.
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setPreviousTitle(String title) {
		getOrCreateTitle().putString("previous", title);
		return this;
	}
	
	/**
	 * Sets the next title to display. Optional.
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setNextTitle(String title) {
		getOrCreateTitle().putString("next", title);
		return this;
	}
	
	/**
	 * Sets a text message to show in the context of the content. Optional.
	 * @param info Text message to show.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setInfo(String info) {
		json.putString("info", info);
		return this;
	}
	
	/**
	 * Sets the progress for the assistance process. <b>Required.</b>
	 * @param progress Progress between 0.0 (started) to 1.0 (completed) inclusively.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setProgress(Double progress) {
		json.putNumber("progress", progress);
		return this;
	}
	
	/**
	 * Adds a warning information to be displayed with the context. Optional.
	 * @param message Text message to display.
	 * @param iconPath Path of the warning icon/sign to display. May either be a relative path or an absolute URL. 
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder addWarning(String message, String iconPath) {
		JsonObject warning = new JsonObject();
		warning.putString("message", message);
		warning.putString("icon", iconPath);
		json.putObject("warning", warning);
		JsonArray warnings = json.getArray("warnings");
		if (warnings == null) {
			warnings = new JsonArray();
			json.putArray("warnings", warnings);
		}
		warnings.addObject(warning);
		return this;
	}
	
	/**
	 * Sets the content for the assistance step. <b>Required.</b>
	 * @param contentBody Content to display.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setContentBody(ContentBody contentBody) {
		json.putObject("content", contentBody.asJson());
		return this;
	}
	
	/**
	 * Sets the action which should be executed if a client view is closed. Optional.
	 * @param action Action to perform on close.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setCloseAction(Action action) {
		JsonObject navigation = json.getObject("navigation");
		navigation.putObject("close", action.asJson());
		return this;
	}
	
	/**
	 * Sets the action which should be executed if a client navigates back. Optional.
	 * @param action Action to perform on back navigation.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setBackAction(Action action) {
		JsonObject navigation = json.getObject("navigation");
		navigation.putObject("back", action.asJson());
		return this;
	}
	
	/**
	 * Sets the action which should be executed if a client opens the notes. Optional.
	 * @param action Action to perform on opening the notes.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setNotesAction(Action action) {
		JsonObject navigation = json.getObject("navigation");
		navigation.putObject("notes", action.asJson());
		return this;
	}
	
	/**
	 * Sets the action which should be executed if a client opens the contacts. Optional.
	 * @param action Action to perform on opening the contacts.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setContactsAction(Action action) {
		JsonObject navigation = json.getObject("navigation");
		navigation.putObject("contacts", action.asJson());
		return this;
	}
	
	/**
	 * Sets the action which should be executed if a client opens a learning nugget. Optional.
	 * @param action Action to perform on opening a learning nugget.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder setKnowledgeAction(Action action) {
		JsonObject navigation = json.getObject("navigation");
		navigation.putObject("knowledge", action.asJson());
		return this;
	}
	
	/**
	 * Adds a action button with a text display.
	 * @param id Identifier for the button.
	 * @param text Text to be displayed on the button.
	 * @param action Action to perform when the button is clicked.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder addActionButtonWithText(String id, String text, Action action) {
		JsonObject actionButton = new JsonObject();
		actionButton.putString("id", id);
		actionButton.putString("text", text);
		actionButton.putObject("action", action.asJson());
		JsonArray buttons = json.getObject("navigation").getArray("buttons");
		buttons.addObject(actionButton);
		return this;
	}
	
	/**
	 * Adds a action button with a text display. 
	 * @param id Identifier for the button.
	 * @param iconPath Path of the icon to display. May either be a relative path or an absolute URL.
	 * @param action Action to perform when the button is clicked.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder addActionButtonWithIcon(String id, String iconPath, Action action) {
		JsonObject actionButton = new JsonObject();
		actionButton.putString("id", id);
		actionButton.putString("icon", iconPath);
		actionButton.putObject("action", action.asJson());
		JsonArray buttons = json.getObject("navigation").getArray("buttons");
		buttons.addObject(actionButton);
		return this;
	}
	
	/**
	 * Adds a action button with a text display. 
	 * @param id Identifier for the button.
	 * @param text Text to be displayed on the button.
	 * @param iconPath Path of the icon to display. May either be a relative path or an absolute URL.
	 * @param action Action to perform when the button is clicked.
	 * @return Builder instance.
	 */
	public AssistanceStepBuilder addActionButtonWithTextAndIcon(String id, String text, String iconPath, Action action) {
		JsonObject actionButton = new JsonObject();
		actionButton.putString("id", id);
		actionButton.putString("text", text);
		actionButton.putString("icon", iconPath);
		actionButton.putObject("action", action.asJson());
		JsonArray buttons = json.getObject("navigation").getArray("buttons");
		buttons.addObject(actionButton);
		return this;
	}
	
	/**
	 * Builds the assistance step.
	 * @return Assistance step to display.
	 * @throws IllegalArgumentException The given data does not represent a valid assistance step.
	 */
	public AssistanceStep build() throws IllegalArgumentException {
		return new AssistanceStep(json);
	}
}
