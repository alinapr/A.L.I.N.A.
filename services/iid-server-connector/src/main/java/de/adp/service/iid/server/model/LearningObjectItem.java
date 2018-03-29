package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a service item displayed in the learning objects catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class LearningObjectItem extends ServiceItem {
	protected LearningObjectItem(JsonObject json) throws IllegalArgumentException {
		super(json);
		validateJson(json);
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String title = json.getString("title");
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing title for instruction item (title).");
		}
	}
	
	/**
	 * Returns the title to be displayed in the item.
	 * @return Title to display.
	 */
	public String getTitle() {
		return json.getString("title");
	}
	
	/**
	 * Returns the image to be displayed in the item.
	 * @return URL to retrieve image file from.
	 */
	public String getImageUrl() {
		return json.getString("imageUrl");
	}
	
	/**
	 * Returns the current learning progress of the current user. 
	 * @return Value between 0.0 (not started) and (1.0) completed.
	 */
	public double getProgress() {
		Number progress = json.getNumber("progress");
		if (progress != null) {
			return progress.doubleValue();
		} else {
			return 0.0d;
		}
	}
	
	/**
	 * Checks if the learning object contains image media.
	 * @return <code>true</code> if the media type is contained, otherwise <code>false</code>.
	 */
	public boolean hasImages() {
		return json.getBoolean("hasImages", false);
	}
	
	/**
	 * Checks if the learning object contains video media.
	 * @return <code>true</code> if the media type is contained, otherwise <code>false</code>.
	 */
	public boolean hasVideo() {
		return json.getBoolean("hasVideo", false);
	}
	
	/**
	 * Checks if the learning object contains audio media.
	 * @return <code>true</code> if the media type is contained, otherwise <code>false</code>.
	 */
	public boolean hasAudio() {
		return json.getBoolean("hasAudio", false);
	}
	
	/**
	 * Checks if the learning object contains 3D media.
	 * @return <code>true</code> if the media type is contained, otherwise <code>false</code>.
	 */
	public boolean has3D() {
		return json.getBoolean("has3D", false);
	}
}
