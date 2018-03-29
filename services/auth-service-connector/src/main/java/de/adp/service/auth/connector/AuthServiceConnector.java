package de.adp.service.auth.connector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import de.adp.service.auth.connector.model.Session;
import de.adp.service.auth.connector.model.User;
import de.adp.service.auth.connector.model.UserStatus;
import de.adp.service.auth.connector.model.View;

/**
 * Public connector for the authentication and session service.
 * @author simon.schwantzer(at)im-c.de
 */
public class AuthServiceConnector {
	public static final String SERVICE_ID = "adp:service:auth";
	
	public enum AuthType {
		PASSWORD,
		PIN,
		HASH
	}
	
	private final EventBus eventBus;
	private final String address;
	
	/**
	 * Creates a new service connector.
	 * @param eventBus Event bus for sending and receiving messages.
	 * @param address Event bus address of the auth service.
	 */
	public AuthServiceConnector(EventBus eventBus, String address) {
		this.eventBus = eventBus;
		this.address = address;
	}
	
	/**
	 * Request a new user token.
	 * @param userId ID of the user to request token for.
	 * @param authType Authentication type.
	 * @param authCode Authentication code, i.e. password, hash, or pin depending on authentication type.
	 * @param resultHandler Handler for the resulting token string.
	 */
	public void generateTokenForUser(String userId, AuthType authType, String authCode, final AsyncResultHandler<String> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "generateToken");
		request.putString("userId", userId);
		switch (authType) {
		case PASSWORD:
			request.putString("password", authCode);
			break;
		case HASH:
			request.putString("hash", authCode);
			break;
		case PIN:
			request.putString("pin", authCode);
			break;
		}
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<String>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public String result() {
						return succeeded() ? body.getString("token") : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Validates a user token.
	 * @param token Token to validate.
	 * @param userId ID of the user the token should authenticate.
	 * @param resultHandler Handler to check if the validation succeeds. Contains the token claims if successful.
	 */
	public void validateTokenForUser(String token, String userId, final AsyncResultHandler<JsonObject> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "validateToken");
		request.putString("subject", userId);
		request.putString("token", token);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<JsonObject>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public JsonObject result() {
						return succeeded() ? body.getObject("claims") : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Authenticates a user.
	 * @param userId Identifier of the user to identify.
	 * @param authType Type of authentication.
	 * @param authCode Code for authentication based on type, i.e. password, hash or pin.
	 * @param resultHandler Handler to check if the operation succeeded. If successful, a user object for the authenticated user is returned.
	 */
	public void authenticateUser(String userId, AuthType authType, String authCode, final AsyncResultHandler<User> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "authenticateUser");
		request.putString("userId", userId);
		switch (authType) {
		case PASSWORD:
			request.putString("password", authCode);
			break;
		case HASH:
			request.putString("hash", authCode);
			break;
		case PIN:
			request.putString("pin", authCode);
			break;
		}
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<User>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public User result() {
						if (succeeded()) {
							User user = new User(body.getObject("user"));
							user.cacheToken(body.getString("token"));
							return user;
						} else {
							return null;
						}
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
		
	}

	/**
	 * Returns the publicly visible information of a user.  
	 * @param userId Identifier of the requested user.
	 * @param resultHandler Handler to return the resulting user object.
	 */
	public void getUserInformation(String userId, final AsyncResultHandler<User> resultHandler) {
		getUserInformation(userId, null, resultHandler);
	}
	
	/**
	 * Returns information of a user. 
	 * @param userId Identifier of the user requested.
	 * @param token User token to authenticate request. If <code>null</code>, only publicly visible information will be returned.
	 * @param resultHandler Handler for the resulting user object.
	 */
	public void getUserInformation(String userId, String token, final AsyncResultHandler<User> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "getUser");
		request.putString("userId", userId);
		if (token != null) request.putString("token", token);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<User>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public User result() {
						return succeeded() ? new User(body.getObject("user")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}
	
	/**
	 * Returns a map of all users registered in the domain.
	 * @param resultHandler Handler for the resulting map.
	 */
	public void getUserInformation(final AsyncResultHandler<Map<String, User>> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "getUsers");
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<Map<String, User>>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Map<String, User> result() {
						if (succeeded()) {
							Map<String, User> users = new LinkedHashMap<String, User>();
							JsonObject usersObject = body.getObject("users");
							for (String userId : usersObject.getFieldNames()) {
								User user = new User(usersObject.getObject(userId));
								users.put(userId, user);
							}
							return users;
						} else {
							return null;
						}
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}
	
	/**
	 * Creates a new session.
	 * @param resultHandler Handler for the the resulting session object. 
	 */
	public void createSession(final AsyncResultHandler<Session> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "createSession");
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(body.getObject("session")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Stores a session.
	 * @param session Session to store.
	 * @param token User authentication token. Required if the session is a user session.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void storeSession(Session session, String token, final AsyncResultHandler<Void> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "storeSession");
		request.putObject("session", session.asJson());
		if (token != null) request.putString("token", token);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				if (resultHandler != null) resultHandler.handle(new AsyncResult<Void>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Void result() {
						return null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Returns a session.
	 * @param sessionId Identifier of the session requested.
	 * @param token User authentication token. Required if the session is a user session.
	 * @param resultHandler Handler for the resulting session object.
	 */
	public void getSession(String sessionId, String token, final AsyncResultHandler<Session> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "getSession");
		request.putString("sessionId", sessionId);
		if (token != null) request.putString("token", token);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(body.getObject("session")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Requests the session of a user.
	 * @param userId User identifier.
	 * @param token User authentication token.
	 * @param resultHandler Handler for the resulting session object.
	 */
	public void getSessionForUser(String userId, String token, final AsyncResultHandler<Session> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "getSession");
		request.putString("userId", userId);
		request.putString("token", token);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(body.getObject("session")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
		
	}

	/**
	 * Deletes a session.
	 * @param sessionId Identifier of the session to delete.
	 * @param token User authentication token. Required if the session is a user session.
	 * @param resultHandler Handler to check of the operation succeeded. May be <code>null</code>.
	 */
	public void deleteSession(String sessionId, String token, final AsyncResultHandler<Void> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "deleteSession");
		request.putString("sessionId", sessionId);
		if (token != null) request.putString("token", token);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				if (resultHandler != null) resultHandler.handle(new AsyncResult<Void>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Void result() {
						return null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Registers a view in a user session.
	 * @param sessionId Session to register view.
	 * @param token User authentication token.
	 * @param view View to register.
	 * @param resultHandler Handler to check if the operation succeeded. Returns modified session if successful. May be <code>null</code>.
	 */
	public void registerView(String sessionId, String token, View view, final AsyncResultHandler<Session> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "registerView");
		request.putString("sessionId", sessionId);
		request.putString("token", token);
		request.putObject("view", view.asJson());
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
	
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				if (resultHandler != null) resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(body.getObject("session")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Removes a view from a user session.
	 * @param sessionId Identifier of the session.
	 * @param token User authentication token.
	 * @param viewId Identifier of the view which should be removed.
	 * @param resultHandler Handler to check if the operation succeeded. Returns modified session if successful. May be <code>null</code>.
	 */
	public void removeView(String sessionId, String token, String viewId, final AsyncResultHandler<Session> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "removeView");
		request.putString("sessionId", sessionId);
		request.putString("token", token);
		request.putString("viewId", viewId);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				if (resultHandler != null) resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(body.getObject("session")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Stores data in a session.
	 * @param sessionId Identifier of the session to store data in.
	 * @param token User authentication token. Required if the session is a user session.
	 * @param data Data to store. The fields will be added and replace existing fields.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void storeData(String sessionId, String token, JsonObject data, final AsyncResultHandler<Void> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "storeData");
		request.putString("sessionId", sessionId);
		if (token != null) request.putString("token", token);
		request.putObject("data", data);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
			
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				if (resultHandler != null) resultHandler.handle(new AsyncResult<Void>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Void result() {
						return null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Returns the data from a session.
	 * @param sessionId Identifier of the session.
	 * @param token User authentication token. Required of the session is a user session.
	 * @param fieldNames List of fields which are requested.
	 * @param resultHandler Handler for the returned JSON object.
	 */
	public void getData(String sessionId, String token, List<String> fieldNames, final AsyncResultHandler<JsonObject> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "getData");
		request.putString("sessionId", sessionId);
		if (token != null) request.putString("token", token);
		JsonArray fields = new JsonArray();
		for (String fieldName : fieldNames) {
			fields.addString(fieldName);
		}
		request.putArray("fields", fields);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
	
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<JsonObject>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public JsonObject result() {
						return succeeded() ? body.getObject("data") : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

	/**
	 * Deletes data from a session.
	 * @param sessionId Identifier of the session.
	 * @param token User authentication token. Required of the session is a user session.
	 * @param fieldNamesUser authentication token. Required of the session is a user session.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void deleteData(String sessionId, String token, List<String> fieldNames, final AsyncResultHandler<Void> resultHandler) {
		JsonObject request = new JsonObject();
		request.putString("action", "deleteData");
		request.putString("sessionId", sessionId);
		if (token != null) request.putString("token", token);
		JsonArray fields = new JsonArray();
		for (String fieldName : fieldNames) {
			fields.addString(fieldName);
		}
		request.putArray("fields", fields);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {
	
			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				if (resultHandler != null) resultHandler.handle(new AsyncResult<Void>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Void result() {
						return null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});	
			}
		});
	}
	
	/**
	 * Requests the status of a user.
	 * @param userId Identifier of the requested user.
	 * @param resultHandler Handler for the user status information.
	 */
	public void getUserStatus(String userId, final AsyncResultHandler<UserStatus> resultHandler) {
		JsonObject request = new JsonObject()
			.putString("action", "getUserStatus")
			.putString("userId", userId);
		eventBus.send(address, request, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> event) {
				final JsonObject body = event.body();
				resultHandler.handle(new AsyncResult<UserStatus>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public UserStatus result() {
						return succeeded() ? new UserStatus(body.getObject("userStatus")) : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return failed() ? new Throwable(body.getString("message")) : null;
					}
				});
			}
		});
	}

}
