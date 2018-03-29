package de.adp.service.iid.server.model;

/**
 * Warning level for a station.
 */
public enum Level {
	INFO,
	WARNING,
	ERROR;
	
	public static Level fromString(String levelString) throws IllegalArgumentException {
		for (Level level : Level.values()) {
			if (level.name().equalsIgnoreCase(levelString)) return level;
		}
		throw new IllegalArgumentException("Invalid notification level.");
	}
}