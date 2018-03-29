package de.adp.service.iid.server.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a notification send to the user.
 * @author simon.schwantzer(at)im-c.de
 */
public class Notification {
	
	/**
	 * Builder for notifications.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Builder {
		private JsonObject json;
		
		/**
		 * Creates a new builder instance.
		 * @param id Identifier for the notification.
		 * @param message Short message to display.
		 * @param level Level of the notification.
		 */
		public Builder(String id, String message, Level level) {
			json = new JsonObject();
			json.putString("id", id);
			json.putString("message", message);
			json.putString("level", level.name().toLowerCase());
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			json.putString("created", DatatypeConverter.printDateTime(cal));
		}
		
		/**
		 * Sets the description to be displayed in a detail view. 
		 * @param description Description for the notification.
		 * @return Builder instance.
		 */
		public Builder setDescription(String description) {
			json.putString("description", description);
			return this;
		}
		
		/**
		 * Sets the URL of a image to display in a detail view.
		 * @param imageUrl URL of a image to display.
		 * @return Builder instance.
		 */
		public Builder setImageUrl(String imageUrl) {
			json.putString("imageUrl", imageUrl);
			return this;
		}
		
		/**
		 * Sets a catalog item to be linked with the notification.
		 * @param serviceItemId Identifier for a service catalog item.
		 * @return Builder instance.
		 */
		public Builder setRelatedItem(String serviceItemId) {
			json.putString("relatedItem", serviceItemId);
			return this;
		}
		
		/**
		 * Sets the catalog to be linked with the notification.
		 * @param catalogId Identifier for a service catalog.
		 * @return Builder instance.
		 */
		public Builder setRelatedCatalog(String catalogId) {
			json.putString("relatedCatalog", catalogId);
			return this;
		}
		
		/**
		 * Adds an action button to be displayed with the notification.
		 * @param actionButton Action button to display.
		 * @return Builder instance.
		 */
		public Builder addActionButton(ActionButton actionButton) {
			JsonArray buttons = json.getArray("buttons");
			if (buttons == null) {
				buttons = new JsonArray();
				json.putArray("buttons", buttons);
			}
			buttons.addObject(actionButton.asJson());
			return this;
		}
		
		/**
		 * Builds the notification.
		 * @return Notification object.
		 * @throws IllegalArgumentException The given information is invalid.
		 */
		public Notification build() throws IllegalArgumentException {
			return new Notification(json);
		}
	}
	
	private JsonObject json;
	private List<ActionButton> actionButtons;
	
	/**
	 * Creates a notification object wrapping the given JSON object.
	 * @param json JSON object representing the notification.
	 * @throws IllegalArgumentException The given JSON object does not represent a notification.
	 */
	public Notification(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		
		actionButtons = new ArrayList<>();
		JsonArray buttons = json.getArray("buttons");
		if (buttons != null) for (Object entry : buttons) {
			ActionButton button = new ActionButton((JsonObject) entry);
			actionButtons.add(button);
		}
	}
	
	private void validateJson(JsonObject json) throws IllegalArgumentException {
		String id = json.getString("id");
		if (id == null || id.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing id for notification (id).");
		}
		
		String message = json.getString("message");
		if (message == null || message.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing message for notification (message)");
		}
		
		String levelString = json.getString("level");
		Level.fromString(levelString); // Throws IAE if null or invalid.
	}
	
	/**
	 * Returns the model as JSON object.
	 * @return JSON object representing the model.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the unique identifier for this notification.
	 * @return Identifier for the notification.
	 */
	public String getId() {
		return json.getString("id");
	}
	
	/**
	 * Returns the message to display.
	 * @return Message.
	 */
	public String getMessage() {
		return json.getString("message");
	}
	
	/**
	 * Returns the description to display in a detailed view.
	 * @return Describing text, may be <code>null</code>.
	 */
	public String getDescription() {
		return json.getString("description");
	}
	
	/**
	 * Returns the URL for an image to display in a detailed view.
	 * @return URL of a image or <code>null</code> if not set.
	 */
	public String getImageUrl() {
		return json.getString("imageUrl");
	}
	
	/**
	 * Returns the level of the notification indicating it importance.
	 * @return Level of the notification.
	 */
	public Level getLevel() {
		return Level.fromString(json.getString("level"));
	}
	
	/**
	 * Returns the related item for this notification. May be <code>null</code>.
	 * @return Service item identifier.
	 */
	public String getRelatedItemId() {
		return json.getString("relatedItem");
	}
	
	/**
	 * Returns the related catalog for this notification. May be <code>null</code>.
	 * @return Service catalog identifier.
	 */
	public String getRelatedCatalogId() {
		return json.getString("realtedCatalog");
	}
	
	/**
	 * Returns the actions available.  
	 * @return List of action buttons.
	 */
	public List<ActionButton> getActionButtons() {
		return actionButtons;
	}
}
