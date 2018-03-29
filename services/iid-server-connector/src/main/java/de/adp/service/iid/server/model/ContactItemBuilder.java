package de.adp.service.iid.server.model;


/**
 * Builder for service items used in the contact catalog. 
 * @author simon.schwantzer(at)im-c.de
 */
public class ContactItemBuilder extends ServiceItemBuilder<ContactItemBuilder> {
	/**
	 * Creates a new builder instance.
	 */
	public ContactItemBuilder() {
		json.putString("catalog", "contacts");
	}
	
	/**
	 * Sets the name to display for the contact. <b>Required.</b>
	 * @param displayName Display name.
	 * @return Builder instance.
	 */
	public ContactItemBuilder setDisplayName(String displayName) {
		json.putString("displayName", displayName);
		return this;
	}
	
	/**
	 * Sets the image to be displayed for the contact. Optional.
	 * @param imageUrl URL to load the image from.
	 * @return Builder instance.
	 */
	public ContactItemBuilder setImageUrl(String imageUrl) {
		json.putString("imageUrl", imageUrl);
		return this;
	}
	
	/**
	 * Sets the role to be displayed for the contact. Optional.
	 * @param role Role of the contact.
	 * @return Builder instance.
	 */
	public ContactItemBuilder setRole(String role) {
		json.putString("role", role);
		return this;
	}
	
	/**
	 * Sets the action to perform to initiate a voice call. Optional.
	 * @param action Action to perform.
	 * @return Builder instance.
	 */
	public ContactItemBuilder setPhone(Action action) {
		json.putObject("phone", action.asJson());
		return this;
	}
	
	/**
	 * Sets the action to perform to initiate a chat session. Optional.
	 * @param action Action to perform.
	 * @return Builder instance.
	 */
	public ContactItemBuilder setChat(Action action) {
		json.putObject("chat", action.asJson());
		return this;
	}
	
	/**
	 * Sets the action to perform to initiate a video call. Optional.
	 * @param action Action to perform.
	 * @return Builder instance.
	 */
	public ContactItemBuilder setVideo(Action action) {
		json.putObject("video", action.asJson());
		return this;
	}
	
	/**
	 * Builds the item.
	 * @return Service catalog item.
	 * @throws IllegalArgumentException Not all required information is provided.
	 */
	public ContactItem build() throws IllegalArgumentException {
		return new ContactItem(json);
	}
	
}
