package de.adp.service.usermodel;

import java.util.*;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
public class UserModelService
    extends Verticle
{
    private JsonObject config;
    private String basePath = "";
    private final String eventbusPrefix = "adp:";

    // logger object
    private Logger log = null;

    // Eventbus
    private EventBus eb;

    private final Map<String, JsonObject> sessionJsonObjectMap = new HashMap<String, JsonObject>();
    public void start()
    {
        this.eb = vertx.eventBus();
        this.log = container.logger();
        if (container.config() != null && container.config().size() > 0) {
            config = container.config();

        }
        else {
            // container.logger().warn("Warning: No configuration applied! Using default settings.");
            config = getDefaultConfiguration();
        }

        this.basePath = config.getObject("webserver").getString("basePath");

        setupHandlers();
        setupHttpHandlers();
        // show user details
        vertx.setTimer(2000, new Handler<Long>()
        {

            @Override
            public void handle(Long arg0)
            {
                container.logger().info("Initializing UserManager");
                UserManager um = UserManager.getInstance();
                if (null == um.getEventBus()) {
                    um.setEventBus(eb);
                }
                um.setConfiguration(config);
                um.init();
            }

        });

        container.logger().info("Usermodel Service started");
    }
    
    /**
     * Create a configuration which used if no configuration is passed to the module.
     * 
     * @return Configuration object.
     */
    private static JsonObject getDefaultConfiguration()
    {
        JsonObject defaultConfig = new JsonObject();
        JsonObject webserverConfig = new JsonObject();
        webserverConfig.putNumber("port", 7080);
        webserverConfig.putString("statics",
                "/Users/midi01/Work/vertx_workspace/usermodel-service/src/main/resources");
        webserverConfig.putString("basePath", "/services/usermodel");
        defaultConfig.putObject("webserver", webserverConfig);
        defaultConfig.putBoolean("test", true);
        return defaultConfig;

    }

    private void setupHandlers()
    {
        Handler<Message<JsonObject>> usermodelServiceHandler = new Handler<Message<JsonObject>>(){

            @Override
            public void handle(Message<JsonObject> message)
            {
                JsonObject messageBody = message.body();
                UserManager um = UserManager.getInstance();
                String userId = messageBody.getString("userId");
                // log.info("userModelService/setupHandlers - userId " + userId);
                User user = um.getUser(userId);
                String sessionId = messageBody.getString("sid");
                String token = messageBody.getString("token");
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                if (null != user) {
                    switch (message.address()) {
                        case "adp:event:processEvent:contentSeen" :
                            String contentId = messageBody.getString("contentId");
                            container.logger()
                                    .info("user " + userId + " has seen content: " + contentId);
                                user.contentViewed(contentId);
                            break;
                        case "adp:service:usermodel#mastersProcess" :
                            if (user.getNumMeasurePerformed(
                                    messageBody.getString("processId")) > 10) {
                                messageBody.putBoolean("mastered", true);
                            }
                            else {
                                messageBody.putBoolean("mastered", false);
                            }
                            message.reply(messageBody);
                            break;
                        case "adp:service:usermodel#allowedMeasures" :
                            // log.info("clearanceHandle - " + messageBody.encodePrettily());
                            JsonArray jsonArray = messageBody.getObject("applicableMeasures")
                                    .getObject("results").getField("bindings");
                            ArrayList<String> possibleMeasures = new ArrayList<String>();
                            for (int i = 0; i != jsonArray.size(); i++) {
                                if (jsonArray.get(i) instanceof JsonObject) {
                                    JsonObject nn = (JsonObject) jsonArray.get(i);
                                    possibleMeasures.add(((JsonObject) nn.getField("massnahme"))
                                            .getString("value"));
                                }
                            }
                            // log.info(possibleMeasures.size());
                            ArrayList<String> filteredMeasuresList = UserManager.getInstance()
                                    .getUser(userId).filterMeasures(possibleMeasures);
                            // log.info(filteredMeasuresList.size());
                            messageBody.removeField("applicableMeasures");
                            // send ArrayList possibleMeasures to check
                            try {
                                messageBody.putString("applicableMeasures",
                                        ow.writeValueAsString(filteredMeasuresList));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            message.reply(messageBody);
                            break;

                        case "adp:service:usermodel#getUserInformation" :
                            // attach user information to calling message and return
                            JsonObject result = new JsonObject();
                            result.putString("currentWorkstate", user.getCurrentWorkstate());
                            try{
                                result.putString("workplaceGroups",
                                        ow.writeValueAsString(user.getWorkplaceGroups()));
                                result.putString("developmentGoals",
                                        ow.writeValueAsString(user.getDevelopmentGoals()));
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                            messageBody.putObject("userInformation", result);

                            message.reply(messageBody);
                            break;
                    }
                }
                else {
                    message.reply(new JsonObject());
                }

            }

        };


        // Handlers are always registered for a specific address.
        this.eb.registerHandler(this.eventbusPrefix+"event:processEvent:contentSeen",
 usermodelServiceHandler);
        this.eb.registerHandler(this.eventbusPrefix + "service:usermodel#mastersProcess",
                usermodelServiceHandler);
        this.eb.registerHandler(this.eventbusPrefix + "service:usermodel#allowedMeasures",
                usermodelServiceHandler);
        this.eb.registerHandler(this.eventbusPrefix + "service:usermodel#getUserInformation",
                usermodelServiceHandler);
    }
    

    private void setupHttpHandlers()
    {
        BasePathRouteMatcher rm = new BasePathRouteMatcher(this.basePath);

        // list all users
        rm.get("/users", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                UserManager um = UserManager.getInstance();
                Map<String, User> userMap = um.getUserList();
                if (null == um.getEventBus()) {
                    um.setEventBus(eb);
                }
                if (userMap.isEmpty()) {
                    um.init();
                    container.logger().info(
                            "UserModelService - UserManager eventbus Object: " + um.getEventBus());

                    userMap = um.getUserList();
                }
                String ulString = "";
                /*for (int i : userMap.keySet()) {
                    User user = userMap.get(i);

                    if (null != user) {
                        ulString += "ID: " + user.getId() + ": " + user.getFirstname() + " "
                                + user.getLastname() + " | " + user.getEmployer() + "<br/>";
                    }
                }*/
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ulString = mapper.writeValueAsString(userMap);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                req.response().end("<html><body>" + ulString + "</body></html>");
            }
        });

        // list all activities
        rm.get("/users/:uid/activities", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listActivitiesPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // list all performed actions
        rm.get("/users/:uid/actions", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listActionsPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // list all performed actions
        rm.get("/users/:uid/clearances", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listMeasureClearances() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // list all performed atomic steps
        rm.get("/users/:uid/atomicsteps", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listAtomicStepsPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // list all performed measures
        rm.get("/users/:uid/measures", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listMeasuresPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // increase number of measure performed
        rm.get("/users/:uid/measures/:mid", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                String mId = req.params().get("mid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    user.measurePerformed(mId);
                    req.response().end(
                            "<html><body> Measure " + mId + " was performed</body></html>");
                }
                else {
                    req.response().end("<html><body></body></html>");
                }
            }
        });

        // list all performed measures
        rm.get("/users/:uid/activities", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listActivitiesPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // increase number of measure performed
        rm.get("/users/:uid/activities/:aid", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                String activityId = req.params().get("aid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    user.activityPerformed(activityId);
                    req.response().end(
                            "<html><body> Activity " + activityId + " was performed</body></html>");
                }
                else {
                    req.response().end("<html><body></body></html>");
                }
            }
        });

        // list all performed actions
        rm.get("/users/:uid/actions", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listActionsPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // increase number of measure performed
        rm.get("/users/:uid/actions/:aid", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                String actionId = req.params().get("aid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    user.actionPerformed(actionId);
                    req.response().end(
                            "<html><body> Action " + actionId + " was performed</body></html>");
                }
                else {
                    req.response().end("<html><body></body></html>");
                }
            }
        });

        // list all performed measures
        rm.get("/users/:uid/atomicsteps", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.listAtomicStepsPerformed() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // increase number of measure performed
        rm.put("/users/:uid/atomicsteps/:aid", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                String asId = req.params().get("aid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    user.atomicStepPerformed(asId);
                    req.response().end(
                            "<html><body> Atomic Step " + asId + " was performed</body></html>");
                }
                else {
                    req.response().end("<html><body></body></html>");
                }
            }
        });

        // list all performed measures
        rm.get("/users/:uid/contents", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    req.response().end("<html><body>" + user.listContentsSeen() + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        // show user details
        rm.get("/users/:uid", new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                if (null != user) {
                    /*
                     * String cvStr = user.listContentsSeen(); req.response().end(
                     * "<html><body>" + "ID: " + userId + ": " + user.getFirstname() + " " +
                     * user.getLastname() + " | " + user.getEmployer() + "<br/>" +
                     * user.listMeasuresPerformed() + user.listActivitiesPerformed() +
                     * user.listActionsPerformed() + user.listAtomicStepsPerformed() + cvStr +
                     * "</body></html>");
                     */
                    try {
                        req.response().end(new ObjectMapper().writeValueAsString(user));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    req.response().end("{}");
                }
            }
        });

        // list all performed measures
        rm.get("/users/:uid/activities/:activityId",
                new Handler<HttpServerRequest>()
        {
            public void handle(HttpServerRequest req)
            {
                        String userId = req.params().get("uid");
                UserManager um = UserManager.getInstance();
                User user = um.getUser(userId);
                String activityId = req.params().get("activityId");
                
                if (null != user) {
                    req.response().end(
                            "<html><body>" + user.getNumActivityPerformed(activityId)
                                    + "</body></html>");
                }
                else {
                    req.response().end("");
                }
            }
        });

        vertx.createHttpServer().requestHandler(rm)
                .listen(config.getObject("webserver").getInteger("port"));
    }
    
    public UserModelService()
    {
        //
    }


}
