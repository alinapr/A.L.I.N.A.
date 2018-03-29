package de.adp.service.measuresservice;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class BasicSparQLQueries
{
    // needed prefix string to build SparQL queries
    private static final String PREFIXSTRING = "PREFIX app: <http://www.appsist.de/ontology/>";

    // Eventbus address
    private static final String SPARQLREQUESTS = "adp:requests:semwiki";

    public static void getMeasuresForStates(String states, EventBus eb,
            Handler<Message<String>> stringHandler)
    {
        String sparqlQuery = PREFIXSTRING + "SELECT DISTINCT ?massnahme ?zustand "
                + "WHERE { VALUES ?zustand " + states + " ?zustand app:bedingt ?massnahme .}";
        sendSparQLQuery(sparqlQuery, eb, stringHandler);
    }

    public static void getStationsInWorkplaceGroups(String workplaceGroups, EventBus eb,
            Handler<Message<String>> stringHandler)
    {
        String sparqlQuery = PREFIXSTRING + "SELECT DISTINCT ?station " + "WHERE { VALUES ?apg "
                + workplaceGroups + " ?anlage app:isPartOf ?apg ;" + " app:hatPrioritaet ?p ."
                + " ?station app:isPartOf ?anlage .}";
        sendSparQLQuery(sparqlQuery, eb, stringHandler);
    }

    public static void getLocalStates(String stationStates, EventBus eb,
            Handler<Message<String>> stringHandler)
    {
        String sparqlQuery = PREFIXSTRING + "SELECT DISTINCT ?z ?station ?zt ?p  "
                + "WHERE { VALUES (?z ?station) " + stationStates
                + " ?z rdfs:subClassOf* ?zt . ?zt app:hatPrioritaet ?p}" + "  ORDER BY DESC(?p)";
        sendSparQLQuery(sparqlQuery, eb, stringHandler);
    }

    public static void getLabelFor(String ontologyConcepts, String lang, EventBus eb,
            Handler<Message<String>> stringHandler)
    {
        String sparqlQuery = PREFIXSTRING + "SELECT DISTINCT ?oc ?label " + "WHERE { VALUES ?oc "
                + ontologyConcepts + " ?oc rdfs:label ?label FILTER(LANGMATCHES(LANG(?label), \""
                + lang + "\")) }";
        sendSparQLQuery(sparqlQuery, eb, stringHandler);
    }

    private static void sendSparQLQuery(String sparQLQuery, EventBus eb,
            Handler<Message<String>> stringHandler)
    {
        JsonObject sQuery = new JsonObject().putString("query", sparQLQuery);
        eb.send(SPARQLREQUESTS, new JsonObject().putObject("sparql", sQuery),
                stringHandler);
    }
}
