package de.adp.service.iid.server.model;

import org.vertx.java.core.json.JsonObject;

/**
 * HTTP POST request action.
 * @author simon.schwantzer(at)im-c.de
 */
public class HttpPostAction extends Action {
	/**
	 * Creates a new action performing a HTTP POST call when fired.
	 * @param address Address to call when the action is fired.
	 * @param body Body to send. Session information is automatically added when the event is fired.
	 */
	public HttpPostAction(String address, JsonObject body) {
		super(new JsonObject());
		json.putString("type", "post");
		json.putString("address", address);
		json.putObject("body", body);
	}
	
	protected HttpPostAction(JsonObject json) throws IllegalArgumentException {
		super(json);
		validateJson(json);
	}
	
	private static void validateJson(JsonObject json) throws IllegalArgumentException {
		if (json.getString("address") == null) throw new IllegalArgumentException("Missing address for POST action.");
	}
	
	@Override
	public Type getType() {
		return Type.POST;
	}
	
	/**
	 * Returns the address for the action.
	 * @return URL to call.
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
		return body != null ? json.getString("token") : null;
	}
	
	@Override
	public void setToken(String token) {
		JsonObject body = json.getObject("body");
		if (body == null) {
			body = new JsonObject();
			body.putObject("body", body);
		}
		body.putString("token", token);
	}
}