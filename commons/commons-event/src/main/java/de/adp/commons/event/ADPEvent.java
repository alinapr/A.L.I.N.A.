package de.adp.commons.event;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

/**
 * Model for an event which is exchanged in the APPsist framework.
 * @author simon.schwantzer(at)im-c.de
 */
public class ADPEvent {
	/**
	 * Type of an event.
	 * An event is either triggered by a (direct) user action, a service, or a machine.  
	 */
	public enum Type {
		USER,
		SERVICE,
		MACHINE,
		UNKNOWN
	}
	
	Map<String, Object> content;
	private final Type type;
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @param type Type of the event.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ADPEvent(Map<String, Object> content, Type type) throws IllegalArgumentException {
		this.content = content;
		this.type = type;
		if (!content.containsKey("id") || !content.containsKey("modelId")) {
			throw new IllegalArgumentException("Missing required fields.");
		}
	}	
	
	/**
	 * Constructor for an event. 
	 * @param id ID for the event. The ID should be unique for the domain. 
	 * @param modelId ID of the event model as stored in the event repository.
	 * @param type Type of the event.
	 */
	public ADPEvent(String id, String modelId, Type type) {
		this(id, modelId, type, new LinkedHashMap<String, Object>());
	}
	
	/**
	 * Constructor for an event. 
	 * @param id ID for the event. The ID should be unique for the domain. 
	 * @param modelId ID of the event model as stored in the event repository.
	 * @param type Type of the event.
	 * @param payload JSON map with payload for the event.
	 */
	public ADPEvent(String id, String modelId, Type type, Map<String, Object> payload) {
		content = new LinkedHashMap<String, Object>();
		content.put("id", id);
		content.put("modelId", modelId);
		content.put("payload", payload);
		Calendar cal = Calendar.getInstance();
		content.put("created", DatatypeConverter.printDateTime(cal));
		this.type = type;
	}
	
	/**
	 * Returns the type of this event.
	 * @return Type (source type) of the event.
	 */
	public Type getType() {
		return type; 
	}
	
	/**
	 * Returns the ID which identifies this event.
	 * @return Identifier for the event.
	 */
	public String getId() {
		return (String) content.get("id");
	}
	
	/**
	 * Returns the ID of the event model implemented with this event.
	 * @return Event model ID.
	 */
	public String getModelId() {
		return (String) content.get("modelId");
	}
	
	/**
	 * Connects the event with a session. 
	 * @param sessionId ID of the session to connect the event with.
	 */
	public void setSessionId(String sessionId) {
		content.put("session", sessionId);
	}
	
	/**
	 * Returns the ID of the session the event is connected with.
	 * @return Session ID or <code>null</code> if no session is connected.
	 */
	public String getSessionId() {
		return (String) content.get("session");
	}
	
	/**
	 * Sets the date when the event expires.
	 * If not set, the event does not expire.
	 * @param expiringTime Time for the expiration of the event.
	 */
	public void setExpirationTime(Date expiringTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(expiringTime);
		content.put("expires", DatatypeConverter.printDateTime(cal));
	}
	
	/**
	 * Returns the date when the event expires.
	 * @return Time for the expiration of the event or <code>null</code> if the event does not expire.
	 */
	public Date getExpirationTime() {
		String dateTimeString = (String) content.get("expires");
		if (dateTimeString != null) {
			return DatatypeConverter.parseDateTime(dateTimeString).getTime();
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the creation time of the event.
	 * @param creationTime Time the event was created.
	 */
	public void setCreationTime(Date creationTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(creationTime);
		content.put("created", DatatypeConverter.printDateTime(cal));
	}
	
	/**
	 * Returns the creation time of the event.
	 * @return Time the event was created.
	 */
	public Date getCreationTime() {
		String dateTimeString = (String) content.get("created");
		if (dateTimeString != null) {
			return DatatypeConverter.parseDateTime(dateTimeString).getTime();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the content map representing this event.
	 * Changes to this map will change on the object fields.
	 * @return Map representing the JSON structure of the event. 
	 */
	public Map<String, Object> asMap() {
		return content;
	}
	
	/**
	 * Returns the payload for this event.
	 * @return Map with keys an objects. 
	 */
	public Map<String, Object> getPayload() {
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = (Map<String, Object>) content.get("payload");
		return payload;
	}
}
