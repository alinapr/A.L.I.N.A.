package de.adp.service.ps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import de.adp.service.auth.connector.AuthServiceConnector;
import de.adp.service.auth.connector.model.Session;
import de.adp.service.iid.server.connector.IIDConnector;
import de.adp.service.iid.server.model.HttpPostAction;
import de.adp.service.iid.server.model.InstructionItemBuilder;
import de.adp.service.iid.server.model.ServiceItem;
import de.adp.service.ps.connector.BMDConnector;
import de.adp.service.ps.connector.ISConnector;
import de.adp.service.ps.connector.PKIConnector;

/**
 * Main verticle for the performance support service.
 * @author simon.schwantzer(at)im-c.de  
 */
public class PSMainVerticle extends Verticle {
	public static final String SERVICE_ID = "psd";
	
	private JsonObject config;
	private RouteMatcher routeMatcher;
	private AuthServiceConnector authConnector;
	private PKIConnector pkiConnector;
	private IIDConnector iidConnector;
	private ISConnector isConnector;
	private BMDConnector bmdConnector;
	private UserInteractionHandler clientRequestHandler;
	private String basePath;

	@Override
	public void start() {
		if (container.config() != null && container.config().size() > 0) {
			config = container.config();
		} else {
			container.logger().warn("Warning: No configuration applied! Using default settings.");
			config = getDefaultConfiguration();
		}
		basePath = config.getObject("webserver").getString("basePath");
		JsonObject webserverConfig = config.getObject("webserver");
		StringBuilder builder = new StringBuilder(300);
		builder.append("http://localhost:").append(webserverConfig.getInteger("port")).append(webserverConfig.getString("basePath"));
		String baseUrl = builder.toString();
		
		// Deploy db on demand. 
		if (config.getValue("db") instanceof JsonObject) {
			container.deployModule("io.vertx~mod-mongo-persistor~2.1.0", container.config().getObject("db"), new Handler<AsyncResult<String>>() {
				@Override
				public void handle(AsyncResult<String> result) {
					if (result.succeeded()) {
						container.logger().info("Deployed Mongo DB connector.");
					} else {
						container.logger().warn("Failed to deploy Mongo DB connector.", result.cause());
					}
				}
			});
		}
		
		// Deploy authentication manager on demand.
		/*String authManagerAddress;
		if (config.getValue("auth") instanceof JsonObject) {
			container.deployModule("io.vertx~mod-auth-mgr~2.0.0-final", config, new Handler<AsyncResult<String>>() {
				@Override
				public void handle(AsyncResult<String> result) {
					if (result.succeeded()) {
						container.logger().info("Deployed authentication manager.");
					} else {
						container.logger().warn("Failed to deploy authentication manager.", result.cause());
					}
				}
			});
			authManagerAddress = config.getObject("auth").getString("address");
		} else {
			authManagerAddress = config.getString("auth");
		}*/

		authConnector = new AuthServiceConnector(vertx.eventBus(), AuthServiceConnector.SERVICE_ID);
		bmdConnector = new BMDConnector(vertx.eventBus());
		pkiConnector = new PKIConnector(vertx, container, config.getObject("services"));
		isConnector = new ISConnector(vertx, config.getObject("services"));
		iidConnector = new IIDConnector(vertx.eventBus(), IIDConnector.DEFAULT_ADDRESS);
		clientRequestHandler = new UserInteractionHandler(authConnector, bmdConnector, pkiConnector, iidConnector, isConnector, container.logger(), baseUrl);
		initializeEventBusHandler();
		
		initializeHTTPRouting();
		vertx.createHttpServer()
			.requestHandler(routeMatcher)
			.listen(webserverConfig.getInteger("port"));
		
		container.logger().info("Service \"Performance Support\" has been initialized with the following configuration:\n" + config.encodePrettily());
	}
	
	@Override
	public void stop() {
		container.logger().info("Service \"Performance Support\" has been stopped.");
	}
	
	/**
	 * Create a configuration which used if no configuration is passed to the module.
	 * @return Configuration object.
	 */
	private static JsonObject getDefaultConfiguration() {
		JsonObject defaultConfig =  new JsonObject();
		JsonObject webserverConfig = new JsonObject();
		webserverConfig.putNumber("port", 8088);
		webserverConfig.putString("statics", "static");
		webserverConfig.putString("basePath", "");
		defaultConfig.putObject("webserver", webserverConfig);
		return defaultConfig;
	}
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {
		// Handlers are always registered for a specific address. 
		/*vertx.eventBus().registerHandler("adp:event:supportRequest", new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> event) {
				try {
					SupportRequestEvent requestEvent = (SupportRequestEvent) EventUtil.parseEvent(event.body().toMap());
					clientRequestHandler.handleSupportRequest(requestEvent);
				} catch (IllegalArgumentException | ClassCastException e) {
					container.logger().warn("Failed to parse event.", e);
				}
			}
		});*/
	}
	
	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {
		routeMatcher = new BasePathRouteMatcher(basePath);
		// final String staticFileDirecotry = config.getObject("webserver").getString("statics");
		
		routeMatcher.post("/startSupport/:supportId", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				final String supportId = request.params().get("supportId");
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						JsonObject body = new JsonObject(buffer.toString());
						System.out.println("Start assistance: " + body.encodePrettily());
						final String sessionId = body.getString("sessionId");
						final String token = body.getString("token");
						
						authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {
							
							@Override
							public void handle(AsyncResult<Session> event) {
								if (event.succeeded()) {
									clientRequestHandler.setClientToken(sessionId, token);
									Session session = event.result();
									
									clientRequestHandler.handleStartSupportRequest(response, supportId, session);
								} else {
									container.logger().warn("Failed to retrieve user session.", event.cause());
									response.setStatusCode(500).end(event.cause().getMessage());
								}
							}
						});
					}
				});
			}
		});
		
		routeMatcher.post("/navigate/confirm", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(final HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				request.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer buffer) {
						final JsonObject body = new JsonObject(buffer.toString());
						final String sessionId = body.getString("sessionId");
						final String token = body.getString("token");
						authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {

							@Override
							public void handle(AsyncResult<Session> event) {
								if (event.succeeded()) {
									clientRequestHandler.setClientToken(sessionId, token);
									Session session = event.result();
									clientRequestHandler.handleConfirmRequest(request.response(), session, body.getString("processId"));
								} else {
									container.logger().warn("Failed to retrieve user session.", event.cause());
									response.setStatusCode(500).end(event.cause().getMessage());
								}
							}
						});
					}
				});
			}
		});
		
		routeMatcher.post("/navigate/next", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				final String elementId = request.params().get("elementId");
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						final JsonObject body = new JsonObject(buffer.toString());
						final String sessionId = body.getString("sessionId");
						final String token = body.getString("token");
						authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {

							@Override
							public void handle(AsyncResult<Session> event) {
								if (event.succeeded()) {
									clientRequestHandler.setClientToken(sessionId, token);
									Session session = event.result();
									clientRequestHandler.handleNextRequest(request.response(), session, elementId);
								} else {
									container.logger().warn("Failed to retrieve user session.", event.cause());
									response.setStatusCode(500).end(event.cause().getMessage());
								}
							}
						});
					}
				});
			}
		});
		
		routeMatcher.post("/navigate/previous", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						final JsonObject body = new JsonObject(buffer.toString());
						final String sessionId = body.getString("sessionId");
						final String token = body.getString("token");
						authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {

							@Override
							public void handle(AsyncResult<Session> event) {
								if (event.succeeded()) {
									clientRequestHandler.setClientToken(sessionId, token);
									Session session = event.result();
									clientRequestHandler.handlePreviousRequest(response, session);
								} else {
									container.logger().warn("Failed to retrieve user session.", event.cause());
									response.setStatusCode(500).end(event.cause().getMessage());
								}
							}
						});
					}
				});
			}
		});
		
		routeMatcher.post("/navigate/details", new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				request.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						final JsonObject body = new JsonObject(buffer.toString());
						final String sessionId = body.getString("sessionId");
						final String token = body.getString("token");
						final String activityProcessId = body.getString("activityProcessId");
						authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {

							@Override
							public void handle(AsyncResult<Session> event) {
								if (event.succeeded()) {
									clientRequestHandler.setClientToken(sessionId, token);
									Session session = event.result();
									clientRequestHandler.handleDetailsRequest(response, session, token, activityProcessId);
								} else {
									container.logger().warn("Failed to retrieve user session.", event.cause());
									response.setStatusCode(500).end(event.cause().getMessage());
								}
							}
						});
					}
				});
			}
			
		});
		
		routeMatcher.post("/navigate/close", new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				request.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						final JsonObject body = new JsonObject(buffer.toString());
						final String sessionId = body.getString("sessionId");
						final String token = body.getString("token");
						authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {

							@Override
							public void handle(AsyncResult<Session> event) {
								if (event.succeeded()) {
									clientRequestHandler.setClientToken(sessionId, token);
									Session session = event.result();
									clientRequestHandler.handleCloseRequest(response, session, token);
								} else {
									container.logger().warn("Failed to retrieve user session.", event.cause());
									response.setStatusCode(500).end(event.cause().getMessage());
								}
							}
						});
					}
				});
			}
		});
		
		if (config.getBoolean("debugMode", false)) {
			routeMatcher.post("/debug/addServiceItem", new Handler<HttpServerRequest>() {
				
				@Override
				public void handle(HttpServerRequest request) {
					final HttpServerResponse response = request.response();
					request.bodyHandler(new Handler<Buffer>() {
						
						@Override
						public void handle(Buffer buffer) {
							JsonObject body = new JsonObject(buffer.toString());
							String sessionId = body.getString("sessionId");
							String processId = body.getString("processId");
							String title = body.getString("title");
							
							List<ServiceItem> serviceItems = new ArrayList<>(); 
							HttpPostAction action = new HttpPostAction("http://localhost:8080/services/psd/startSupport/" + processId, new JsonObject());
							serviceItems.add(new InstructionItemBuilder().setId(UUID.randomUUID().toString()).setPriority(50).setService("psd").setTitle(title).setAction(action).build());
							
							
							iidConnector.addServiceItems(sessionId, serviceItems, new AsyncResultHandler<Void>() {
								
								@Override
								public void handle(AsyncResult<Void> addRequest) {
									if (addRequest.succeeded()) {
										response.end();
									} else {
										response.setStatusCode(500).end(addRequest.cause().getMessage());
									}
								}
							}); 
						}
					});
					
				}
			});
		}
	}
}
