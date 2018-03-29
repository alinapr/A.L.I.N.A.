package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a service item in the instructions catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class InstructionItem extends ServiceItem {
	
	protected InstructionItem(JsonObject json) throws IllegalArgumentException {
		super(json);
		validateJson(json);
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		String title = json.getString("title");
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing title for instruction item (title).");
		}
	}
	
	/**
	 * Returns the title to display.
	 * @return Title to display.
	 */
	public String getTitle() {
		return json.getString("title");
	}
	
	/**
	 * Returns the site the item is related to.
	 * @return Site name to display. May be <code>null</code>.
	 */
	public String getSite() {
		return json.getString("site");
	}
	
	/**
	 * Returns the station the item is related to.
	 * @return Station name to display. May be <code>null</code>.
	 */
	public String getStation() {
		return json.getString("station");
	}
	
	/**
	 * Returns the image to be displayed with the item-
	 * @return URL of the image to display. May be <code>null</code>.
	 */
	public String getImageUrl() {
		return json.getString("imageUrl");
	}
	
	/**
	 * Returns the skill level required to peform the task.
	 * @return Skill level.
	 */
	public int getSkillLevel() {
		return json.getInteger("skillLevel");
	}
	
	/**
	 * Returns how many time the current user has executed the task. 
	 * @return Number of executions.
	 */
	public int getNumberOfExecutions() {
		return json.getInteger("numExecutions");
	}
	
	/**
	 * Returns the estimated time to execute the task.
	 * @return Duration in minutes.
	 */
	public int getEstimatedExecutionTime() {
		return json.getInteger("estExecutionTime");
	}
}
