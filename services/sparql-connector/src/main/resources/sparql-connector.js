var vertx   = require('vertx');
var eb      = require('vertx/event_bus');
var console = require('vertx/console');
var container =  require('vertx/container');
var config = container.config;
var eventbusPrefix = "adp:";
load('sparql.js');

// add eventbus handler


var semWikiRequestHandler = function(jsonMessage, replyHandler){
	// extract SparQL Query from message
	var sparqlQuery = jsonMessage.sparql.query;
	// TODO: execute query
	sparqlRequest(sparqlQuery, replyHandler);
	//console.log('request for semantic wiki information arrived:\n'+ jsonMessage);
}

eb.registerHandler(eventbusPrefix+"requests:semwiki", semWikiRequestHandler, function() {
	console.log("semWikiRequestHandler registered in the cluster");
	//console.log("testing sparql-connector");
	//sparqlRequestTest();
});

function sparqlRequest(sparqlQuery, replyHandler) {
	var request = new SPARQL();
	//console.log("Executing Query:");
	//console.log(sparqlQuery);
	
	request.executeQuery(sparqlQuery, function(queryResult, info){
		//console.log(queryResult); //Json by default
		// return query result
		replyHandler(queryResult);
	} );
}


function sparqlRequestTest(handler) {

	var request = new SPARQL();
	// example for building a SparQL query using JavaScript
	/*
	request
		.variable("?m")
		.prefixe("app", "http://www.appsist.de/ontology/")
		.where("?helperClass", "rdfs:subClassOf*", "app:Maschinencd zustand" )
		.where("app:LoctiteLeer","a","?helperClass")
		.where("?helperClass", "rdfs:subClassOf*", "?supClassH")
		.where("?supClassH", "rdfs:subClassOf*", "?supClass")
		.where("?supClassInst", "a", "?supClass")
		.where("?supClassInst", "app:bedingt", "?m")
		.groupBy("?m")
		.groupBy("?supClass")
		.orderBy("(count(?supClassH)-1)");
	request.execute(function(data, info){
		console.log(data); //Json by default
		console.log(info); //Print hello world
		eb.publish(handler, data);

	});*/
	var queryString = "PREFIX app: <http://www.appsist.de/ontology/> SELECT ?m WHERE {?helperClass rdfs:subClassOf* app:Maschinenzustand . app:LoctiteLeer a ?helperClass . ?helperClass rdfs:subClassOf* ?supClassH . ?supClassH rdfs:subClassOf* ?supClass . ?supClassInst a ?supClass . ?supClassInst app:bedingt ?m } GROUP BY ?m ?supClass ORDER BY (count(?supClassH)-1)";
	console.log("Sending sparql query: "+queryString);
	request.executeQuery(queryString, function(data, info){
		console.log(data); //Json by default
	} );

}
