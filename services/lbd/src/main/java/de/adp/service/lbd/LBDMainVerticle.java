package de.adp.service.lbd;

import java.util.*;

import org.vertx.java.core.*;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adp.commons.event.UserOnlineEvent;
import de.adp.commons.util.EventUtil;
import de.adp.service.auth.connector.AuthServiceConnector;
import de.adp.service.auth.connector.model.Session;
import de.adp.service.iid.server.connector.IIDConnector;
import de.adp.service.iid.server.model.*;
import de.adp.service.lbd.model.LocalState;
import de.adp.service.lbd.queries.LBDSparQLQueries;

/*
 * This verticle is executed with the module itself, i.e. initializes all components required by the service.
 * The super class provides two main objects used to interact with the container:
 * - <code>vertx</code> provides access to the Vert.x runtime like event bus, servers, etc.
 * - <code>container</code> provides access to the container, e.g, for accessing the module configuration an the logging mechanism.  
 */
public class LBDMainVerticle
    extends Verticle
{
	private JsonObject config;
    private BasePathRouteMatcher routeMatcher;
    private String basePath;
    private EventBus eb;
    private AuthServiceConnector authConn;
    private Logger log;

    private IIDConnector conn;
    // this map stores sessionIds(keys) and userIds (values)
    private final Map<String, String> sessionUserID = new HashMap<String, String>();

    // this map stores items to search learningcontents for
    private final Map<String, Set<String>> sessionItems = new HashMap<String, Set<String>>();

    private final Map<String, Set<String>> sessionWorkplaceGroups = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> sessionDevelopmentGoals = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> sessionStations = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> sessionMachines = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> sessionStates = new HashMap<String, Set<String>>();
    private final Map<String, Set<LocalState>> sessionLocalStates = new HashMap<String, Set<LocalState>>();
    private final String LBDTRIGGERADRESS = "adp:service:lernbedarfsdienst#startLearningSession";
    
    private final String ontologyPrefix = "http://www.appsist.de/ontology/";
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
        this.log = container.logger();

		/*
		 * In this method the verticle is registered at the event bus in order to receive messages. 
		 */
        this.eb = vertx.eventBus();
        // initialize Authentication Service Connector
        authConn = new AuthServiceConnector(this.eb, AuthServiceConnector.SERVICE_ID);
		initializeEventBusHandler();
        conn = new IIDConnector(this.eb, IIDConnector.DEFAULT_ADDRESS);
        // /*
        // * This block initializes the HTTP interface for the service.
        // */
        this.basePath = config.getObject("webserver").getString("basePath");
        initializeHTTPRouting();
        vertx.createHttpServer().requestHandler(routeMatcher)
                .listen(config.getObject("webserver").getInteger("port"));
		
        container.logger()
                .info("Service \"Lernbedarfsdienst\" has been initialized with the following configuration:\n"
                        + config.encodePrettily());
        // log.info("testrun");
        // String sid = "abrakadabra";
        // Set<String> wpgTest = new HashSet<String>();
        // wpgTest.add("app:Großserien_6:_Normzylinder");
        // wpgTest.add("app:Großserien_4:_Stromregelventile_1");
        // sessionWorkplaceGroups.put(sid, wpgTest);
        // requestStationsInWorkplaceGroups(sid);
	}
	
	@Override
	public void stop() {
        container.logger().info("Service \"Lernbedarfsdienst\" has been stopped.");
	}
	
	/**
	 * Create a configuration which used if no configuration is passed to the module.
	 * @return Configuration object.
	 */
	private static JsonObject getDefaultConfiguration() {
		JsonObject defaultConfig =  new JsonObject();
		JsonObject webserverConfig = new JsonObject();
        webserverConfig.putNumber("port", 7088);
        webserverConfig.putString("basePath", "/services/lbd");
		webserverConfig.putString("statics", "www");
		defaultConfig.putObject("webserver", webserverConfig);
		return defaultConfig;
	}
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {
        Handler<Message<JsonObject>> lbdEventHandler = new Handler<Message<JsonObject>>()
        {
			
			@Override
			public void handle(Message<JsonObject> message) {
				JsonObject messageBody = message.body();
                String sessionId = messageBody.getString("sid");
                String token = messageBody.getString("token");

                switch (message.address()) {
                    case "adp:service:lernbedarfsdienst#startLearningSession" :
                        getUserId(sessionId, token);
					break;
                    default :
                        // TODO store unregistered access attempts
                        break;
				}
			}
		};
		
		// Handlers are always registered for a specific address. 
        vertx.eventBus().registerHandler(this.LBDTRIGGERADRESS, lbdEventHandler);

        // handler for machinestate changes
        Handler<Message<JsonObject>> userOnlineEventHandler = new Handler<Message<JsonObject>>()
        {

            public void handle(Message<JsonObject> jsonMessage)
            {
                container.logger().info("lbd-MainVerticle " + jsonMessage.body());
                UserOnlineEvent uoe = (UserOnlineEvent) EventUtil
                        .parseEvent(jsonMessage.body().toMap());
                requestUserInformation(uoe.getSessionId(), uoe.getUserId(), "token");
            }
        };
        vertx.eventBus().registerHandler("adp:event:userOnline",
                userOnlineEventHandler);
	}
	
	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {
        routeMatcher = new BasePathRouteMatcher(this.basePath);

		final String staticFileDirecotry = config.getObject("webserver").getString("statics");
		
		routeMatcher.get("/testSparql", new Handler<HttpServerRequest>(){

            @Override
            public void handle(final HttpServerRequest request)
            {
                Handler<Message<String>> stringHandler = new Handler<Message<String>>(){

                    @Override
                    public void handle(Message<String> messageString)
                    {
                        log.info(messageString.body());
                        JsonObject result = new JsonObject(messageString.body());
                        Set<String> resultSet = new HashSet<String>();
                        JsonArray messageArray = result.getObject("results").getArray("bindings");
                        Iterator<Object> messageArrayIterator = messageArray.iterator();
                        while (messageArrayIterator.hasNext()) {
                            Object currentArrayEntry = messageArrayIterator.next();
                            if (currentArrayEntry instanceof JsonObject) {
                                resultSet.add(((JsonObject) currentArrayEntry).getObject("inhalt")
                                        .getString("value"));
                            }
                            else {
                                log.info("Expected JsonObject. Found "
                                        + currentArrayEntry.getClass());
                            }
                        }
                        log.info(resultSet);
                        request.response().end(messageString.body());

                    }
                    
                };
                LBDSparQLQueries.getContentsForStatesMachinesStations(
                        "{app:LoctiteLeer app:FettWenig app:S20 app:S10}",
                        eb,
                        stringHandler);
            }
		    
        });
		
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
				request.response().sendFile(staticFileDirecotry + request.path());
			}
		});
	}

    private void getUserId(String sessionId, final String token)
    {
        AsyncResultHandler<Session> sessionHandler = new AsyncResultHandler<Session>()
        {

            @Override
            public void handle(AsyncResult<Session> sessionRequest)
            {
                if (sessionRequest.succeeded()) {
                    Session session = sessionRequest.result();
                    if (null != session) {
                        requestUserInformation(session, token);
                    }
                }
            }

        };
        authConn.getSession(sessionId, token, sessionHandler);
    }

    private void requestUserInformation(String sessionId, String userId, String token)
    {
        this.sessionUserID.put(sessionId, userId);
        JsonObject request = new JsonObject();
        request.putString("sid", sessionId);
        request.putString("userId", userId);
        request.putString("token", token);
        Handler<Message<JsonObject>> userInformationHandler = new Handler<Message<JsonObject>>()
        {

            @Override
            public void handle(Message<JsonObject> message)
            {
                JsonObject messageBody = message.body();
                log.info("lbd - requestUserInformation");
                processUserInformation(messageBody);

            }

        };
        log.info("Sending request to for userInformation" + request);
        eb.send("adp:service:usermodel#getUserInformation", request, userInformationHandler);
    }

    private void requestUserInformation(Session session, String token)
    {
        this.sessionUserID.put(session.getId(), session.getUserId());
        JsonObject request = new JsonObject();
        request.putString("sid", session.getId());
        request.putString("userId", session.getUserId());
        request.putString("token", token);
        Handler<Message<JsonObject>> userInformationHandler = new Handler<Message<JsonObject>>(){

            @Override
            public void handle(Message<JsonObject> message)
            {
                JsonObject messageBody = message.body();
                processUserInformation(messageBody);

            }
            
        };
        eb.send("adp:service:usermodel#getUserInformation", request, userInformationHandler);
    }

    private void processUserInformation(JsonObject messageBody)
    {
        log.info("lbd/processUserInformation" + messageBody);
        JsonObject userInformation = messageBody.getObject("userInformation");
        // store information about user in corresponding maps
        String sessionId = messageBody.getString("sid");
        try {
            Set<String> apgs = new ObjectMapper()
                    .readValue(userInformation.getString("workplaceGroups"), Set.class);
            Set<String> devgoals = new ObjectMapper()
                    .readValue(userInformation.getString("developmentGoals"), Set.class);
            sessionDevelopmentGoals.put(sessionId, devgoals);
            sessionWorkplaceGroups.put(sessionId, apgs);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (userInformation.getString("currentWorkstate").equals("Haupttaetigkeit")) {
            requestStationsInWorkplaceGroups(sessionId);
        }
        else {
            buildLearningMaterialListSide(messageBody);
        }
    }

    private void requestStationsInWorkplaceGroups(final String sessionId)
    {
        try{
            // transform JsonString to JavaObject
            
            String workplaceGroups = "{";
                for (String workplaceGroup: sessionWorkplaceGroups.get(sessionId)){
                workplaceGroups += " " + sparqlPrefix(workplaceGroup);
                }
            workplaceGroups+="}";
            
            Handler<Message<String>> stationsInWorkplaceGroupsHandler = new Handler<Message<String>>() {

                @Override
                public void handle(Message<String> stringMessage)
                {
                    JsonObject result = new JsonObject(stringMessage.body());
                    Set<String> resultSet = new HashSet<String>();
                    JsonArray messageArray = result.getObject("results").getArray("bindings");
                    Iterator<Object> messageArrayIterator = messageArray.iterator();
                    while (messageArrayIterator.hasNext()) {
                        Object currentArrayEntry = messageArrayIterator.next();
                        if (currentArrayEntry instanceof JsonObject) {
                            resultSet.add(((JsonObject) currentArrayEntry).getObject("station")
                                    .getString("value"));
                        }
                        else {
                            log.info("Expected JsonObject. Found " + currentArrayEntry.getClass());
                        }
                    }
                    sessionStations.put(sessionId, resultSet);
                    requestCurrentStatesForStations(sessionId, resultSet);
                }
                
            };
            
            LBDSparQLQueries.getStationsInWorkplaceGroups(workplaceGroups, eb, stationsInWorkplaceGroupsHandler);
            
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    private void requestCurrentStatesForStations(String sessionId, Set<String> stationSet)
    {
        String stateStation = "{";
        for (String station : stationSet) {

            stateStation += "(app:LotiteLeer " + sparqlPrefix(station) + ")";
        }
        stateStation += "}";
        requestLocalStates(sessionId, stateStation);
    }

    private void requestLocalStates(final String sessionId, String stateStation)
    {
        Handler<Message<String>> localStatesHandler = new Handler<Message<String>>(){

            @Override
            public void handle(Message<String> messageString)
            {
                JsonObject result = new JsonObject(messageString.body());
                JsonArray messageArray = result.getObject("results").getArray("bindings");
                Iterator<Object> jsonArrayIterator = messageArray.iterator();
                Set<LocalState> lsSet = new HashSet<LocalState>();

                Set<String> localStates = new HashSet<String>();
                while (jsonArrayIterator.hasNext()) {
                    Object iteratorObject = jsonArrayIterator.next();
                    if (iteratorObject instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) iteratorObject;
                        LocalState ls = new LocalState(
                                sparqlPrefix(jsonObject.getObject("z").getString("value")),
                                sparqlPrefix(jsonObject.getObject("station").getString("value")),
                                sparqlPrefix(jsonObject.getObject("zt").getString("value")),
                                sparqlPrefix(jsonObject.getObject("p").getString("value")));
                        lsSet.add(ls);
                        localStates.add(ls.getState());
                    }
                }
                sessionLocalStates.put(sessionId, lsSet);
                sessionStates.put(sessionId, localStates);
                requestMachinesInWorkplaceGroups(sessionId);
            }
        };
        LBDSparQLQueries.getLocalStates(stateStation, eb, localStatesHandler);
    }

    private void requestMachinesInWorkplaceGroups(final String sessionId)
    {

        Handler<Message<String>> machinesInWorkplaceGroupsHandler = new Handler<Message<String>>()
        {

            @Override
            public void handle(Message<String> stringMessage)
            {
                JsonObject result = new JsonObject(stringMessage.body());
                Set<String> resultSet = new HashSet<String>();
                JsonArray messageArray = result.getObject("results").getArray("bindings");
                Iterator<Object> messageArrayIterator = messageArray.iterator();
                while (messageArrayIterator.hasNext()) {
                    Object currentArrayEntry = messageArrayIterator.next();
                    if (currentArrayEntry instanceof JsonObject) {
                        resultSet.add(((JsonObject) currentArrayEntry).getObject("anlage")
                                .getString("value"));
                    }
                    else {
                        log.info("Expected JsonObject. Found " + currentArrayEntry.getClass());
                    }
                }
                sessionMachines.put(sessionId, resultSet);
                requestLearningMaterialListMain(sessionId);
            }

        };
        Set<String> workplaceGroups = this.sessionWorkplaceGroups.get(sessionId);
        String workplaceGroupsString = "{";
        for (String workplaceGroup : workplaceGroups) {
            workplaceGroupsString += " " + sparqlPrefix(workplaceGroup);
        }
        workplaceGroupsString += "}";
        LBDSparQLQueries.getMachinesInWorkplaceGroups(workplaceGroupsString, eb,
                machinesInWorkplaceGroupsHandler);
    }

    private void requestLearningMaterialListMain(final String sessionId)
    {
        Handler<Message<String>> learningMaterialListMainHandler = new Handler<Message<String>>(){

            @Override
            public void handle(Message<String> messageString)
            {
                JsonObject result = new JsonObject(messageString.body());
                Set<String> resultSet = new HashSet<String>();
                JsonArray messageArray = result.getObject("results").getArray("bindings");
                Iterator<Object> messageArrayIterator = messageArray.iterator();
                while (messageArrayIterator.hasNext()) {
                    Object currentArrayEntry = messageArrayIterator.next();
                    if (currentArrayEntry instanceof JsonObject) {
                        resultSet.add(((JsonObject) currentArrayEntry).getObject("inhalt")
                                .getString("value"));
                    }
                    else {
                        log.info("Expected JsonObject. Found "
                                + currentArrayEntry.getClass());
                    }
                }
                buildLearningMaterialList(sessionId, resultSet);
            }
        };
        String ids = "{";
        for (String state : this.sessionStates.get(sessionId)) {
            ids += " " + sparqlPrefix(state);
        }
        for (String machine : this.sessionMachines.get(sessionId)) {
            ids += " " + sparqlPrefix(machine);
        }
        for (String station : this.sessionStations.get(sessionId)) {
            ids += " " + sparqlPrefix(station);
        }
        ids += "}";
        LBDSparQLQueries.getContentsForStatesMachinesStations(ids, eb,
                learningMaterialListMainHandler);
    }

    private void buildLearningMaterialList(String sessionId, Set<String> contentIds)
    {
        log.info("Reached end of process");
        log.info(contentIds);
        List<ServiceItem> serviceItemList = new ArrayList<ServiceItem>();
        int priority = contentIds.size();
        for (String contentId : contentIds) {
            LearningObjectItemBuilder loib = new LearningObjectItemBuilder();
            JsonObject jo = new JsonObject();
            jo.putString("sessionId", sessionId).putString("token", "token").putString("processId",
                    contentId);
            JsonObject bo = new JsonObject();
            bo.putObject("body", jo);
            SendMessageAction sma = new SendMessageAction(
                    "adp:content-navigation-service#learningObjectStart", bo);
            loib.setPriority(priority--).setTitle(prettifyContentId(contentId))
                    .setId("lbd-" + System.currentTimeMillis()).setService("lbd")
                    .setImageUrl("/services/cds/static/Produktionsanlage.jpg").setAction(sma);
            serviceItemList.add(loib.build());
        }
        conn.addServiceItems(sessionId, serviceItemList, null);
    }
    private void buildLearningMaterialListSide(JsonObject message)
    {

    }

    private String sparqlPrefix(String original)
    {
        return original.replace(this.ontologyPrefix, "app:");
    }

    private String prettifyContentId(String contentId)
    {
        String result = contentId;
        switch (result) {
            // TODO: add cases here
        }
        return result;
    }
}


