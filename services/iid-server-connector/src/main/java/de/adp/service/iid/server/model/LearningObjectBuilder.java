package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import de.adp.service.iid.server.model.LearningObject.Chapter;

/**
 * Builder for learning objects.
 * @author simon.schwantzer(at)im-c.de
 */
public class LearningObjectBuilder {
	protected JsonObject json;
	
	/**
	 * Creates a new builder instance.
	 */
	public LearningObjectBuilder() {
		json = new JsonObject();
		JsonArray chapters = new JsonArray();
		json.putArray("chapters", chapters);
	}
	
	/**
	 * Sets a title for the learning object.
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public LearningObjectBuilder setTitle(String title) {
		json.putString("title", title);
		return this;
	}
	
	/**
	 * Adds a chapter to the learning object.
	 * @param chapter Chapter to add.
	 * @return Builder instance.
	 */
	public LearningObjectBuilder addChapter(Chapter chapter) {
		json.getArray("chapters").addObject(chapter.asJson());
		return this;
	}
	
	/**
	 * Builds the learning object.
	 * @return Learning object.
	 * @throws IllegalArgumentException Failed to build a learning object with the given information.
	 */
	public LearningObject build() throws IllegalArgumentException {
		return new LearningObject(json);
	}
}