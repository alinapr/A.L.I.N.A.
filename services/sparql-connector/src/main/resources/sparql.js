/**
*
* This class help to get content by using a SPARQL endpoint
*
* @author Damien Legrand  < http://damienlegrand.com >
* @author Hans-Helge Bürger < http://hanshelgebuerger.com >
*
*/

// make sure module configuration is available

function isConfigAvailable(){
	if (undefined != config) {
		var configString = JSON.stringify(config);
		configString = configString.replace(/\s/g,"");
		if (configString != "{}") return true;
	}
	return false;
}



// if locally installed information workbench should be used 
// uncomment the following setConnectorDefaultConfiguration
function setConnectorDefaultConfiguration(){
	config = {
		webserver: {
			port: 7081,
	    	basePath: "/services/sparqlconnector"
		},
		sparql: {
			reqBaseUrl: "localhost",
			reqPath: "/sparql",
			reqBasePort: 8443
		}
	}
}


// if public available information workbench should be used 
// uncomment the following setConnectorDefaultConfiguration
//function setConnectorDefaultConfiguration(){
//	config = {
//		webserver: {
//			port: 7081,
//	    	basePath: "/services/usermodel"
//		},
//		sparql: {
//			reqBaseUrl: "iwb.appsist.de",
//			reqPath: "/sparql",
//			reqBasePort: 443
//		}
//	}
//}

function SPARQL()
{
	/**
	*
	* ATTRIBUTES
	*
	**/
	
	if (!isConfigAvailable()) setConnectorDefaultConfiguration();

	//The endpoint base URL without parameters
	//Ommit 'http://' or other protocol names
	this.baseUrl 		= config.sparql.reqBaseUrl;
	
	//The optional path to the endpoint
	this.path 			= config.sparql.reqPath;
	
	// The endpoint base URL Port
	this.basePort		= undefined;
	if (config.sparql.reqBasePort != undefined){
		this.basePort 		= config.sparql.reqBasePort;
	}
	
	//The result's type
	this.format 		= "json";

	//The Accept Header for SPARQL Request
	this.accept			= "application/sparql-results+" + this.format;

	//GET or POST
	this.method 		= "GET";
	this.values			= "";

	//If you need to attach some information (will be return to the callback)
	this.info 			= "";

	//The SPARQL request
	this.sparql 		= null;

	//The Request URL with path and query
	this.requestUrl		= null;

	//Arrays to build the request
	this.prefixes 		= [];
	this.distinctSelect = false;
	this.variables 		= [];
	this.wheres 		= [];
	this.group			= [];
	this.orders			= [];
	this.limitNb		= null;
	this.offsetNb		= null;
	this.unions 		= []; //array of SPARQL object


	/**
	*
	* METHODS
	*
	**/

	this.prefixe 		= function(ns, x) 	{ this.prefixes.push("PREFIX " + ns + ": <" + x + ">"); return this; };
	this.distinct		= function(bool)	{ this.distinctSelect = bool; return this;};
	this.variable 		= function(x) 		{ this.variables.push(x); return this; };
	this.where 			= function(x, y, z) { this.wheres.push(x + " " + y + " " + z); return this; };
	this.optionalWhere 	= function(x, y, z) { this.wheres.push("OPTIONAL {" + x + " " + y + " " + z + "}"); return this; };
	this.groupBy 		= function (x) 		{ this.group.push(x); return this; };
	this.union 			= function(x) 		{ this.unions.push(x); return this; };
	this.filter 		= function(x) 		{ this.wheres.push("FILTER ( " + x + " )"); return this; };
	this.orderBy 		= function(x) 		{ this.orders.push(x); return this; };
	this.limit 			= function(x) 		{ this.limitNb = x; return this; };
	this.offset 		= function(x) 		{ this.offestNb = x; return this; };
	this.setInfo		= function(x) 		{ this.info = x; return this; };

	this.build 		= function() {
		var sp = "";
		var first = false;

		//PREFIXES
		for(var i = 0; i < this.prefixes.length; i++)
		{
			sp += this.prefixes[i] + "\n";
		}

		//VARIABLES
		sp += "SELECT ";

		if(this.distinctSelect) sp += "DISTINCT ";
		if(this.variables.length > 0)
		{
						
			first = true;
			for(var i = 0; i < this.variables.length; i++)
			{	
				if(first) {first = false;}
				else if(i < this.variables.length) {sp += ", ";}
				sp += this.variables[i];
			}
		}
		else sp += "*";

		//WHERES
		sp += "\nWHERE";

		if(this.unions.length > 0) sp += "\n{";

		var w = this.buildWhere();

		sp += w;

		//UNIONS
		first = true;
		for(i = 0; i < this.unions.length; i++)
		{
			var u = this.unions[i].buildWhere();

			if(u !== "")
			{
				if(first)
				{
					first = false;
					if(w !== "") sp += "UNION";
				}else sp += "UNION";

				sp += u;
			}
		}

		if(this.unions.length > 0) sp += "\n}\n";
		
		 //GROUP BY
		if (this.group.length > 0) sp += "GROUP BY " + this.group.join(" ");
		
		//ORDER BY
		if(this.orders.length > 0)
		{	
		sp += "\nORDER BY ";
			first = true;
			for(i = 0; i < this.orders.length; i++)
			{
				if(first) first = false;
				else if(i < this.orders.length) sp += ", ";
				sp += this.orders[i];
			}
		}

		//LIMIT
		if(this.limitNb !== null) sp += "LIMIT " + this.limitNb + "\n";

		//OFFSET
		if(this.offsetNb !== null) sp += "OFFSET " + this.offsetNb + "\n";
		//console.log("SparQL Query:"+sp);
		return sp;
	};

	this.buildWhere = function() {
		var sp = "";
		if(this.wheres.length === 0) return sp;

		sp += "\n{\n";

		for(var i = 0; i < this.wheres.length; i++)
		{
			sp += this.wheres[i];
			if(i < this.wheres.length - 1) sp += " .";

			sp += "\n";
		}

		sp += "}\n";

		return sp;
	};

	this.buildRequestUrl = function() {
		var url = encodeURI(
			this.path +
			"?query=" + this.sparql
			);
		return url;
	};

	this.execute = function(callback) {

		var cur = this;

		if(this.sparql     === null) this.sparql     = this.build();
		if(this.requestUrl === null) this.requestUrl = this.buildRequestUrl();

		var client = vertx.createHttpClient()
			.host(this.baseUrl)
			.ssl(true)
			.trustAll(true)
			.keepAlive(false);
		if (this.basePort != undefined) {
			client.port(this.basePort);
		}
		
		var sparql_request = client.request(
			this.method,
			this.requestUrl,
			function(resp) {
				var response = resp.bodyHandler(function(buffer) {
					callback(buffer.toString(), cur.info);
				});
			}
		);

		if (this.accept !== null)
			sparql_request.putHeader("Accept", this.accept);

		sparql_request.end();

	};
	
	this.executeQuery = function(query, callback) {
		var cur = this;
		if(this.requestUrl === null) {
			
			this.requestUrl = encodeURI(this.path +"?query="+query);
		}
		var client = vertx.createHttpClient()
			.host(this.baseUrl)
			.ssl(true)
			.trustAll(true)
			.keepAlive(false);
		if (this.basePort != undefined) {
			client.port(this.basePort);
		}
		var sparql_request = client.request(
			this.method,
			this.requestUrl,
			function(resp) {
				var response = resp.bodyHandler(function(buffer) {
					callback(buffer.toString(), cur.info);
				});
			}
		);

		if (this.accept !== null)
			sparql_request.putHeader("Accept", this.accept);

		sparql_request.end();

	};

}
