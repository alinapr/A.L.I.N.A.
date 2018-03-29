package de.adp.service.iid.server.model;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a site overview page.
 * @author simon.schwantzer(at)im-c.de
 */
public class SiteOverview {
	/**
	 * Model for a station object listed on the site overview page.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public static class Station {
		private JsonObject json;
		private ContentBody content;
		private Action learningAction;
		
		/**
		 * Creates a station information for the site overview.
		 * @param station Station name to display.
		 * @param level Warning level.
		 * @param content Content to display.
		 * @param learningAction Action to perform when the learning symbol clicked. May be <code>null</code>.
		 */
		public Station(String station, Level level, ContentBody content, Action learningAction) {
			json = new JsonObject();
			json.putString("station", station);
			json.putString("level", level.toString().toLowerCase());
			json.putObject("content", content.asJson());
			this.content = content;
			if (learningAction != null) {
				json.putObject("learningAction", learningAction.asJson());
				this.learningAction = learningAction;
			}
		}
		
		protected Station(JsonObject json) throws IllegalArgumentException {
			this.json = json;
			validateJson(json);
			content = ContentBody.fromJson(json.getObject("content"));
			JsonObject actionObject = json.getObject("learningAction");
			if (actionObject != null) {
				learningAction = Action.fromJson(actionObject);
			}
		}
		
		private static void validateJson(JsonObject json) throws IllegalArgumentException {
			String station = json.getString("station");
			if (station == null) {
				throw new IllegalArgumentException("Missing station name (station).");
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
		
		/**
		 * Returns the JSON object wrapped by this model.
		 * @return JSON object representation of the model.
		 */
		public JsonObject asJson() {
			return json;
		}
		
		/**
		 * Returns the station name.
		 * @return Station name to display.
		 */
		public String getStation() {
			return json.getString("station");
		}
		
		/**
		 * Returns the warning level of the station.
		 * @return Warning level to display.
		 */
		public Level getLevel() {
			String level = json.getString("level");
			return Level.valueOf(level.toUpperCase());
		}
		
		/**
		 * Returns the content to be displayed for the station.
		 * @return Content body to display.
		 */
		public ContentBody getContentBody() {
			return content;
		}
		
		/**
		 * Returns the action performed when learning is requested.
		 * @return Learning action, may be <code>null</code>.
		 */
		public Action getLearningAction() {
			return learningAction;
		}
	}
	
	private final JsonObject json;
	private final List<Station> stations;
	
	/**
	 * Creates a new site overview wrapping the given JSON object.
	 * @param json JSON object representing a site overview.
	 * @throws IllegalArgumentException The given JSON object does not represent a site overview.
	 */
	public SiteOverview(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		stations = new ArrayList<>();
		for (Object station : json.getArray("stations")) {
			stations.add(new Station((JsonObject) station));
		}
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String site = json.getString("site");
		if (site == null) {
			throw new IllegalArgumentException("Missing site name (site).");
		}
		JsonArray stations = json.getArray("stations");
		if (stations == null) {
			throw new IllegalArgumentException("Missing station information (stations)."); 
		}
	}

	/**
	 * Returns the site for which an overview is given.
	 * @return Site name to display.
	 */
	public String getSite() {
		return json.getString("site");
	}
	
	/**
	 * Returns a list of all stations listen in the overview.
	 * @return List of stations. May be empty.
	 */
	public List<Station> getStations() {
		return stations;
	}
	
	public JsonObject asJson() {
		return json;
	}
}
