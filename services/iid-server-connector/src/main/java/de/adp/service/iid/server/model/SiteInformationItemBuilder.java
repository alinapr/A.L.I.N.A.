package de.adp.service.iid.server.model;

import de.adp.service.iid.server.model.SiteInformationItem.State;

/**
 * Builder for service catalog items in the site information catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class SiteInformationItemBuilder extends ServiceItemBuilder<SiteInformationItemBuilder>{
	
	/**
	 * Creates a new builder instance.
	 */
	public SiteInformationItemBuilder() {
		json.putString("catalog", "siteInfo");
	}
	
	/**
	 * Sets the state of the site. <b>Required.</b>
	 * @param state State to display. 
	 * @return Builder instance
	 */
	public SiteInformationItemBuilder setState(State state) {
		json.putString("state", state.toString().toLowerCase());
		return this;
	}
	
	/**
	 * Sets the site referenced by the item. <b>Required.</b>
	 * @param site Site name to display.
	 * @return Builder instance.
	 */
	public SiteInformationItemBuilder setSite(String site) {
		json.putString("site", site);
		return this;
	}
	
	/**
	 * Sets the station referenced by the item. <b>Required.</b>
	 * @param station Station name to display.
	 * @return Builder instance.
	 */
	public SiteInformationItemBuilder setStation(String station) {
		json.putString("station", station);
		return this;
	}
	
	/**
	 * Sets the status message to be displayed.
	 * @param message Message to display. May be <code>null</code>.
	 * @return Builder instance.
	 */
	public SiteInformationItemBuilder setMessage(String message) {
		json.getString("message", message);
		return this;
	}
	
	/**
	 * Builds the service catalog item.
	 * @return Service item for the site information catalog.
	 * @throws IllegalArgumentException Not all required information is provided.
	 */
	public SiteInformationItem build() throws IllegalArgumentException {
		return new SiteInformationItem(json);
	}
}
