package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Builder for station info page models. 
 * @author simon.schwantzer(at)im-c.de
 */
public class StationInfoBuilder {
	private final JsonObject json;
	
	/**
	 * Creates a new builder instance.
	 */
	public StationInfoBuilder() {
		json = new JsonObject();
		json.putArray("panels", new JsonArray());
	}
	
	/**
	 * Sets the site name.
	 * @param site Site name to display.
	 * @return Builder instance.
	 */
	public StationInfoBuilder setSite(String site) {
		json.putString("site", site);
		return this;
	}

	/**
	 * Sets the station name.
	 * @param station Station name to display.
	 * @return Builder instance.
	 */
	public StationInfoBuilder setStation(String station) {
		json.putString("station", station);
		return this;
	}
	
	/**
	 * Adds a panel to the station info page.
	 * @param panel Panel to add.
	 * @return Builder instance.
	 */
	public StationInfoBuilder addPanel(StationInfo.Panel panel) {
		JsonArray panels = json.getArray("panels");
		panels.addObject(panel.asJson());
		return this;
	}
	
	/**
	 * Builds the station info page.
	 * @return Station info page model.
	 * @throws IllegalArgumentException Not all required information is provided.
	 */
	public StationInfo build() throws IllegalArgumentException {
		return new StationInfo(json);
	}
}
