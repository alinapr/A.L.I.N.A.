package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a item in the contacts catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class ContactItem extends ServiceItem {
	private Action phoneAction;
	private Action chatAction;
	private Action videoAction;
	
	protected ContactItem(JsonObject json) throws IllegalArgumentException {
		super(json);
		validateJson(json);
		JsonObject phone = json.getObject("phone");
		phoneAction = (phone != null) ? Action.fromJson(phone) : null;
		JsonObject chat = json.getObject("chat");
		chatAction = (chat != null) ? Action.fromJson(chat) : null;
		JsonObject video = json.getObject("video");
		videoAction = (chat != null) ? Action.fromJson(video) : null;
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String displayName = json.getString("displayName");
		if (displayName == null || displayName.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing display name for contact (displayName).");
		}
	}
	
	/**
	 * Returns the name to display for the contact.
	 * @return Display name of the contact.
	 */
	public String getDisplayName() {
		return json.getString("displayName");
	}
	
	/**
	 * Returns the image to be displayed for the contact.
	 * @return URL to retrieve an image for the contact from. May be <code>null</code>.
	 */
	public String getImageUrl() {
		return json.getString("imageUrl");
	}
	
	/**
	 * Returns the role of the contact.
	 * @return Role of the contact to display. May be <code>null</code>.
	 */
	public String getRole() {
		return json.getString("role");
	}
	
	/**
	 * Returns the action to perform to initiate voice call.
	 * @return Action to initiate a voice call or <code>null</code> if voice calls are not possible.
	 */
	public Action getPhone() {
		return phoneAction;
	}
	
	/**
	 * Returns the action to perform to initiate a chat.
	 * @return Action to initiate a chat or <code>null</code> if chatting is not possible.
	 */
	public Action getChat() {
		return chatAction;
	}
	
	/**
	 * Returns the action to perform to initiate vdieo call.
	 * @return Action to initiate a video call or <code>null</code> if video calls are not possible.
	 */
	public Action getViceo() {
		return videoAction;
	}
}
