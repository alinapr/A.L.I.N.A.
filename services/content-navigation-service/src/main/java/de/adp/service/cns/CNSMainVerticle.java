package de.adp.service.cns;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import de.adp.service.cns.api.Methods;
import de.adp.service.cns.model.ContentNode;
import de.adp.service.cns.model.ContentStructure;
import de.adp.service.cns.utils.ContentStructureReader;
import de.adp.service.iid.server.connector.IIDConnector;
import de.adp.service.iid.server.model.*;

/*
 * This verticle is executed with the module itself, i.e. initializes all components required by the service.
 * The super class provides two main objects used to interact with the container:
 * - <code>vertx</code> provides access to the Vert.x runtime like event bus, servers, etc.
 * - <code>container</code> provides access to the container, e.g, for accessing the module configuration an the logging mechanism.  
 */
public class CNSMainVerticle
    extends Verticle
{
	private JsonObject config;
	private RouteMatcher routeMatcher;

    public static final String DEFAULT_ADDRESS = "adp:service:iid@updateDisplay";

    private Map<String, ContentStructure> contentStructures = new HashMap<String, ContentStructure>();


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
		}
		
		/*
		 * In this method the verticle is registered at the event bus in order to receive messages. 
		 */
		initializeEventBusHandler();
		
		/*
		 * This block initializes the HTTP interface for the service. 
		 */
		
        container.logger().info(
                "Service \"Lerninhalte Navigator\" has been initialized with the following configuration:\n");
	}
	
	@Override
	public void stop() {
        container.logger().info("Service \"Lerninhalte Navigator\" has been stopped.");
	}
	




	/**
	 * In this method the handlers for the event bus are initialized.
     *
     * a) the initial trigger, to start a Lerninhalt
     * b) go-back, go-to-next, go-back-to-first step
     *
     * a map (sessionId->processStructure) is hosting different sessionIds with their processStructures
     *
     * next/back-Buttons only holding information back/next ,
     *      the actual process progress is stored in ContentStructure
     *      -> node->visited
     *      -> adaptionService can intercept here to skip subtrees
     *
	 */
	private void initializeEventBusHandler() {

		Handler<Message<JsonObject>> cnsEventHandler = new Handler<Message<JsonObject>>() {
			
			@Override
			public void handle(Message<JsonObject> message) {

                JsonObject messageBody = message.body();

                String processId = messageBody.getObject( "body" ).getString("processId");
                String sessionId = messageBody.getObject( "body" ).getString( "sessionId" );
                String token     = messageBody.getObject( "body" ).getString( "token" );

                String contentStructureKey = sessionId + processId;

                ContentStructure contentStructure = contentStructures.get( contentStructureKey );


                ContentNode contentToDeliver = null;
				switch (message.address()) {

                    /**
                     * a trigger was sent for a  first  Chapter
                     *  - read related process structure from json-file
                     *  - put sessionId -> ProcessStructure into scope map
                     *  - answer with first content
                     */
                    case Methods.START_LEARNING_OBJECT:

                        // read structure and put into scope
                        contentStructure = ContentStructureReader.getContentStructureFromFile( processId );
                        contentStructures.put( contentStructureKey, contentStructure );

                        // find first step
                        contentToDeliver = contentStructure.getFirstContentNode();

                        break;

                    /**
                     * a trigger was sent for "next" content
                     *  - answer with next content
                     */
                    case Methods.STEP_FORWARD:

                        contentStructure.setContentNodeAsDone( contentStructure.getCurrentNode() );
                        contentToDeliver = contentStructure.getNextContentNode();

                        break;

                    /**
                     * a trigger was sent for "back" content
                     *  - answer with last content
                     */
                    case Methods.STEP_BACKWARD:

                        contentStructure.stepBackToNode( contentStructure.getCurrentNode().getPreviousNode() );
                        contentToDeliver = contentStructure.getCurrentNode();

                        break;

                    /**
                     * a trigger was sent for "back to first step" content
                     *  - answer with first step content
                     */
                    case Methods.STEP_TO_FIRST_ITEM:

                        contentStructure.stepBackToFirstNod();
                        contentToDeliver = contentStructure.getCurrentNode();

                        break;

                    /**
                     * a trigger was sent for finished process
                     *  - remove from map
                     */
                    case Methods.FINISH_LEARNING_OBJECT:

                        contentStructures.remove(sessionId);

                        break;
                }
                /**
                 * only if there is a navigation through content structure
                 */
                if ( contentToDeliver != null ) {

                    /**
                     * contentToDeliver contains currentNode to Display,
                     * create chapter and send
                     */
                    IIDConnector conn = new IIDConnector(vertx.eventBus(),
                            IIDConnector.DEFAULT_ADDRESS);

                    LearningObjectBuilder lob = new LearningObjectBuilder();
                    lob.setTitle(contentToDeliver.getTitle());
                    ContentBody contentBody = new ContentBody.Package( contentToDeliver.getContentId() );
                    lob.addChapter(
                            new LearningObject.Chapter(contentToDeliver.getTitle(), contentBody));
                    conn.displayLearningObject(sessionId, "cns", lob.build(), null);
                }

			}
		};


		// Handlers are always registered for a specific address. 
        vertx.eventBus().registerHandler(Methods.START_LEARNING_OBJECT, cnsEventHandler);
        vertx.eventBus().registerHandler(Methods.STEP_FORWARD, cnsEventHandler);
        vertx.eventBus().registerHandler(Methods.STEP_BACKWARD, cnsEventHandler);
        vertx.eventBus().registerHandler(Methods.STEP_TO_FIRST_ITEM, cnsEventHandler);
        vertx.eventBus().registerHandler(Methods.FINISH_LEARNING_OBJECT, cnsEventHandler);
	}
	


}
