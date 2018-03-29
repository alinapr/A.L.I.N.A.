package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Content body for assistance steps and learning object chapters.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class ContentBody {

	/**
	 * Type of the content body.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public enum Type {
		FRAME,
		PACKAGE,
		HTML,
		EMPTY
	}
	
	/**
	 * Content embedding a frame. 
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Frame extends ContentBody {
		/**
		 * Creates a new frame content body.
		 * @param src URL to be loaded in the frame.
		 */
		public Frame(String src) {
			super(Type.FRAME);
			json.putString("src", src);
		}
		
		protected Frame(JsonObject json) throws IllegalArgumentException {
			super(json);
			validateJson();
		}
		
		/**
		 * Returns the source for the frame.
		 * @return URL to be loaded in the frame.
		 */
		public String getFrameSrc() {
			return json.getString("src");
		}
		
		private void validateJson() throws IllegalArgumentException {
			String src = json.getString("src");
			if (src == null || src.trim().isEmpty()) {
				throw new IllegalArgumentException("Misssing frame source (src).");
			}
		}
	}
	
	/**
	 * Content displaying a content package.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Package extends ContentBody {
		/**
		 * Creates a content package body.
		 * @param id Identifier of the content package to display.
		 */
		public Package(String id) {
			super(Type.PACKAGE);
			json.putString("id", id);
		}
		
		protected Package(JsonObject json) throws IllegalArgumentException {
			super(json);
			validateJson();
		}
		
		/**
		 * Returns the content package to be displayed.
		 * @return Content package identifier.
		 */
		public String getPackageId() {
			return json.getString("id");
		}
		
		private void validateJson() throws IllegalArgumentException {
			String id = json.getString("id");
			if (id == null || id.trim().isEmpty()) {
				throw new IllegalArgumentException("Misssing package identifier (id).");
			}
		}
	}
	
	/**
	 * Content embedding a HTML snippet.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class HTML extends ContentBody {
		/**
		 * Creates a html content body.
		 * @param body HTML string to display.
		 */
		public HTML(String body) {
			super(Type.HTML);
			json.putString("body", body);
		}
		
		protected HTML(JsonObject json) throws IllegalArgumentException {
			super(json);
			validateJson();
		}
		
		/**
		 * Returns the HTML snippet to embed.
		 * @return HTML string.
		 */
		public String getHtmlBody() {
			return json.getString("body");
		}
		
		private void validateJson() throws IllegalArgumentException {
			String body = json.getString("body");
			if (body == null || body.trim().isEmpty()) {
				throw new IllegalArgumentException("Misssing html body (body).");
			}
		}
	}
	
	/**
	 * Empty content, i.e., only the meta information is displayed.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Empty extends ContentBody {
		/**
		 * Creates an empty body.
		 */
		public Empty() {
			super(Type.EMPTY);
		}
	}
	
	protected final JsonObject json;
	
	protected ContentBody(Type type) {
		json = new JsonObject();
		json.putString("type", type.toString().toLowerCase());
	}
	
	protected ContentBody(JsonObject json) {
		this.json = json;
	}
	
	/**
	 * Parses an JSON object to retrieve a typed content body.
	 * @param json JSON object to parse.
	 * @return Typed content body.
	 * @throws IllegalArgumentException The given JSON object does not encode a content body.
	 */
	public static ContentBody fromJson(JsonObject json) throws IllegalArgumentException {
		String type = json.getString("type");
		if (type == null) {
			throw new IllegalArgumentException("Missing content type (type).");
		}
		switch (type) {
		case "html":
			return new HTML(json);
		case "package":
			return new Package(json);
		case "frame":
			return new Frame(json);
		case "empty":
			return new Empty();
		default:
			throw new IllegalArgumentException("Invalid content type: " + type);
		}
	}
	
	/**
	 * Returns the JSON object wrapped by this model.
	 * @return JSON model representing the content body.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the type of the content body.
	 * @return Content body type.
	 */
	public Type getType() {
		return Type.valueOf(json.getString("type").toUpperCase());
	}
}
