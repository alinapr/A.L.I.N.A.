package de.adp.service.auth;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;

import de.adp.service.auth.connector.MongoDBConnector;
import de.adp.service.auth.model.Session;
import de.adp.service.auth.model.View;

/**
 * Manager for sessions.
 * The manager stores the session in a MongoDB instance. All operations are asynchronous.
 * @author simon.schwantzer(at)im-c.de
 */
public class SessionManager {
	private final MongoDBConnector mongo;
	
	public SessionManager(MongoDBConnector mongoConnector) {
		this.mongo = mongoConnector;
	}
	
	/**
	 * Creates a new session.
	 * @param resultHandler Handler for retrieving the newly created session.
	 */
	public void createSession(final AsyncResultHandler<Session> resultHandler) {
		String sessionId = UUID.randomUUID().toString();
		final Session session = new Session(sessionId);
		session.update();
		mongo.save("sessions", session.asJson(), new AsyncResultHandler<Void>() {
			
			@Override
			public void handle(final AsyncResult<Void> result) {
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return result.succeeded();
					}
					
					@Override
					public Session result() {
						return succeeded() ? session : null;
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return result.cause();
					}
				});
			}
		});
	}
	
	/**
	 * Stores a session.
	 * @param session Session to store.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void storeSession(Session session, final AsyncResultHandler<Void> resultHandler) {
		session.update();
		mongo.update("sessions", new JsonObject().putString("id", session.getId()), session.asJson(), true, false, resultHandler);
	}
	
	/**
	 * Updates the last activity information of a session.
	 * @param sessionId ID of the session to indicate activity.
	 */
	public void updateSession(String sessionId) {
		JsonObject newObj = new JsonObject();
		String now = ISODateTimeFormat.dateTime().print(new DateTime());
		newObj.putObject("$set", new JsonObject().putString("lastActivity", now));
		mongo.update("sessions", new JsonObject().putString("id", sessionId), newObj, false, false, null);
	}
	
	/**
	 * Returns a session.
	 * @param sessionId ID of the session to return.
	 * @param resultHandler Handler for the asynchronous request.
	 */
	public void getSession(String sessionId, final AsyncResultHandler<Session> resultHandler) {
		JsonObject keys = new JsonObject().putNumber("_id", 0);
		mongo.findOne("sessions", new JsonObject().putString("id", sessionId), keys, new AsyncResultHandler<JsonObject>() {
			@Override
			public void handle(final AsyncResult<JsonObject> result) {
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return result.succeeded();
					}
					
					@Override
					public Session result() {
						if (succeeded()) {
							Session session = new Session(result.result());
							updateSession(session.getId());
							return session;
						} else {
							return null;
						}
					}
					
					@Override
					public boolean failed() {
						return result.failed();
					}
					
					@Override
					public Throwable cause() {
						return result.cause();
					}
				});
			}
		});
	}
	
	/**
	 * Return the session for a specific user.
	 * @param userId User identifier.
	 * @param resultHandler Handler for the asynchronous result. 
	 */
	public void getSessionForUser(String userId, final AsyncResultHandler<Session> resultHandler) {
		JsonObject keys = new JsonObject().putNumber("_id", 0);

		mongo.findOne("sessions", new JsonObject().putString("userId", userId), keys, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(final AsyncResult<JsonObject> result) {
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return result.succeeded();
					}
					
					@Override
					public Session result() {
						if (succeeded() && result.result() != null) {
							Session session = new Session(result.result());
							updateSession(session.getId());
							return session;
						} else {
							return null;
						}
					}
					
					@Override
					public boolean failed() {
						return result.failed();
					}
					
					@Override
					public Throwable cause() {
						return result.cause();
					}
				});
			}
		});
	}
	
	/**
	 * Deletes a session.
	 * @param sessionId ID of the session to delete.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void deleteSession(String sessionId, final AsyncResultHandler<Integer> resultHandler) {
		mongo.delete("sessions", new JsonObject().putString("id", sessionId), resultHandler);
	}
	
	/**
	 * Registers a view in a session. 
	 * @param sessionId ID of the session the view should be registered in.
	 * @param view View to register.
	 * @param resultHandler Handler to return the updated session object.
	 */
	public void registerView(String sessionId, final View view, final AsyncResultHandler<Session> resultHandler) {
		final JsonObject matcher = new JsonObject().putString("id", sessionId);
		final JsonObject keys = new JsonObject().putNumber("_id", 0);
		mongo.findOne("sessions", matcher, keys, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(final AsyncResult<JsonObject> loadRequest) {
				if (loadRequest.succeeded()) {
					final Session session = new Session(loadRequest.result());
					session.registerView(view);
					mongo.update("sessions", matcher, session.asJson(), false, false, new AsyncResultHandler<Void>() {
						
						@Override
						public void handle(final AsyncResult<Void> updateRequest) {
							resultHandler.handle(new AsyncResult<Session>() {
								
								@Override
								public boolean succeeded() {
									return updateRequest.succeeded();
								}
								
								@Override
								public Session result() {
									return succeeded() ? session : null;
								}
								
								@Override
								public boolean failed() {
									return !succeeded();
								}
								
								@Override
								public Throwable cause() {
									return failed() ? updateRequest.cause() : null;
								}
							});
						}
					});
				} else {
					resultHandler.handle(new AsyncResult<Session>() {
						
						@Override
						public boolean succeeded() {
							return false;
						}
						
						@Override
						public Session result() {
							return null;
						}
						
						@Override
						public boolean failed() {
							return true;
						}
						
						@Override
						public Throwable cause() {
							return loadRequest.cause();
						}
					});
				}
			}
		});
		/* Won't work because a bug in mongo-persistor < 2.1.2 prevents using find_and_modify.
		JsonObject update = new JsonObject();
		update.putObject("$addToSet", new JsonObject().putArray("views", new JsonArray().addObject(view.asJson())));
		String now = ISODateTimeFormat.dateTime().print(new DateTime());
		update.putObject("$set", new JsonObject().putString("lastActivity", now));
		mongo.findAndModify("sessions", new JsonObject().putString("id", sessionId), update, false, false, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(final AsyncResult<JsonObject> result) {
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return result.succeeded();
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(result.result()) : null;
					}
					
					@Override
					public boolean failed() {
						return result.failed();
					}
					
					@Override
					public Throwable cause() {
						return result.cause();
					}
				});
			}
		});
		*/
	}
	
	/**
	 * Removes a view from a session.
	 * @param sessionId ID of the session to remove view from.
	 * @param viewId ID of the view to remove.
	 * @param resultHandler Handler to return the updated session object.
	 */
	public void removeView(String sessionId, final String viewId, final AsyncResultHandler<Session> resultHandler) {
		final JsonObject matcher = new JsonObject().putString("id", sessionId);
		final JsonObject keys = new JsonObject().putNumber("_id", 0);
		mongo.findOne("sessions", matcher, keys, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(final AsyncResult<JsonObject> loadRequest) {
				if (loadRequest.succeeded()) {
					final Session session = new Session(loadRequest.result());
					session.removeView(viewId);
					mongo.update("sessions", matcher, session.asJson(), false, false, new AsyncResultHandler<Void>() {
						
						@Override
						public void handle(final AsyncResult<Void> updateRequest) {
							resultHandler.handle(new AsyncResult<Session>() {
								
								@Override
								public boolean succeeded() {
									return updateRequest.succeeded();
								}
								
								@Override
								public Session result() {
									return succeeded() ? session : null;
								}
								
								@Override
								public boolean failed() {
									return !succeeded();
								}
								
								@Override
								public Throwable cause() {
									return failed() ? updateRequest.cause() : null;
								}
							});
						}
					});
				} else {
					resultHandler.handle(new AsyncResult<Session>() {
						
						@Override
						public boolean succeeded() {
							return false;
						}
						
						@Override
						public Session result() {
							return null;
						}
						
						@Override
						public boolean failed() {
							return true;
						}
						
						@Override
						public Throwable cause() {
							return loadRequest.cause();
						}
					});
				}
			}
		});
		/*
		JsonObject update = new JsonObject();
		update.putObject("$pull", new JsonObject().putObject("views", new JsonObject().putString("id", viewId)));
		String now = ISODateTimeFormat.dateTime().print(new DateTime());
		update.putObject("$set", new JsonObject().putString("lastActivity", now));
		mongo.findAndModify("sessions", new JsonObject().putString("id", sessionId), update, true, false, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(final AsyncResult<JsonObject> result) {
				resultHandler.handle(new AsyncResult<Session>() {
					
					@Override
					public boolean succeeded() {
						return result.succeeded();
					}
					
					@Override
					public Session result() {
						return succeeded() ? new Session(result.result()) : null;
					}
					
					@Override
					public boolean failed() {
						return result.failed();
					}
					
					@Override
					public Throwable cause() {
						return result.cause();
					}
				});
			}
		});
		*/
	}
	
	/**
	 * Stores data in a session.
	 * @param sessionId ID of the session to store data in.
	 * @param data Data to store. All fields of the object will be stored, existing fields will be updated.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void storeData(String sessionId, JsonObject data, final AsyncResultHandler<Void> resultHandler) {
		JsonObject update = new JsonObject();
		JsonObject dataUpdates = new JsonObject(); 
		for (String fieldName : data.getFieldNames()) {
			dataUpdates.putValue("data." + fieldName, data.getValue(fieldName));
		}
		update.putObject("$set", dataUpdates);
		mongo.update("sessions", new JsonObject().putString("id", sessionId), update, false, false, resultHandler);
	}
	
	/**
	 * Retrieves data from a session.
	 * @param sessionId ID of the session to retrieve data from.
	 * @param fieldNames Name of the fields to retrieve.
	 * @param resultHandler Handler for the result, a JSON object with a field "data" containing the requested fields.
	 */
	public void getData(String sessionId, List<String> fieldNames, final AsyncResultHandler<JsonObject> resultHandler) {
		JsonObject matcher = new JsonObject();
		matcher.putString("id", sessionId);
		JsonObject keys = new JsonObject();
		keys.putNumber("_id", 0);
		for (String fieldName : fieldNames) {
			keys.putNumber("data." + fieldName, 1);
		}
		mongo.findOne("sessions", matcher, keys, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(final AsyncResult<JsonObject> event) {
				resultHandler.handle(new AsyncResult<JsonObject>() {
					
					@Override
					public boolean succeeded() {
						return event.succeeded();
					}
					
					@Override
					public JsonObject result() {
						return event.result().getObject("data");
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						return event.cause();
					}
				});
			}
		});
	}
	
	/**
	 * Deletes data from a session.
	 * @param sessionId ID of the session to delete data from.
	 * @param fieldNames Name of the fields to delete.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void deleteData(String sessionId, List<String> fieldNames, final AsyncResultHandler<Void> resultHandler) {
		JsonObject update = new JsonObject();
		JsonObject dataUpdates = new JsonObject(); 
		for (String fieldName : fieldNames) {
			dataUpdates.putString("data." + fieldName, "");
		}
		update.putObject("$unset", dataUpdates);
		mongo.update("sessions", new JsonObject().putString("id", sessionId), update, false, false, resultHandler);
	}
	
	/**
	 * Deletes all sessions which haven't been updated lately. 
	 * @param purgeBefore Sessions older than this time will be deleted.
	 * @param resultHandler Handler to return the number of purged sessions. May be <code>null</code>.
	 */
	public void purgeOldSessions(DateTime purgeBefore, final AsyncResultHandler<Integer> resultHandler) {
		JsonObject matcher = new JsonObject();
		String isoString = ISODateTimeFormat.dateTime().print(purgeBefore);
		matcher.putObject("lastActivity", new JsonObject().putString("$lt", isoString));
		mongo.delete("sessions", matcher, resultHandler);
	}
}
