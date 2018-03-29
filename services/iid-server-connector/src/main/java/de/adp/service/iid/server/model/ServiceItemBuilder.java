package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

public abstract class ServiceItemBuilder<E extends ServiceItemBuilder<E>> {
	protected final JsonObject json;
	
	protected ServiceItemBuilder() {
		json = new JsonObject();
	}
	
	/**
	 * Sets the identifier for the catalog item. <b>Required.</b>
	 * @param id Unique identifier.
	 * @return Builder instance.
	 */
	@SuppressWarnings("unchecked")
	public E setId(String id) {
		json.putString("id", id);
		return (E) this;
	}
	
	/**
	 * Sets the action to perform when the item is selected. Optional. 
	 * @param action Action to perform.
	 * @return Builder instance.
	 */
	@SuppressWarnings("unchecked")
	public E setAction(Action action) {
		json.putObject("action", action.asJson());
		return (E) this;
	}

	/**
	 * Sets the listing priority. <b>Required.</b>
	 * @param priority Priority. Items with a high priority are listed first.
	 * @return Builder instance.
	 */
	@SuppressWarnings("unchecked")
	public E setPriority(int priority) {
		json.putNumber("priority", priority);
		return (E) this;
	}
	
	/**
	 * Sets the service who created the item. Required.
	 * @param serviceId Service identifier.
	 * @return Builder instance.
	 */
	@SuppressWarnings("unchecked")
	public E setService(String serviceId) {
		json.putString("service", serviceId);
		return (E) this;
	}
}
