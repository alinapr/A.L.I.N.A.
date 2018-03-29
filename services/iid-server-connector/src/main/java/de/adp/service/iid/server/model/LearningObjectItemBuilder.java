package de.adp.service.iid.server.model;


/**
 * Builder for service items displayed in the learning object catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class LearningObjectItemBuilder extends ServiceItemBuilder<LearningObjectItemBuilder>{
	/**
	 * Creates a new builder instance.
	 */
	public LearningObjectItemBuilder() {
		json.putString("catalog", "learningObjects");
	}
	
	/**
	 * Sets the title to be displayed in the item. <b>Required.</b>
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder setTitle(String title) {
		json.putString("title", title);
		return this;
	}
	
	/**
	 * Sets the image to be displayed in the item. Optional.
	 * @param imageUrl URL to retrieve the image file from.
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder setImageUrl(String imageUrl) {
		json.putString("imageUrl", imageUrl);
		return this;
	}
	
	/**
	 * Sets the progress the current user has made in the related learning object. Optional.
	 * @param progress Value between 0.0 (not started) and 1.0 (completed).
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder setProgress(double progress) {
		json.putNumber("progress", progress);
		return this;
	}
	
	/**
	 * Flags image media to be used in the related learning object. Optional, defaults to <code>false</code>.
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder setImages() {
		json.putBoolean("hasImages", true);
		return this;
	}
	
	/**
	 * Flags video media to be used in the related learning object. Optional, defaults to <code>false</code>.
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder setVideo() {
		json.putBoolean("hasVideo", true);
		return this;
	}
	
	/**
	 * Flags audio media to be used in the related learning object. Optional, defaults to <code>false</code>.
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder hasAudio() {
		json.putBoolean("hasAudio", true);
		return this;
	}
	
	/**
	 * Flags 3D media to be used in the related learning object. Optional, defaults to <code>false</code>.
	 * @return Builder instance.
	 */
	public LearningObjectItemBuilder has3D() {
		json.putBoolean("has3D", true);
		return this;
	}
	
	/**
	 * Builds the service item.
	 * @return Service item to be displayed in the learning objects catalog.
	 * @throws IllegalArgumentException Not all required information is available.
	 */
	public LearningObjectItem build() throws IllegalArgumentException {
		return new LearningObjectItem(json);
	}
}
