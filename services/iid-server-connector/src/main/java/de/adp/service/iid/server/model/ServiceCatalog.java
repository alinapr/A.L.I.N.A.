package de.adp.service.iid.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Model for a service item catalog.
 * @author simon.schwantzer(at)im-c.de
 */
public class ServiceCatalog {
	private final JsonObject json;
	
	/**
	 * Creates a new catalog with the given id.
	 * @param id Identifier for the catalog.
	 */
	public ServiceCatalog(String id) {
		this.json = new JsonObject();
		json.putString("id", id);
		json.putArray("items", new JsonArray());
	}
	
	/**
	 * Returns the identifier of this catalog.
	 * @return Catalog identifier.
	 */
	public String getId() {
		return json.getString("id");
	}
	
	/**
	 * Returns the JSON representation of this catalog.
	 * @return JSON object representing the catalog.
	 */
	public JsonObject asJson() {
		return json;
	}
	
	/**
	 * Returns all service items of the catalog.
	 * @return List of service items.
	 */
	public List<ServiceItem> getItems() {
		List<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
		for (Object itemObject : json.getArray("items")) {
			ServiceItem item = new ServiceItem((JsonObject) itemObject);
			serviceItems.add(item);
		}
		return serviceItems;
	}
	
	/**
	 * Removes a service item from the catalog.
	 * @param itemId Identifier of the item to remove.
	 * @return <code>true</code> if the item has been found and removed, otherwise <code>false</code>.
	 */
	public boolean removeItem(String itemId) {
		JsonArray newItemsArray = new JsonArray();
		boolean itemFound = false;
		for (Object itemObject : json.getArray("items")) {
			JsonObject item = ((JsonObject) itemObject);
			if (itemId.equals(item.getString("id"))) {
				itemFound = true;
				continue;
			} else {
				newItemsArray.addObject(item);
			}
		}
		if (itemFound) {
			json.putArray("items", newItemsArray);
		}
		return itemFound;
	}
	
	/**
	 * Adds an item to the catalog. If an item with the same id already exists, it will be replaced.
	 * @param itemToSet Item to add to/update in the catalog.
	 */
	public void addItem(ServiceItem itemToSet) {
		List<ServiceItem> serviceItems = new ArrayList<ServiceItem>(); 
		boolean itemExists = false;
		for (Object itemObject : json.getArray("items")) {
			ServiceItem item = new ServiceItem((JsonObject) itemObject);
			if (itemToSet.getId().equals(item.getId())) {
				// replace
				serviceItems.add(itemToSet);
				itemExists = true;
			} else {
				serviceItems.add(item);
			}
		}
		if (!itemExists) {
			serviceItems.add(itemToSet);
		}
		Collections.reverse(serviceItems);
		JsonArray newItemsArray = new JsonArray();
		for (ServiceItem item : serviceItems) {
			newItemsArray.addObject(item.asJson());
		}
		json.putArray("items", newItemsArray);
	}
	
	/**
	 * Adds multiple items to the catalog. Existing items will be replaced of their id equals to one of the new set.
	 * @param itemsToSet List of items to add to the catalog.
	 */
	public void addItems(List<ServiceItem> itemsToSet) {
		List<ServiceItem> serviceItems = new ArrayList<ServiceItem>(); 
		Map<String, ServiceItem> itemsToSetMap = new HashMap<String, ServiceItem>();
		for (ServiceItem item : itemsToSet) {
			itemsToSetMap.put(item.getId(), item);
		}
		for (Object itemObject : json.getArray("items")) {
			ServiceItem existingItem = new ServiceItem((JsonObject) itemObject);
			String id = existingItem.getId();
			if (itemsToSetMap.keySet().contains(id)) {
				serviceItems.add(itemsToSetMap.get(id));
				// newItemsArray.addObject(itemsToSetMap.get(id).asJson());
				itemsToSetMap.remove(id);
			} else {
				serviceItems.add(existingItem);
				// newItemsArray.addObject(existingItem.asJson());
			}
		}
		
		for (Entry<String, ServiceItem> entry : itemsToSetMap.entrySet()) {
			serviceItems.add(entry.getValue());
		}
		
		Collections.reverse(serviceItems);
		JsonArray newItemsArray = new JsonArray();
		for (ServiceItem item : serviceItems) {
			newItemsArray.addObject(item.asJson());
		}
		json.putArray("items", newItemsArray);
	}
	
	/**
	 * Removes all items which have been posted by a specific service.
	 * @param serviceId Identifier for a service.
	 */
	public void removeItemsOfService(String serviceId) {
		JsonArray newItemsArray = new JsonArray();
		for (Object itemObject : json.getArray("items")) {
			ServiceItem item = new ServiceItem((JsonObject) itemObject);
			if (serviceId.equals(item.getServiceId())) {
				continue;
			} else {
				newItemsArray.addObject(item.asJson());
			}
		}
		json.putArray("items", newItemsArray);
	}
}
