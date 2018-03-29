package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * Send a message using the event bus.
 * @author simon.schwantzer(at)im-c.de
 */
public class SendMessageAction extends Action {
	/**
	 * Creates a new action sending a message over the event bus.
	 * @param address Address to send message to.
	 * @param body Message to send.
	 */
	public SendMessageAction(String address, JsonObject body) {
		super(new JsonObject());
		json.putString("type", "event");
		json.putString("address", address);
		json.putObject("body", body);
	}
	
	protected SendMessageAction(JsonObject json) throws IllegalArgumentException {
		super(json);
		validateJson(json);
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		if (json.getString("address") == null) throw new IllegalArgumentException("Missing event bus address (address).");
	}
	
	@Override
	public Type getType() {
		return Type.MESSAGE;
	}
	
	/**
	 * Returns the event bus address.
	 * @return Event bus address to send data to.
	 */
	public String getAddress() {
		return json.getString("address");
	}
	
	/**
	 * Returns the body to be sent.
	 * @return Body for the message to send. If no body is set, an empty body will be created and returned.
	 */
	public JsonObject getBody() {
		JsonObject body = json.getObject("body");
		if (body == null) {
			body = new JsonObject();
			json.putObject("body", body);
		}
		return body;
	}

	@Override
	public String getSessionId() {
		JsonObject body = json.getObject("body");
		return body != null ? body.getString("sessionId") : null;
	}

	@Override
	public void setSessionId(String sessionId) {
		JsonObject body = json.getObject("body");
		if (body == null) {
			body = new JsonObject();
			json.putObject("body", body);
		}
		body.putString("sessionId", sessionId);
	}

	@Override
	public String getToken() {
		JsonObject body = json.getObject("body");
		return body != null ? body.getString("token") : null;
	}

	@Override
	public void setToken(String token) {
		JsonObject body = json.getObject("body");
		if (body == null) {
			body = new JsonObject();
			json.putObject("body", body);
		}
		body.putString("token", token);
	}
}