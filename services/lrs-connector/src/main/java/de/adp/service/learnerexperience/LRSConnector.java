package de.adp.service.learnerexperience;

import java.io.IOException;
import java.net.*;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.json.StringOfJSON;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import com.rusticisoftware.tincan.lrsresponses.StatementsResultLRSResponse;
import com.rusticisoftware.tincan.v10x.StatementsQuery;


public class LRSConnector
    extends Verticle
{
    // JsonObject representing the configuration of this verticle
    private JsonObject config;
    // routematcher to match http queries
    private BasePathRouteMatcher routeMatcher = null;
    // verticle logger
    private Logger log = null;

    // remote learning record store
    private RemoteLRS lrs = null;
   
    // basePath String
    private String basePath = "";
    
    // ontology prefix
    private final String ontologyPrefix = "http://www.appsist.de/ontology/";

    // eventbus prefix
    private final String eventbusPrefix = "adp:";
    @Override
  public void start() {
        // init logger
        log = container.logger();

        // ensure verticle is configured
        if (container.config() != null && container.config().size() > 0) {
            config = container.config();
        }
        else {
            // container.logger().warn("Warning: No configuration applied! Using default settings.");
            config = getDefaultConfiguration();
        }

        initializeLearningRecordStoreConnection();

        this.basePath = config.getObject("webserver").getString("basePath");
        // init SparQL prefix string


        initializeHttpRequestHandlers();
        initializeEventbusHandlers();
        log.info("*******************");
        log.info("  LRSConnector auf Port "
                + config.getObject("webserver").getNumber("port") + " gestartet ");
        log.info("                              *******************");
  }


    private void initializeHttpRequestHandlers()
    {
        // init routematcher with basePath from configuration
        routeMatcher = new BasePathRouteMatcher(this.basePath);
        // set handlers here
        routeMatcher.get("/callTest", new Handler<HttpServerRequest>()
        {
            @Override
            public void handle(final HttpServerRequest request)
            {
                log.info("Test method called");
                testLRSStorage();
                request.response().end("done");
            }
        });
        
        routeMatcher.post("/statements", new Handler<HttpServerRequest> ()
        {
            @Override
            public void handle(final HttpServerRequest request)
            {
                try {
                    // log.info(request.params().get("statement"));
                    Statement st = new Statement(
                            new StringOfJSON(request.params().get("statement")));
                    StatementLRSResponse lrsRes = lrs.saveStatement(st);
                    if (lrsRes.getSuccess()) {
                        // success, use lrsRes.getContent() to get the statement back
                        log.info(lrsRes.getContent());
                    }
                    else {
                        // failure, error information is available in lrsRes.getErrMsg()
                        log.info(lrsRes);
                        log.info(lrsRes.getErrMsg());
                    }

                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        // start verticle webserver at configured port
        vertx.createHttpServer().requestHandler(routeMatcher)
                .listen(config.getObject("webserver").getInteger("port"));

    }

    private void initializeEventbusHandlers()
    {

        Handler<Message<JsonObject>> storeStatementHandler = new Handler<Message<JsonObject>>()
        {
            public void handle(Message<JsonObject> message)
            {
                // retrieve statement from json object
                String statementString = message.body().getString("statement");
                // store statement
                storeLearningExperience(statementString);
            }
        };
        vertx.eventBus().registerHandler(this.eventbusPrefix + "query:lrs#storeStatement",
                storeStatementHandler);

        Handler<Message<JsonObject>> buildAndStoreStatementHandler = new Handler<Message<JsonObject>>()
        {
            public void handle(Message<JsonObject> message)
            {
                // retrieve statement from json object
                String agent = message.body().getString("agent");
                String verb = message.body().getString("verb");
                String object = message.body().getString("activityId");
                // store statement
                storeLearningExperience(agent, verb, object);
            }
        };

        vertx.eventBus().registerHandler(this.eventbusPrefix + "query:lrs#buildAndStoreStatement",
                buildAndStoreStatementHandler);

        Handler<Message<JsonObject>> postQueryHandler = new Handler<Message<JsonObject>>()
        {
            public void handle(Message<JsonObject> message)
            {
                // retrieve query from json object
                String agent = message.body().getString("agent");
                String verb = message.body().getString("verb");
                String object = message.body().getString("activityId");
                // get results
                // return results
            }
        };
        vertx.eventBus().registerHandler(this.eventbusPrefix + "query:lrs#postQuery",
                postQueryHandler);

    }

    private void initializeLearningRecordStoreConnection()
    {
        try {
            this.lrs = new RemoteLRS();
            JsonObject lrsConfig = config.getObject("lrs");
            this.lrs.setEndpoint(lrsConfig.getString("endpoint"));
            this.lrs.setVersion(TCAPIVersion.V100);
            this.lrs.setUsername(lrsConfig.getString("username"));
            this.lrs.setPassword(lrsConfig.getString("password"));
            log.info("connected to LRS");

        }
        catch (MalformedURLException e) {
            // tell service gateway service is not available
            // shutdown service
            log.info("MalformedURLException");
        }

    }
  

    /**
     * Create a configuration which is used if no configuration is passed to the module.
     * 
     * @return Configuration object.
     */
    private static JsonObject getDefaultConfiguration()
    {
        JsonObject defaultConfig = new JsonObject();
        JsonObject webserverConfig = new JsonObject();
        webserverConfig.putNumber("port", 7086);
        webserverConfig.putString("basePath", "/services/lrsconnect");
        // TODO: test statics with relative path
        // until now only full path is working

        defaultConfig.putObject("webserver", webserverConfig);

        JsonObject lrsConfig = new JsonObject();
        lrsConfig.putString("endpoint", "http://localhost:1234/data/xAPI");
        lrsConfig.putString("username", "f76aaf09f21be1f9e8bb1f634bc5b86db4a0603a");
        lrsConfig.putString("password", "7dc2379ee04bb6b064c98217aaff222c99aca798");
        defaultConfig.putObject("lrs", lrsConfig);
        return defaultConfig;
    }



    private void storeLearningExperience(String statement)
    {
        try {
            Statement st = new Statement(new StringOfJSON(statement));
            StatementLRSResponse lrsRes = lrs.saveStatement(st);
            if (lrsRes.getSuccess()) {
                // success, use lrsRes.getContent() to get the statement back
            }
            else {
                // failure, error information is available in lrsRes.getErrMsg()
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void storeLearningExperience(String actor, String verb, String activityId)
    {
        if (null == actor || null == verb || null == activityId) {
            // invalid statement
        }

        if (actor.equals("") || verb.equals("") || activityId.equals("")) {
            // invalid statement
        }

        Agent stAgent = null;
        Verb stVerb = null;
        Activity stActivity = null;
        // preparations for statement
        // build mbox string

        String mBoxString = "mailto:" + actor + "@adp.de";
        LanguageMap verbDisplays = new LanguageMap();
        String currentVerb = verb.substring(verb.lastIndexOf("/") + 1);
        verbDisplays.put("en-US", currentVerb);

        try {
            stAgent = new Agent();

            stAgent.setMbox(mBoxString);
            // stAgent.setName("Employee");
            stVerb = new Verb(verb);
            stVerb.setDisplay(verbDisplays);
            stActivity = new Activity();
            stActivity.setId(activityId);
        }
        catch (Exception e) {
            e.printStackTrace();
            // store failed statement parameters
        }
        Statement st = new Statement(stAgent, stVerb, stActivity);
        StatementLRSResponse lrsRes = this.lrs.saveStatement(st);
        if (lrsRes.getSuccess()) {
            // success, use lrsRes.getContent() to get the statement back
            log.info("Statement stored in LRS: " + lrsRes.getContent());
        }
        else {
            // failure, error information is available in lrsRes.getErrMsg()
            log.info("Error: " + lrsRes.getErrMsg());
        }
    }

    private StatementsResult queryLearningExperience(String actor, String verb, String activityId)
    {
        if (null == actor || null == verb) {
            // invalid statement
        }

        if (actor.equals("") || verb.equals("")) {
            // invalid statement
        }
        Agent stAgent = null;
        Verb stVerb = null;
        URI activityURI = null;

        // preparations for statement
        // build mbox string

        String mBoxString = "mailto:" + actor + "@adp.de";
        LanguageMap verbDisplays = new LanguageMap();
        String currentVerb = verb.substring(verb.lastIndexOf("/"));
        verbDisplays.put("en-US", currentVerb);
        try {
            stAgent = new Agent();
            stAgent.setMbox(mBoxString);
            stVerb = new Verb(verb);
            if (null != activityId && !activityId.equals("")) {
                activityURI = new URI(activityId);
            }

            StatementsQuery sq = new StatementsQuery();
            sq.setAgent(stAgent);
            sq.setVerbID(stVerb);
            if (null != activityURI) {
                sq.setActivityID(activityURI);
            }

            StatementsResultLRSResponse lrsRes = lrs.queryStatements(sq);

            if (lrsRes.getSuccess()) {
                // success, use lrsRes.getContent() to get the statement back
                log.info(lrsRes.getContent().getStatements().size());
                return lrsRes.getContent();
            }
            else {
                // failure, error information is available in lrsRes.getErrMsg()
                log.info("Error: " + lrsRes.getErrMsg());
                return new StatementsResult();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return new StatementsResult();
            // store failed statement parameters
        }
    }

    private void testLRSStorage()
    {
        String actor = "adp17789";
        String verb = "http://adlnet.gov/expapi/verbs/experienced";
        String activityId = this.ontologyPrefix+"MassnahmeFuerFettWechseln";
        queryLearningExperience(actor, verb, activityId);
        // storeLearningExperience(actor, verb, activityId);
    }
}
