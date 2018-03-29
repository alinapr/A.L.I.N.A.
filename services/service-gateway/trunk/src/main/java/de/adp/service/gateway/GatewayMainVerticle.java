package de.adp.service.gateway;

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Main verticle for the service gateway. It deploys other system services depending on its configuration. 
 * @author simon.schwantzer(at)im-c.de
 */
public class GatewayMainVerticle extends Verticle {
	private JsonObject config;
	private RouteMatcher routeMatcher;

	@Override
	public void start() {
		if (container.config() != null && container.config().size() > 0) {
			config = container.config();
		} else {
			container.logger().warn("Warning: No configuration applied! Using default settings.");
			config = getDefaultConfiguration();
		}
		
		deployServices();
		
		initializeEventBusHandler();
		initializeHTTPRouting();
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(routeMatcher);
		
		// Initialize the event bus client bridge.
		JsonObject bridgeConfig = config.getObject("eventBusBridge");
		if (bridgeConfig != null) {
			bridgeConfig.putString("prefix", "/eventbus");
			
			vertx.createSockJSServer(httpServer).bridge(bridgeConfig, bridgeConfig.getArray("inbound"), bridgeConfig.getArray("outbound"));
		}
		httpServer.listen(config.getObject("webserver").getInteger("port"));
		
		container.logger().info("Service Gateway has been initialized.");
	}
	
	@Override
	public void stop() {
		container.logger().info("Service Gateway has been stopped.");
	}
	
	/**
	 * Create a configuration which used if no configuration is passed to the module.
	 * @return Configuration object.
	 */
	private static JsonObject getDefaultConfiguration() {
		JsonObject defaultConfig =  new JsonObject();
		JsonObject webserverConfig = new JsonObject();
		webserverConfig.putNumber("port", 8088);
		webserverConfig.putString("statics", "www");
		defaultConfig.putObject("webserver", webserverConfig);
		defaultConfig.putObject("services", new JsonObject());
		return defaultConfig;
	}
	
	private void deployServices() {
		JsonArray deploys = config.getArray("deploys");
		if (deploys != null) {
			Set<String> serviceIds = new HashSet<>();
			for (Object entry : deploys) {
				serviceIds.add(((JsonObject) entry).getString("id"));
			}
			ResultAggregationHandler<String, String> resultAggregation = new ResultAggregationHandler<String, String>(serviceIds, new AsyncResultHandler<String>() {

				@Override
				public void handle(AsyncResult<String> deployRequest) {
					if (deployRequest.succeeded()) {
						container.logger().info("Successfully deployed all system services.");
						initializeSystemServices();
					} else {
						container.logger().error("Failed to deploy all system services. Aborting.", deployRequest.cause());
						System.exit(1);
					}
				}
			});
			for (Object entry : deploys) {
				JsonObject deployConfig = (JsonObject) entry;
				String serviceId = deployConfig.getString("id");
				JsonObject config = deployConfig.getObject("config");
				container.logger().info("Trying to deploy system service: " + serviceId);
				container.deployModule(serviceId, config, resultAggregation.getRequestHandler(serviceId));
			}
		}
	}
	
	/**
	 * Initializes all services with the given configurations.
	 */
	private void initializeSystemServices() {
		JsonObject services = config.getObject("services");
		ResultAggregationHandler<String, String> resultAggregation = new ResultAggregationHandler<String, String>(services.getFieldNames(), new AsyncResultHandler<String>() {

			@Override
			public void handle(AsyncResult<String> deployRequest) {
				if (deployRequest.succeeded()) {
					container.logger().info("Successfully deployed all system services.");
				} else {
					container.logger().error("Failed to deploy system services.", deployRequest.cause());
					System.exit(1);
				}
			}
		});
		for (String serviceId : services.getFieldNames()) {
			JsonObject serviceConfig = services.getObject(serviceId);
			StringBuilder builder = new StringBuilder(100);
			builder.append("de.adp.service~");
			builder.append(serviceId);
			builder.append("~");
			builder.append(serviceConfig.getString("version"));
			container.logger().info("Trying to deploy system service: " + serviceId);
			container.deployModule(builder.toString(), serviceConfig.getObject("config"), resultAggregation.getRequestHandler(serviceId));
		}
	}
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {
		Handler<Message<JsonObject>> serviceConfigRequestReplyHandler = new Handler<Message<JsonObject>>() {
			
			@Override
			public void handle(Message<JsonObject> message) {
				JsonObject messageBody = message.body();
				String serviceId = messageBody.getString("serviceId");
				if (serviceId != null) {
					JsonObject serviceConfig = config.getObject("services").getObject(serviceId);
					if (serviceConfig == null) {
						serviceConfig = new JsonObject();
					}
					message.reply(serviceConfig);
				} else {
					message.reply(config.getObject("services"));
				}
			}
		};
		
		vertx.eventBus().registerHandler("adp:services:gateway#getServiceConfig", serviceConfigRequestReplyHandler);	
	}
	
	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {
		routeMatcher = new RouteMatcher();
		
		// All requests to /services/:serviceId/... are proxied to the webserver of the related service.
		for (String serviceId : config.getObject("services").getFieldNames()) {
			JsonObject serviceConfig = config.getObject("services").getObject(serviceId);
			JsonObject serviceWebserverConfig = serviceConfig.getObject("config").getObject("webserver");
			if (serviceWebserverConfig != null) {
				HttpServiceProxy httpServiceProxy = new HttpServiceProxy(vertx, serviceId, serviceWebserverConfig);
				routeMatcher.allWithRegEx(serviceWebserverConfig.getString("basePath") + "/.*", httpServiceProxy);
			}
		}

		// Lists the service configuration.
		routeMatcher.get("/services", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				response.end(config.getObject("services").encodePrettily());
			}
		});
		
		/*
		final String staticFileDirecotry = config.getObject("webserver").getString("statics");
		routeMatcher.getWithRegEx("/.*", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				request.response().sendFile(staticFileDirecotry + request.path());
			}
		});
		*/
	}
}
