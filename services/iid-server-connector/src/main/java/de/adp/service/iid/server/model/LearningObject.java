package de.adp.service.iid.server.model;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a learning object.
 * @author simon.schwantzer(at)im-c.de
 */
public class LearningObject {
	
	/**
	 * Model for a chapter in a learning object.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Chapter {
		
		private final JsonObject json; 
		private final ContentBody contentBody;

		/**
		 * Creates a new chapter.
		 * @param caption Caption of the chapter.
		 * @param body Body of the chapter.
		 */
		public Chapter(String caption, ContentBody body) {
			json = new JsonObject();
			json.putString("caption", caption);
			contentBody = body;
			json.putObject("content", contentBody.asJson());
		}
		
		protected Chapter(JsonObject json) throws IllegalArgumentException {
			validateJson(json);
			this.json = json;
			contentBody = ContentBody.fromJson(json.getObject("content"));
		}
		
		private static void validateJson(JsonObject json) throws IllegalArgumentException {
			String caption = json.getString("caption");
			if (caption == null || caption.trim().isEmpty()) {
				throw new IllegalArgumentException("Missing a caption for the chapter (caption).");
			}
			JsonObject content = json.getObject("content");
			if (content == null) {
				throw new IllegalArgumentException("Missing content for the chapter (content).");
			}
		}
		
		/**
		 * Returns the caption of the chapter.
		 * @return Caption to display.
		 */
		public String getCaption() {
			return json.getString("caption");
		}
		
		/**
		 * Returns the body of the chapter.
		 * @return Content body to display.
		 */
		public ContentBody getBody() {
			return contentBody;
		}
		
		/**
		 * Returns the JSON object wrapped by this model.
		 * @return JSON object representing the chapter.
		 */
		public JsonObject asJson() {
			return json;
		}
	}
	
	private final JsonObject json;
	private final List<Chapter> chapters;
	
	/**
	 * Creates a new learning object by wrapping the given JSON object.
	 * @param json JSON object representing the learning object.
	 * @throws IllegalArgumentException The given JSON object does not represent a learning object.
	 */
	public LearningObject(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		this.chapters = new ArrayList<>();
		for (Object entry : json.getArray("chapters")) {
			JsonObject chapterJson = (JsonObject) entry;
			chapters.add(new Chapter(chapterJson));
		}
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String title = json.getString("title");
		if (title == null) {
			throw new IllegalArgumentException("Missing title for learning object (title).");
		}
		JsonArray chapters = json.getArray("chapters");
		if (chapters == null || chapters.size() == 0) {
			throw new IllegalArgumentException("No chapter defined (chapters).");
		}
	}
	
	/**
	 * Returns the title of the learning object.
	 * @return Title of the learning object.
	 */
	public String getTitle() {
		return json.getString("title");
	}
	
	/**
	 * Returns the chapters of the learning object.
	 * @return List of chapters.
	 */
	public List<Chapter> getChapters() {
		return chapters;
	}
	
	/**
	 * Returns the JSON representation of this model.
	 * @return JSON object representing the model.
	 */
	public JsonObject asJson() {
		return json;
	}
}
