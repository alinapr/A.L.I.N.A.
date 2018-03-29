package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Model for a service item.
 * A service item represents an element to display in the UI and represents a single action user can perform.
 * The model wraps an JSON object. 
 * @author simon.schwantzer(at)im-c.de
 */
public class ServiceItem implements Comparable<ServiceItem> {
	protected final JsonObject json;
	
	/**
	 * Creates a service item wrapping the given JSON object.
	 * @param json JSON object representing a service item.
	 */
	public ServiceItem(JsonObject json) throws IllegalArgumentException {
		validateJson(json);
		this.json = json;
	}
	
	private void validateJson(JsonObject json) throws IllegalArgumentException {
		String id = json.getString("id");
		if (id == null || id.length() == 0) {
			throw new IllegalArgumentException("Missing identifier (id).");
		}
		String catalog = json.getString("catalog");
		if (catalog == null || catalog.length() == 0) {
			throw new IllegalArgumentException("Missing catalog reference (catalog).");
		}
		String service = json.getString("service");
		if (service == null || service.length() == 0) {
			throw new IllegalArgumentException("Missing service reference (service).");
		}
	}
	
	/**
	 * Returns the JSON object wrapped by this model.
	 * @return JSON object representing the service item.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns the identifier for the service item.
	 * @return Item identifier.
	 */
	public String getId() {
		return json.getString("id");
	}
	
	/**
	 * Returns the service which posts the item.
	 * @return Service identifier.
	 */
	public String getServiceId() {
		return json.getString("service");
	}
	
	/**
	 * Returns the service catalog to display the item in.
	 * @return Service catalog identifier.
	 */
	public String getCatalogId() {
		return json.getString("catalog");		
	}
	
	/**
	 * Returns the priority with witch the item should be displayed.
	 * @return Priority indicated by positive number, the higher the more important.
	 */
	public int getPriority() {
		return json.getInteger("priority");
	}
	
	/**
	 * Returns the action which is performed when the item is selected.
	 * @return Action to be performed.
	 */
	public Action getAction() {
		JsonObject actionObject = json.getObject("action");
		if (actionObject != null) {
			return Action.fromJson(actionObject);
		} else {
			return null;
		}
	}

	@Override
	public int compareTo(ServiceItem other) {
		if (this.getPriority() == 0 && other.getPriority() == 0) {
			return 0;
		}
		if (this.getPriority() == 0) return 1;
		if (other.getPriority() == 0 ) return -1;
		return Integer.valueOf(this.getPriority()).compareTo(Integer.valueOf(other.getPriority()));
	}
}
