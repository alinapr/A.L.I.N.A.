package de.adp.service.measuresservice;

import java.util.*;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adp.commons.event.MachineStateChangedEvent;
import de.adp.commons.event.UserOnlineEvent;
import de.adp.commons.util.EventUtil;
import de.adp.service.iid.server.connector.IIDConnector;
import de.adp.service.iid.server.model.*;
/*
 * This verticle is executed with the module itself, i.e. initializes all components required by the service.
 * The super class provides two main objects used to interact with the container:
 * - <code>vertx</code> provides access to the Vert.x runtime like event bus, servers, etc.
 * - <code>container</code> provides access to the container, e.g, for accessing the module configuration an the logging mechanism.  
 */
public class MeasuresMain
    extends Verticle
{
	private JsonObject config;
    private BasePathRouteMatcher routeMatcher;

    private Map<String, Set<JsonNode>> clearedMeasures;

    // needed prefix string to build SparQL queries
    private String prefixString = "";

    private String ontologyPrefix = "";

    // basePath String
    private String basePath = "";

    // eventbus
    private EventBus eb = null;

    // logger object
    private Logger log = null;

    // this Map stores session IDs of Logged in Users
    private Map<String, String> userSessionMap;

    // eventbus address prefix
    private String eventbusPrefix = "adp:";

    private IIDConnector conn;

	@Override
	public void start() {

        this.log = container.logger();
        /*
         * The module can be configured by one of the following ways: - The module is executed
         * with from the command line with the option "-conf <filename>". - The module is
         * executed programmatically with a configuration object. - The (hardcoded) default
         * configuration is applied if none of the above options has been applied.
         */

        if (container.config() != null && container.config().size() > 0) {
			config = container.config();
		} else {
            // container.logger().warn("Warning: No configuration applied! Using default settings.");
            log.info("Using default configuration");
			config = getDefaultConfiguration();
		}

        this.eb = vertx.eventBus();

        this.conn = new IIDConnector(this.eb, IIDConnector.DEFAULT_ADDRESS);
        this.basePath = config.getObject("webserver").getString("basePath");
		// init SparQL prefix string
        this.ontologyPrefix = config.getObject("sparql").getString("ontologyPrefix");
        setPrefixString("PREFIX " + this.ontologyPrefix + " <"
                + config.getObject("sparql").getString("ontologyUri") + ">");
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
        // for testing purposes invoke SparQL connector and usermodel service here
        // container.deployModule("de.adp.service~mid~0.1.0-SNAPSHOT");
        // container.deployModule("de.adp.service~ucs~1.0-SNAPSHOT");
        // container.deployModule("de.adp.service~usermodel-service~0.1.0-SNAPSHOT");

        // initialize maps
        this.clearedMeasures = new HashMap<String, Set<JsonNode>>();
        this.userSessionMap = new HashMap<String, String>();
        container.logger().info("*******************");
        container.logger().info(
                "  Maßnahmendienst auf Port " + config.getObject("webserver").getNumber("port")
                        + " gestartet ");
        container.logger().info("                              *******************");
	}
	
	@Override
	public void stop() {
        container.logger().info("Service \"Massnahmendienst\" has been stopped.");
	}
	
	    /**
     * Create a configuration which is used if no configuration is passed to the module.
     * 
     * @return Configuration object.
     */
	private static JsonObject getDefaultConfiguration() {
		JsonObject defaultConfig =  new JsonObject();
		JsonObject webserverConfig = new JsonObject();
        webserverConfig.putNumber("port", 7082);
        webserverConfig.putString("basePath", "/services/measures");
        // TODO: test statics with relative path
        // until now only full path is working

        webserverConfig.putString("statics",
                "www");

		defaultConfig.putObject("webserver", webserverConfig);
        JsonObject sparqlConfig = new JsonObject();
        
        sparqlConfig.putString("ontologyPrefix", "app:");
        sparqlConfig.putString("ontologyUri", "http://www.appsist.de/ontology/");
        defaultConfig.putObject("sparql", sparqlConfig);
		return defaultConfig;
	}
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {
        // handler for machinestate changes
        Handler<Message<JsonObject>> machineStateChangedHandler = new Handler<Message<JsonObject>>()
        {
            public void handle(Message<JsonObject> jsonMessage)
            {
                MachineStateChangedEvent msce = (MachineStateChangedEvent) EventUtil
                        .parseEvent(jsonMessage.body().toMap());
                processMachineStateChangedEvent(msce, false, "");
            }
        };
        vertx.eventBus().registerHandler(this.eventbusPrefix + "event:machinestateChangedEvent",
                machineStateChangedHandler);
		
        // handler for machinestate changes
        Handler<Message<JsonObject>> userOnlineEventHandler = new Handler<Message<JsonObject>>()
        {
            public void handle(Message<JsonObject> jsonMessage)
            {
                UserOnlineEvent uoe = (UserOnlineEvent) EventUtil.parseEvent(jsonMessage.body()
                        .toMap());
                processUserOnlineEvent(uoe);

                processMachineStateChangedEvent(null, true, uoe.getUserId());
            }
        };
        vertx.eventBus().registerHandler(this.eventbusPrefix + "event:userOnline",
                userOnlineEventHandler);
	}
	
	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {
        routeMatcher = new BasePathRouteMatcher(this.basePath);
	}


    public String getPrefixString()
    {
        return this.prefixString;
    }

    private void setPrefixString(String prefix)
    {
        // check for valid prefix needed here
        this.prefixString = prefix;
    }

    private void processMachineStateChangedEvent(MachineStateChangedEvent msce, boolean demo,
            String uId)
    {
        Handler<Message<String>> handleMeasuresForStatesResult = new Handler<Message<String>>()
        {

            @Override
            public void handle(Message<String> arg0)
            {
                log.info("handleMeasuresForStatesResult-" + arg0.body());
                processReceivedMeasureList(arg0.body());
            }
        };
        String newMachineState = "";
        if (!demo) {
            newMachineState = "{app:" + msce.getErrorTag() + "}";
        }
        else {
            log.info(uId);
            if (uId.equals("alice.tester@example.com")) {

                newMachineState = "{app:GemessenerFehler}";
            }
            else {
                newMachineState = "{app:FehlendesBauteil}";
            }
            log.info("set new machinestate:" + newMachineState);
        }

        BasicSparQLQueries.getMeasuresForStates(newMachineState, eb,
                handleMeasuresForStatesResult);

        // we currently assume that new machine state is related to the user currently logged
        // into the system

    }

    private void processReceivedMeasureList(String messageString)
    {
        Handler<Message<JsonObject>> handleAllowedMeasuresForUser = new Handler<Message<JsonObject>>()
        {

            @Override
            public void handle(Message<JsonObject> arg0)
            {
                log.info("measure/handleAllowedMeasuresForUSer: processReceivedMeasureList");
                log.info("measure/handleAllowedMeasuresForUSer:" + arg0.body().encodePrettily());
                final String sessionId = arg0.body().getString("sid");
                ArrayList<String> measureList = new ArrayList<String>();
                try {
                    measureList = new ObjectMapper().readValue(
                            arg0.body().getString("applicableMeasures"), ArrayList.class);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Handler<Message<String>> handleMeasureLabels = new Handler<Message<String>>(){

                    @Override
                    public void handle(Message<String> arg0)
                    {
                        log.info("handleMeasureLabels:" + arg0.body());
                        // build HashMap with measure/label
                        HashMap<String, String> measureLabels = new HashMap<String, String>();
                        JsonObject jsonObject = new JsonObject(arg0.body());
                        JsonArray jsonArray = jsonObject.getObject("results").getArray("bindings");
                        Iterator<Object> jsonArrayIterator = jsonArray.iterator();
                        while (jsonArrayIterator.hasNext()) {
                            Object currentObject = jsonArrayIterator.next();
                            if (currentObject instanceof JsonObject) {
                                JsonObject currentJsonObject = (JsonObject) currentObject;
                                String measure = currentJsonObject.getObject("oc")
                                        .getString("value");
                                String label = currentJsonObject.getObject("label")
                                        .getString("value");
                                measureLabels.put(measure, label);
                            }
                        }
                        // update catalog

                        log.info("measures/processReceivedMeasureList: " + measureLabels);
                        buildServiceItems(measureLabels, sessionId);
                    }
                };

                String measuresForQuery = "{";
                for (String measure : measureList) {
                    measuresForQuery = measuresForQuery + " <" + measure + ">";
                }
                measuresForQuery += "}";
                BasicSparQLQueries.getLabelFor(measuresForQuery, "de", eb, handleMeasureLabels);
            }

        };

        JsonObject jo = new JsonObject(messageString);
        JsonObject message = new JsonObject();
        Iterator<String> userSessionKeys = this.userSessionMap.keySet().iterator();
        String uId = userSessionKeys.next();
        String sId = this.userSessionMap.get(uId);

        message.putString("userId", uId);
        message.putString("sid", sId);
        message.putObject("applicableMeasures", jo);
        log.info(message.encodePrettily());
        eb.send(this.eventbusPrefix + "service:usermodel#allowedMeasures", message,
                handleAllowedMeasuresForUser);
    }
    
    // method to build ServiceItemsList
    private void buildServiceItems(Map<String, String> measureLabels, String sessionId)
    {
        log.info(measureLabels.keySet().size() + " | " + sessionId);
        List<ServiceItem> serviceItemList = new ArrayList<ServiceItem>();
        for (String measure : measureLabels.keySet()) {
            InstructionItemBuilder iib = new InstructionItemBuilder();
            iib.setTitle(measureLabels.get(measure));
            iib.setId(System.currentTimeMillis() + "");
            iib.setPriority(5);
            iib.setService("measures-service");
            
            HttpPostAction hpa = new HttpPostAction(
                    "http://localhost:8080/services/psd/startSupport/567f5c4c-1f2b-11e5-b5f7-727283247c7f",
                    null);
            iib.setAction(hpa);
            InstructionItem ii = iib.build();
            serviceItemList.add(ii);

        }
        conn.addServiceItems(sessionId, serviceItemList, null);
    }


    // after user logs in -> send information to IID
    private void processUserOnlineEvent(UserOnlineEvent uoe)
    {
        String user = uoe.getUserId();
        this.userSessionMap.put(user, uoe.getSessionId());
    }
    

    


    

    // returns pretty Name for Maßnahmen
    private String prettyMeasureName(String measure)
    {
        switch (measure) {
            case "MassnahmeFuerLoctite" : {
                return "Ma&szlig;nahme f&uuml;r Loctite";
            }
            case "WerkstoffWechseln" : {
                return "Werkstoff Wechseln";
            }
            case "Werkzeugbestueckung" : {
                return "Werkzeugbest&uuml;ckung";
            }
            case "allgemeineExterneStoerungsmassnahme" : {
                return "Allgemeine externe St&ouml;rungsma&szlig;nahme";
            }
            case "fehlendesBauteildiagnostizieren" : {
                return "Fehlendes Bauteil diagnostizieren";
            }
            case "spezielleMassnahmeFuerFett" : {
                return "Spezielle Ma&szlig;nahme f&uuml;r Fett";
            }
        }
        return measure;
    }


}