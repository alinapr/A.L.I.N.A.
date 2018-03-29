package de.adp.service.nsaservice;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import de.adp.commons.event.*;
import de.adp.commons.util.EventUtil;
import de.adp.service.nsaservice.model.Trigger;

/*
 * This verticle is executed with the module itself, i.e. initializes all components required by the service.
 * The super class provides two main objects used to interact with the container:
 * - <code>vertx</code> provides access to the Vert.x runtime like event bus, servers, etc.
 * - <code>container</code> provides access to the container, e.g, for accessing the module configuration an the logging mechanism.  
 */
public class NSAMainVerticle
    extends Verticle
{
	private JsonObject config;
	private RouteMatcher routeMatcher;

    // ontology prefix
    private final String ontologyPrefix = "http://www.appsist.de/ontology/";

    // logging facility
    private Logger log;

	@Override
	public void start() {
		/* 
		 * The module can be configured by one of the following ways:
		 * - The module is executed with from the command line with the option "-conf <filename>".
		 * - The module is executed programmatically with a configuration object. 
		 * - The (hardcoded) default configuration is applied if none of the above options has been applied.  
		 */
		if (container.config() != null && container.config().size() > 0) {
			config = container.config();
		} else {
			container.logger().warn("Warning: No configuration applied! Using default settings.");
			config = getDefaultConfiguration();
		}
        log = container.logger();
		/*
		 * In this method the verticle is registered at the event bus in order to receive messages. 
		 */
		initializeEventBusHandler();
		
		/*
		 * This block initializes the HTTP interface for the service. 
		 */
		initializeHTTPRouting();
		vertx.createHttpServer()
			.requestHandler(routeMatcher)
			.listen(config.getObject("webserver")
			.getInteger("port"));
		
        log.info(
                "Service \"NSA-Service\" has been initialized with the following configuration:\n"
                        + config.encodePrettily());
	}
	
	@Override
	public void stop() {
        container.logger().info("Service \"NSA-Service\" has been stopped.");
	}
	
	/**
	 * Create a configuration which used if no configuration is passed to the module.
	 * @return Configuration object.
	 */
	private static JsonObject getDefaultConfiguration() {
		JsonObject defaultConfig =  new JsonObject();
		JsonObject webserverConfig = new JsonObject();
        webserverConfig.putNumber("port", 7087);
		webserverConfig.putString("statics", "www");
		defaultConfig.putObject("webserver", webserverConfig);
		return defaultConfig;
	}
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {
		Handler<Message<JsonObject>> pkiEventHandler = new Handler<Message<JsonObject>>() {
			
			@Override
			public void handle(Message<JsonObject> message) {
				
				switch (message.address()) {
                    case Trigger.PROCESSSTART :
                        ProcessStartEvent pse = EventUtil.parseEvent(message.body().toMap(),
                                ProcessStartEvent.class);
                        log.info(pse);
                        handleProcessStartEvent(pse);
					break;

                    case Trigger.PROCESSERROR :
                        ProcessErrorEvent pee = EventUtil.parseEvent(message.body().toMap(),
                                ProcessErrorEvent.class);
                        handleProcessErrorEvent(pee);
					break;

                    case Trigger.PROCESSCANCELLED :
                        ProcessCancelledEvent pcae = EventUtil.parseEvent(message.body().toMap(),
                                ProcessCancelledEvent.class);
                        handleProcessCancelledEvent(pcae);
                        break;
                    case Trigger.PROCESSCOMPLETE :
                        ProcessCompleteEvent pce = EventUtil.parseEvent(message.body().toMap(),
                                ProcessCompleteEvent.class);
                        handleProcessCompleteEvent(pce);
                        break;
				}

				container.logger().info("Received a message on the event channel!");
			}
		};
		
		// Handlers are always registered for a specific address. 
        vertx.eventBus().registerHandler(Trigger.PROCESSCANCELLED,
                pkiEventHandler);
        vertx.eventBus().registerHandler(Trigger.PROCESSCOMPLETE, pkiEventHandler);
        vertx.eventBus().registerHandler(Trigger.PROCESSERROR, pkiEventHandler);
        vertx.eventBus().registerHandler(Trigger.PROCESSSTART, pkiEventHandler);
	}
	
	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {
		routeMatcher = new RouteMatcher();
        final String staticFileDirectory = config.getObject("webserver").getString("statics");
		
		/*
		 * The following rules are applied in the order given during the initialization.
		 * The first rule which matches the request is applied and the latter rules are ignored. 
		 */
		
		/*
		 * This rule applies to all request of type GET to a path like "/entries/abc".
		 * The path segment "abc" is being made available as request parameter "id".
		 */
		routeMatcher.get("/entries/:id", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				String id = request.params().get("id");
				request.response().end("Received request for entry '" + id + "'.");
			}
		});
		
		/*
		 * This entry serves files from a directory specified in the configuration.
		 * In the default configuration, the files are served from "src/main/resources/www", which is packaged with the module. 
		 */
		routeMatcher.getWithRegEx("/.*", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
                request.response().sendFile(staticFileDirectory + request.path());
			}
		});
	}

    private void handleProcessStartEvent(ProcessStartEvent pse)
    {
        String verb = "http://adlnet.gov/expapi/verbs/attempted";
        String actor = pse.getUserId();
        String object = pse.getProcessId();
        // TODO: check if processID is absolute
        if (!object.startsWith(ontologyPrefix)) {
            object = ontologyPrefix + object;
        }
        log.info(actor +" did "+ verb + " with "+object);
        spreadInformation(actor, verb, object);
    }

    private void handleProcessCompleteEvent(ProcessCompleteEvent pce)
    {
        String verb = "http://adlnet.gov/expapi/verbs/completed";
        String actor = pce.getUserId();
        String object = pce.getProcessId();
        // TODO: check if processID is absolute
        if (!object.startsWith(ontologyPrefix)) {
            object = ontologyPrefix + object;
        }
        spreadInformation(actor, verb, object);
    }

    private void handleProcessErrorEvent(ProcessErrorEvent pee)
    {
        String verb = "http://adlnet.gov/expapi/verbs/failed";
        String actor = pee.getUserId();
        String object = pee.getProcessId();
        // TODO: check if processID is absolute
        if (!object.startsWith(ontologyPrefix)) {
            object = ontologyPrefix + object;
        }
        spreadInformation(actor, verb, object);
    }

    private void handleProcessCancelledEvent(ProcessEvent pcae)
    {
        String verb = "http://purl.org/xapi/adl/verbs/abandoned";
        String actor = pcae.getUserId();
        String object = pcae.getProcessId();
        // TODO: check if processID is absolute
        if (!object.startsWith(ontologyPrefix)) {
            object = ontologyPrefix + object;
        }
        spreadInformation(actor, verb, object);
    }

    private void spreadInformation(String actor, String verb, String object)
    {
        JsonObject jo = new JsonObject();
        jo.putString("agent", actor);
        jo.putString("verb", verb);
        jo.putString("activityId", object);
        vertx.eventBus().send("adp:query:lrs#buildAndStoreStatement", jo);
    }
}
