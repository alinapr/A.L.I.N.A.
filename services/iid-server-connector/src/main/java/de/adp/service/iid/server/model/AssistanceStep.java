package de.adp.service.iid.server.model;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a assistance step.
 * @author simon.schwantzer(at)im-c.de
 */
public class AssistanceStep {
	
	public static class Warning {
		private final JsonObject json;
		
		public Warning(String message, String iconPath) {
			json = new JsonObject();
			json.putString("message", message);
			json.putString("icon", iconPath);
		}
		
		public Warning(JsonObject json) throws IllegalArgumentException {
			validateJson(json);
			this.json = json;
		}
		
		private static void validateJson(JsonObject json) throws IllegalArgumentException {
			String message = json.getString("message");
			if (message == null) {
				throw new IllegalArgumentException("Missing warning message (message).");
			}
			String icon = json.getString("icon");
			if (icon == null) {
				throw new IllegalArgumentException("Missing warning icon path (icon).");
			}
		}
		
		public String getMessage() {
			return json.getString("message");
		}
		
		public String getIconPath() {
			return json.getString("iconPath");
		}
	}
	
	protected final JsonObject json;
	protected final ContentBody contentBody;
	protected final List<Warning> warnings;
	
	/**
	 * Creates a new display update by wrapping the given JSON object.
	 * @param json JSON object representing the display update.
	 * @throws IllegalArgumentException The given JSON object does not represent a display update.
	 */
	public AssistanceStep(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		JsonObject content = json.getObject("content");
		contentBody = ContentBody.fromJson(content);
		warnings = new ArrayList<>();
		JsonArray warningsArray = json.getArray("warnings");
		if (warningsArray != null) {
			for (Object entry : warningsArray) {
				warnings.add(new Warning((JsonObject) entry));
			}
		}
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		JsonObject content = json.getObject("content");
		if (content == null) {
			throw new IllegalArgumentException("Missing content information (content).");
		}
		JsonObject navigation = json.getObject("navigation");
		if (navigation == null || navigation.getArray("buttons") == null) {
			throw new IllegalArgumentException("Missing navigation (navigation, navigation.buttons).");
		}
		Number progress = json.getNumber("progress");
		if (progress == null) {
			throw new IllegalArgumentException("Missing progress.");
		}		
	}
	
	/**
	 * Returns the JSON representation of this model.
	 * @return JSON object representing the model.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Imports the content package descriptor.
	 * @param descriptor JSON object with metadata for the content package.
	 */
	public void importDescriptor(JsonObject descriptor, String baseUrl) {
		JsonObject content = contentBody.asJson();
		if (descriptor.getString("main") != null) {
			content.putString("main", baseUrl + descriptor.getString("main"));
			content.putString("mimeType", descriptor.getString("mimeType"));
		}
		
		String lastUpdate = descriptor.getString("lastUpdate");
		if (lastUpdate != null) content.putString("lastUpdate", lastUpdate);
		
		String version = descriptor.getString("version");
		if (version != null) content.putString("version", version);
		
		String title = descriptor.getString("title");
		if (title != null) {
			JsonObject titleContainer = json.getObject("title");
			if (titleContainer == null) {
				titleContainer = new JsonObject();
				json.putObject("title", titleContainer);
			}
			titleContainer.putString("current", title);
		}
		
		String info = descriptor.getString("info");
		if (info != null) json.putString("info", info);
		
		JsonArray newWarnings = descriptor.getArray("warnings");
		if (newWarnings != null) {
			JsonArray existingWarnings = json.getArray("warnings");
			if (existingWarnings == null) {
				existingWarnings = new JsonArray();
				json.putArray("warnings", existingWarnings);
			}
			for (Object entry : newWarnings) {
				existingWarnings.add(entry);
			}
		}
	}
	
	/**
	 * Returns the current title to be display.
	 * @return Title to display.
	 */
	public String getTitle() {
		JsonObject title = json.getObject("title");
		return title != null ? title.getString("current") : null;
	}
	
	/**
	 * Returns the previous title to be displayed.
	 * @return Title to display.
	 */
	public String getPreviousTitle() {
		JsonObject title = json.getObject("title");
		return title != null ? title.getString("previous") : null;
	}
	
	/**
	 * Returns the next title to be displayed.
	 * @return Title to display.
	 */
	public String getNextTitle() {
		JsonObject title = json.getObject("title");
		return title != null ? title.getString("next") : null;
	}
	
	/**
	 * Returns the overall progress of the assistance.
	 * @return Value between 0.0 (started) an 1.0 (completed) inclusively.
	 */
	public Double getProgress() {
		Number progress = json.getNumber("progress");
		return progress.doubleValue();
	}
	
	/**
	 * Returns the info string to be displayed.
	 * @return Info string.
	 */
	public String getInfo() {
		return json.getString("info");
	}
	
	/**
	 * Returns the list of warnings related to this step.
	 * @return List of warnings. May be empty.
	 */
	public List<Warning> getWarnings() {
		return warnings;
	}
	
	/**
	 * Returns the content to display.
	 * @return Content body.
	 */
	public ContentBody getContent() {
		return contentBody;
	}
	
	/**
	 * Returns the navigation settings.
	 * @return JSON object encoding the navigation options.
	 */
	public JsonObject getNavigationObject() {
		return json.getObject("navigation");
	}
}
