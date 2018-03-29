package de.adp.service.iid.server.model;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a station info page.
 * @author simon.schwantzer(at)im-c.de
 */
public class StationInfo {
	
	/**
	 * Model for a panel on the station info page.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Panel {
		private JsonObject json;
		private ContentBody content;
		private Action learningAction;
		
		/**
		 * Creates a panel for the station info page.
		 * @param title Title for the panel.
		 * @param level Warning level.
		 * @param content Content to display.
		 * @param learningAction Action to perform when the learning symbol clicked. May be <code>null</code>.
		 */
		public Panel(String title, Level level, ContentBody content, Action learningAction) {
			json = new JsonObject();
			json.putString("title", title);
			json.putString("level", level.toString().toLowerCase());
			json.putObject("content", content.asJson());
			this.content = content;
			if (learningAction != null) {
				json.putObject("learningAction", learningAction.asJson());
				this.learningAction = learningAction;
			}
		}
		
		protected Panel(JsonObject json) throws IllegalArgumentException {
			this.json = json;
			validateJson(json);
			content = ContentBody.fromJson(json.getObject("content"));
			JsonObject actionObject = json.getObject("learningAction");
			if (actionObject != null) {
				learningAction = Action.fromJson(actionObject);
			}
		}
		
		private static void validateJson(JsonObject json) throws IllegalArgumentException {
			String title = json.getString("title");
			if (title == null) {
				throw new IllegalArgumentException("Missing panel title (title).");
			}
			
			String level = json.getString("level");
			if (level == null) {
				throw new IllegalArgumentException("Missing station level (level).");
			} else {
				Level.valueOf(level.toUpperCase());
			}
			
			JsonObject content = json.getObject("content");
			if (content == null) {
				throw new IllegalArgumentException("Missing content to display (content).");
			}
		}
		
		public JsonObject asJson() {
			return json;
		}
		
		/**
		 * Returns the title of the panel.
		 * @return Title to display.
		 */
		public String getTitle() {
			return json.getString("title");
		}
		
		/**
		 * Returns the warning level to display.
		 * @return Warning level to display.
		 */
		public Level getLevel() {
			String level = json.getString("level");
			return Level.valueOf(level.toUpperCase());
		}
		
		/**
		 * Returns the content of the panel.
		 * @return Content body.
		 */
		public ContentBody getContentBody() {
			return content;
		}
		
		/**
		 * Returns the action performed when learning is requested.
		 * @return Learning action. May be <code>null</code>.
		 */
		public Action getLearningAction() {
			return learningAction;
		}
	}
	
	private final JsonObject json;
	private final List<Panel> panels;
	
	/**
	 * Creates a new station info by wrapping the given JSON object.
	 * @param json JSON object representing a station info model.
	 * @throws IllegalArgumentException The given JSON object does not represent a valid station info model.
	 */
	public StationInfo(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		panels = new ArrayList<>();
		for (Object panel : json.getArray("panels")) {
			panels.add(new Panel((JsonObject) panel));
		}
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String site = json.getString("site");
		if (site == null) {
			throw new IllegalArgumentException("Missing site name (site).");
		}
		String station = json.getString("station");
		if (station == null) {
			throw new IllegalArgumentException("Missing station name (station).");
		}
		JsonArray panels = json.getArray("panels");
		if (panels == null) {
			throw new IllegalArgumentException("Missing panels for site info page (panels)."); 
		}
	}
	
	/**
	 * Returns the site name.
	 * @return Site name to display.
	 */
	public String getSite() {
		return json.getString("site");
	}
	
	/**
	 * Returns the station name.
	 * @return Station name to display.
	 */
	public String getStation() {
		return json.getString("station");
	}
	
	/**
	 * Returns the panels of the station info page.
	 * @return List of panels. May be empty.
	 */
	public List<Panel> getPanels() {
		return panels;
	}

	public JsonObject asJson() {
		return json;
	}
}
