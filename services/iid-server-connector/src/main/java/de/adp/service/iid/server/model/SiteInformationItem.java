package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Service catalog item for the site information catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class SiteInformationItem extends ServiceItem {
	public enum State {
		WARNING,
		ERROR,
		RUNNING,
		STANDBY,
		OFFLINE
	}
	
	protected SiteInformationItem(JsonObject json) throws IllegalArgumentException {
		super(json);
		validateJson(json);
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String state = json.getString("state");
		if (state == null) {
			throw new IllegalArgumentException("Missing state of site (state).");
		} else {
			State.valueOf(state.toUpperCase());
		}
		String site = json.getString("site");
		if (site == null || site.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing site name to display (site).");
		}
		String station = json.getString("station");
		if (station == null || site.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing station name to display (station).");
		}
	}
	
	/**
	 * Returns the state of the site. 
	 * @return Site state.
	 */
	public State getState() {
		String state = json.getString("state");
		return State.valueOf(state.toUpperCase());
	}
	
	/**
	 * Returns the site referenced by the item.
	 * @return Site name to display.
	 */
	public String getSite() {
		return json.getString("site");
	}
	
	/**
	 * Returns the station referenced by the item.
	 * @return Station name to display.
	 */
	public String getStation() {
		return json.getString("station");
	}
	
	/**
	 * Returns the status message to be displayed.
	 * @return Message to display. May be <code>null</code>.
	 */
	public String getMessage() {
		return json.getString("message");
	}
}
