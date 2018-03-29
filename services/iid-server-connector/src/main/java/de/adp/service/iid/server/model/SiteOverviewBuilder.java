package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Builder for site overview page updates.
 * @author simon.schwantzer(at)im-c.de
 */
public class SiteOverviewBuilder {
	private final JsonObject json;
	
	/**
	 * Creates a new builder instance.
	 */
	public SiteOverviewBuilder() {
		json = new JsonObject();
		json.putArray("stations", new JsonArray());
	}
	
	/**
	 * Sets the site name. <b>Required.</b>
	 * @param site Site name to display.
	 * @return Builder instance.
	 */
	public SiteOverviewBuilder setSite(String site) {
		json.putString("site", site);
		return this;
	}
	
	/**
	 * Adds a station panel to the overview page.
	 * @param station Station panel to add.
	 * @return Builder instance.
	 */
	public SiteOverviewBuilder addStation(SiteOverview.Station station) {
		JsonArray stations = json.getArray("stations");
		stations.addObject(station.asJson());
		return this;
	}
	
	/**
	 * Builds the site overview page.
	 * @return Site overview page model.
	 * @throws IllegalArgumentException Not all required information is provided.
	 */
	public SiteOverview build() throws IllegalArgumentException {
		return new SiteOverview(json);
	}
}
