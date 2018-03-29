package de.adp.service.iid.server.model;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.vertx.java.core.json.JsonObject;

/**
 * Model class for location information.
 * @author simon.schwantzer(at)im-c.de
 */
public class Location {
	/**
	 * Type of the location.
	 * @author simon.schwantzer(at)im-c.de
	 */
	public enum Type {
		/**
		 * Fix (configured) location.
		 */
		FIX,
		/**
		 * Unknown location, only raw data available.
		 */
		UNKNOWN
	}
	
	private final JsonObject json;
	private final Type type;
	private final Date lastUpdate;
	
	public Location(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
		
		type = Type.valueOf(json.getString("type").toUpperCase());
		
		String lastUpdateString = json.getString("lastUpdate");
		lastUpdate = lastUpdateString != null ? DatatypeConverter.parseDateTime(lastUpdateString).getTime() : null;
	}
	
	private static final void validateJson(JsonObject json) throws IllegalArgumentException {
		if (json.getString("displayName") == null) {
			throw new IllegalArgumentException("Missing name to display for location [displayName].");
		}
		
		if (json.getString("type") == null) {
			throw new IllegalAccessError("Missing location type [type].");
		}
	}
	
	/**
	 * Returns the JSON object wrapped.
	 * @return JSON object representing a location.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the ID of the location.
	 * @return Location identifier. <code>null</code> if location is of {@link Type#UNKNOWN}.
	 */
	public String getId() {
		return json.getString("id");
	}
	
	/**
	 * Returns the name to display for the location.
	 * @return Name to display.
	 */
	public String getDisplayName() {
		return json.getString("displayName");
	}
	
	/**
	 * Returns the building of the location.
	 * @return Building name to display. May be <code>null</code>.
	 */
	public String getBuilding() {
		return json.getString("building");
	}
	
	/**
	 * Returns the type of the location information.
	 * @return Type of the location information.
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Returns the raw data for the location.
	 * @return Raw data, depending on location information type.
	 */
	public JsonObject getRawData() {
		return json.getObject("raw");
	}
	
	/**
	 * Returns the date time the location was captured.
	 * @return Date time. May be <code>null</code>.
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}
}
