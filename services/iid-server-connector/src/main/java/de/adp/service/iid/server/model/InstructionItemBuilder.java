package de.adp.service.iid.server.model;


/**
 * Builder for items in the instructions catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class InstructionItemBuilder extends ServiceItemBuilder<InstructionItemBuilder> {
	/**
	 * Creates a builder instance.
	 */
	public InstructionItemBuilder() {
		json.putString("catalog", "instructions");
	}
	
	/**
	 * Sets the title to be displayed for the instruction. <b>Required.</b>
	 * @param title Title to display.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setTitle(String title) {
		json.putString("title", title);
		return this;
	}
	
	/**
	 * Sets the image to be displayed in the item. Optional.
	 * @param url URL to retrieve image from.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setImageUrl(String url) {
		json.putString("imageUrl", url);
		return this;
	}
	
	/**
	 * Sets the site related to this item. Optional.
	 * @param site Site name to display.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setSite(String site) {
		json.putString("site", site);
		return this;
	}
	
	/**
	 * Sets the station related to this item. Optional.
	 * @param station Station name to display.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setStation(String station) {
		json.putString("station", station);
		return this;
	}
	
	/**
	 * Sets the skill level for the instruction. Optional.
	 * @param skillLevel Skill level.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setSkillLevel(int skillLevel) {
		json.putNumber("skillLevel", skillLevel);
		return this;
	}
	
	/**
	 * Sets the number of successful executions by the user. Optional.
	 * @param numExecutions Number of executions.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setNumberOfExecutions(int numExecutions) {
		json.putNumber("numExecutions", numExecutions);
		return this;
	}
	
	/**
	 * Sets the estimated execution time for the instruction. Optional.
	 * @param minutes Duration in minutes.
	 * @return Builder instance.
	 */
	public InstructionItemBuilder setEstimatedExecutionTime(int minutes) {
		json.putNumber("estExecutionTime", minutes);
		return this;
	}
	
	/**
	 * Builds the service catalog item.
	 * @return Item to be displayed in the instructions catalog.
	 * @throws IllegalArgumentException Not all required information is available.
	 */
	public InstructionItem build() throws IllegalArgumentException {
		return new InstructionItem(json);
	}
}
