package de.adp.service.ihs;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class IHSMainVerticle
    extends Verticle
{

    // JsonObject representing the configuration of this verticle
    private JsonObject config;
    // routematcher to match http queries
    private BasePathRouteMatcher routeMatcher = null;
    // verticle logger
    private Logger log = null;

    // needed prefix string to build SparQL queries
    private String prefixString = "";


    // basePath String
    private String basePath = "";

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

        this.basePath = config.getObject("webserver").getString("basePath");
        // init SparQL prefix string

        setPrefixString("PREFIX app: <http://www.appsist.de/ontology/> PREFIX terms: <http://purl.org/dc/terms/>");

        initializeHttpRequestHandlers();

        log.info("*******************");
        log.info("  Inhalteselektor auf Port "
                + config.getObject("webserver").getNumber("port") + " gestartet ");
        log.info("                              *******************");

  }


    private void initializeHttpRequestHandlers()
    {
        // init routematcher with basePath from configuration
        routeMatcher = new BasePathRouteMatcher(this.basePath);
        // set handlers here

        final String staticFileDirectory = config.getObject("webserver").getString("statics");
        /*
         * The following rules are applied in the order given during the initialization. The
         * first rule which matches the request is applied and the latter rules are ignored.
         */

        routeMatcher.get("/content", new Handler<HttpServerRequest>()
        {
            @Override
            public void handle(final HttpServerRequest request)
            {
                loadTaskContentsForUser(request);
            }
        });
        

        /*
         * This entry serves files from a directory specified in the configuration. In the
         * default configuration, the files are served from "src/main/resources/www", which is
         * packaged with the module.
         */
        routeMatcher.getWithRegEx("/.*", new Handler<HttpServerRequest>()
        {

            @Override
            public void handle(HttpServerRequest request)
            {
                container.logger().info(staticFileDirectory + request.path());
                request.response().sendFile(staticFileDirectory + request.path());
            }
        });

        // start verticle webserver at configured port
        vertx.createHttpServer().requestHandler(routeMatcher)
                .listen(config.getObject("webserver").getInteger("port"));

    }

    // for one user get list with all cleared measures
    private void loadTaskContentsForUser(final HttpServerRequest request)
    {
        JsonObject message = new JsonObject();
        final String taskId = request.params().get("elementId");
        final String userId = request.params().get("userId");

        String sparqlQueryForContents = this.prefixString
                + " SELECT DISTINCT ?inhalt WHERE { { ?id app:hatProzessId '" + taskId
                + "'. ?inhalt app:informiertUeber ?id }" + " UNION {?inhalt app:informiertUeber '"
                + taskId + "' } }";
        JsonObject sQuery = new JsonObject();
        sQuery.putString("query", sparqlQueryForContents);
        message.putObject("sparql", sQuery);
        vertx.eventBus().send(this.eventbusPrefix + "requests:semwiki", message,
                new Handler<Message<String>>()
                {
                    public void handle(Message<String> reply)
                    {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(reply.body());
                            if (null != root) {
                                List<String> valueList = root.findValuesAsText("value");
                                if (null != valueList && !valueList.isEmpty()) {
                                    JsonObject cId = new JsonObject();

                                    String resultString = valueList.get(0);
                                    resultString = resultString.substring(resultString
                                            .lastIndexOf("/") + 1);
                                    cId.putString("contentId", resultString);
                                    request.response().end(cId.encode());
                                }
                                else {
                                    request.response().end(new JsonObject().encode());
                                }
                            }
                            else {
                                request.response().end(new JsonObject().encode());
                            }
                            


                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                });

    }

    private void setPrefixString(String prefix)
    {
        // check for valid prefix needed here
        this.prefixString = prefix;
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
        webserverConfig.putString("basePath", "/services/ihs");
        // TODO: test statics with relative path
        // until now only full path is working

        webserverConfig.putString("statics",
                "/www");

        defaultConfig.putObject("webserver", webserverConfig);

        JsonObject sparqlConfig = new JsonObject();
        sparqlConfig.putString("reqBaseUrl", "localhost");
        sparqlConfig.putString("reqPath", "/sparql");
        sparqlConfig.putNumber("reqBasePort", 8443);
        sparqlConfig.putString("ontologyPrefix", "app:");
        sparqlConfig.putString("ontologyUri", "http://www.appsist.de/ontology/");
        defaultConfig.putObject("sparql", sparqlConfig);
        return defaultConfig;
    }

}
