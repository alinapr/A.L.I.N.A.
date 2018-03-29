package de.adp.service.ps.connector;

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import de.adp.commons.event.CallActivityEvent;
import de.adp.commons.event.ManualTaskEvent;
import de.adp.commons.event.ProcessCompleteEvent;
import de.adp.commons.event.ProcessErrorEvent;
import de.adp.commons.event.ProcessUserRequestEvent;
import de.adp.commons.event.UserTaskEvent;
import de.adp.commons.util.EventUtil;
import de.adp.service.ps.HttpException;

/**
 * Manager for the communication with the process coordination instance.
 * @author simon.schwantzer(at)im-c.de
 */
public class PKIConnector {
	private final HttpClient pkiClient;
	private final String basePath;
	private final Logger logger;
	
	private Set<Handler<ManualTaskEvent>> manualTaskHandlers;
	private Set<Handler<UserTaskEvent>> userTaskHandlers;
	private Set<Handler<CallActivityEvent>> callActivityHandlers;
	private Set<Handler<ProcessUserRequestEvent>> processUserRequestHandlers;
	private Set<Handler<ProcessCompleteEvent>> processCompleteHandlers;
	private Set<Handler<ProcessErrorEvent>> processErrorHandlers;
	
	// private final Map<String, Set<Handler<? extends ADPEvent>>> handlers;
	
	private class JsonResponseHandler implements Handler<HttpClientResponse> {
		private final AsyncResultHandler<JsonObject> resultHandler;
		
		private JsonResponseHandler(AsyncResultHandler<JsonObject> resultHandler) {
			this.resultHandler = resultHandler;
		}
		
		@Override
		public void handle(final HttpClientResponse response) {
			response.bodyHandler(new Handler<Buffer>() {
				
				@Override
				public void handle(Buffer buffer) {
					final String body = buffer.toString();
					resultHandler.handle(new AsyncResult<JsonObject>() {
						
						@Override
						public boolean succeeded() {
							return response.statusCode() == 200;
						}
						
						@Override
						public JsonObject result() {
							return succeeded() ? new JsonObject(body) : null;
						}
						
						@Override
						public boolean failed() {
							return !succeeded();
						}
						
						@Override
						public Throwable cause() {
							return failed() ? new HttpException(body, response.statusCode()) : null;
						}
					});
					
				}
			});
		}
	}
	
	public PKIConnector(Vertx vertx, Container container, JsonObject serviceConfig) {
		initializeEventBusHandlers(vertx.eventBus());
		pkiClient = vertx.createHttpClient();
		pkiClient.setHost(serviceConfig.getString("host"));
		pkiClient.setPort(serviceConfig.getInteger("port"));
		pkiClient.setSSL(serviceConfig.getBoolean("secure", false));
		basePath = serviceConfig.getObject("paths").getString("pki");
		logger = container.logger();
	}
	
	private void initializeEventBusHandlers(EventBus eventBus) {
		
		// manual tasks
		manualTaskHandlers = new HashSet<Handler<ManualTaskEvent>>();
		eventBus.registerHandler("adp:event:" + ManualTaskEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				ManualTaskEvent event;
				try {
					event = EventUtil.parseEvent(message.body().toMap(), ManualTaskEvent.class);
					System.out.println("[PSD] Received manual task event: " + message.body().encode());
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to parse event.", e);
					return;
				}
				for (Handler<ManualTaskEvent> handler : manualTaskHandlers) {
					handler.handle(event);
				}
			}
		});
		
		// user tasks
		userTaskHandlers = new HashSet<Handler<UserTaskEvent>>();
		eventBus.registerHandler("adp:event:" + UserTaskEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				UserTaskEvent event;
				try {
					event = EventUtil.parseEvent(message.body().toMap(), UserTaskEvent.class);
					System.out.println("[PSD] Received user task event: " + message.body().encode());
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to parse event.", e);
					return;
				}
				for (Handler<UserTaskEvent> handler : userTaskHandlers) {
					handler.handle(event);
				}
			}
		});
		
		// activity start
		callActivityHandlers = new HashSet<Handler<CallActivityEvent>>();
		eventBus.registerHandler("adp:event:" + CallActivityEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				CallActivityEvent event;
				try {
					event = EventUtil.parseEvent(message.body().toMap(), CallActivityEvent.class);
					System.out.println("[PSD] Received start activity event: " + message.body().encode());
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to parse event.", e);
					return;
				}
				for (Handler<CallActivityEvent> handler : callActivityHandlers) {
					handler.handle(event);
				}
			}
		});
		
		// process user requests
		processUserRequestHandlers = new HashSet<Handler<ProcessUserRequestEvent>>();
		eventBus.registerHandler("adp:event:" + ProcessUserRequestEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				ProcessUserRequestEvent event;
				try {
					event = EventUtil.parseEvent(message.body().toMap(), ProcessUserRequestEvent.class);
					System.out.println("[PSD] Received user request event: " + message.body().encode());
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to parse event.", e);
					return;
				}
				for (Handler<ProcessUserRequestEvent> handler : processUserRequestHandlers) {
					handler.handle(event);
				}
			}
		});
		
		// process complete
		processCompleteHandlers = new HashSet<Handler<ProcessCompleteEvent>>();
		eventBus.registerHandler("adp:event:" + ProcessCompleteEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				ProcessCompleteEvent event;
				try {
					event = EventUtil.parseEvent(message.body().toMap(), ProcessCompleteEvent.class);
					System.out.println("[PSD] Received process complete event: " + message.body().encode());
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to parse event.", e);
					return;
				}
				for (Handler<ProcessCompleteEvent> handler : processCompleteHandlers) {
					handler.handle(event);
				}
			}
		});
		
		// process error
		processErrorHandlers = new HashSet<Handler<ProcessErrorEvent>>();
		eventBus.registerHandler("adp:event:" + ProcessErrorEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				ProcessErrorEvent event;
				try {
					event = EventUtil.parseEvent(message.body().toMap(), ProcessErrorEvent.class);
					System.out.println("[PSD] Received process error event: " + message.body().encode());
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to parse event.", e);
					return;
				}
				for (Handler<ProcessErrorEvent> handler : processErrorHandlers) {
					handler.handle(event);
				}
			}
		});
	}
	
	public void registerManualTaskHandler(Handler<ManualTaskEvent> handler) {
		manualTaskHandlers.add(handler);
	}
	
	public void registerUserTaskHandler(Handler<UserTaskEvent> handler) {
		userTaskHandlers.add(handler);
	}
	
	public void registerCallActivityHandler(Handler<CallActivityEvent> handler) {
		callActivityHandlers.add(handler);
	}
	
	public void registerProcessUserRequestHandler(Handler<ProcessUserRequestEvent> handler) {
		processUserRequestHandlers.add(handler);
	}
	
	public void registerProcessCompleteHandler(Handler<ProcessCompleteEvent> handler) {
		processCompleteHandlers.add(handler);
	}
	
	public void registerProcessErrorHandler(Handler<ProcessErrorEvent> handler) {
		processErrorHandlers.add(handler);
	}
	
	/**
	 * Requests a process definition.
	 * @param processId ID of the process definition.
	 * @param resultHandler Handler for the asynchronous request.
	 */
	public void getProcessDefinition(String processId, AsyncResultHandler<JsonObject> resultHandler) {
		pkiClient.get(basePath + "/processes/" + processId, new JsonResponseHandler(resultHandler)).end();
	}
	
	public void getProcessInstance(String processInstanceId, AsyncResultHandler<JsonObject> resultHandler) {
		pkiClient.get(basePath + "/instances/" + processInstanceId, new JsonResponseHandler(resultHandler)).end();
	}
	
	public void getProcessElement(String processId, String elementId, AsyncResultHandler<JsonObject> resultHandler) {
		pkiClient.get(basePath + "/processes/" + processId + "/elements/" + elementId, new JsonResponseHandler(resultHandler)).end();
	}
	
	/**
	 * Instantiates a process.
	 * @param processId ID of the process definition to instantiate.
	 * @param userId ID of the user instantiating the process.
	 * @param resultHandler Handler for the asynchronous request. 
	 */
	public void instantiateProcess(String processId, String sessionId, String userId, AsyncResultHandler<JsonObject> resultHandler) {
		StringBuilder pathBuilder = new StringBuilder(50);
		pathBuilder.append(basePath).append("/processes/").append(processId).append("/instantiate");
		pathBuilder.append("?sid=").append(sessionId);
		pathBuilder.append("&userId=").append(userId);
		pkiClient.post(pathBuilder.toString(), new JsonResponseHandler(resultHandler)).end();
	}
	
	public void next(String processInstanceId, String sessionId, String elementId, AsyncResultHandler<JsonObject> resultHandler) {
		StringBuilder pathBuilder = new StringBuilder(50);
		pathBuilder.append(basePath).append("/instances/").append(processInstanceId).append("/next").append("?sid=").append(sessionId);
		if (elementId != null) pathBuilder.append("&elementId=").append(elementId);
		pkiClient.post(pathBuilder.toString(), new JsonResponseHandler(resultHandler)).end();
	}
	
	public void confirm(String processInstanceId, String sessionId, AsyncResultHandler<JsonObject> resultHandler) {
		StringBuilder pathBuilder = new StringBuilder(50);
		pathBuilder.append(basePath).append("/instances/").append(processInstanceId).append("/confirm").append("?sid=").append(sessionId);
		pkiClient.post(pathBuilder.toString(), new JsonResponseHandler(resultHandler)).end();
	}
	
	public void previous(String processInstanceId, String sessionId, AsyncResultHandler<JsonObject> resultHandler) {
		StringBuilder pathBuilder = new StringBuilder(50);
		pathBuilder.append(basePath).append("/instances/").append(processInstanceId).append("/previous").append("?sid=").append(sessionId);
		pkiClient.post(pathBuilder.toString(), new JsonResponseHandler(resultHandler)).end();
	}
	
	public void terminate(String processInstanceId, String sessionId, final AsyncResultHandler<Void> resultHandler) {
		StringBuilder pathBuilder = new StringBuilder(50);
		pathBuilder.append(basePath).append("/instances/").append(processInstanceId).append("/terminate").append("?sid=").append(sessionId);
		pkiClient.post(pathBuilder.toString(), new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(final HttpClientResponse response) {
				response.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						final String body = buffer.toString();
						if (resultHandler != null) resultHandler.handle(new AsyncResult<Void>() {
							
							@Override
							public boolean succeeded() {
								return response.statusCode() == 200;
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
								return failed() ? new HttpException(body, response.statusCode()) : null;
							}
						});
					}
				});
			}
		}).end();
	}
	
	public void getCurrentElement(String processInstanceId, String sessionId, AsyncResultHandler<JsonObject> resultHandler) {
		StringBuilder pathBuilder = new StringBuilder(50);
		pathBuilder.append(basePath).append("/instances/").append(processInstanceId).append("/currentElement").append("?sid=").append(sessionId);
		pkiClient.get(pathBuilder.toString(), new JsonResponseHandler(resultHandler)).end();
	}
	
}
