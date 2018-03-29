package de.adp.service.usermodel;

import java.util.*;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class UserManager
{
    private static UserManager _instance = new UserManager();
    private JsonObject config;
    private Map<String, User> userList;
    private int nextUserId = 1;
    private EventBus eb = null;
    private List<String> loggedInUsers = null;

    // activate debug mode here
    private boolean debug = false;

    // eventbus prefix
    private final String eventbusPrefix = "adp:";
    
    private final String ontologyUri = "http://www.appsist.de/ontology/";

    public static UserManager getInstance()
    {
        return _instance;
    }

    private UserManager()
    {
        userList = new HashMap<String, User>();
        this.setLoggedInUsers(new ArrayList<String>());
        // addSomeUsers();
    }

    public User getUser(String userId)
    {
        if (this.userList.containsKey(userId)) {
            return this.userList.get(userId);
        }
        return null;
    }

    // create a new user
    public void createUser(String fn, String ln, String emp, String empType)
    {
        int uId = this.nextUserId++;
        String userId = fn.concat(ln) + "@example.com";

        User newUser = new User();
        newUser.setId(userId);
        newUser.setInternalId(uId);
        newUser.setFirstname(fn);
        newUser.setLastname(ln);
        newUser.setEmployer(emp);
        newUser.setEmployeenumber("MA" + uId);
        newUser.setDateofbirth(new Date());
        newUser.setCurrentWorkstate("Haupttaetigkeit");
        Set<String> wpg = new HashSet<String>();
        wpg.add(this.ontologyUri+"Produktionsanlagenbau");
        newUser.setWorkplaceGroups(wpg);
        Set<String> dg = new HashSet<String>();
        dg.add(this.ontologyUri+"Anlagenoperator");
        newUser.setDevelopementGoals(dg);
        newUser.addMeasureClearance(this.ontologyUri+"RezepturAnpassen");
        // newUser.setEmployeeType(User.EmployeeType.EMPLTYPE_AB);
        this.userList.put(userId, newUser);
        System.out.println("UserManager - created " + userId);

    }

    // create a new user
    public void createUser(Map<String, List<String>> userPropertiesMap)
    {
        // System.out.println("Received request to create new user with properties:"
        // + userPropertiesMap);

        int uId = this.nextUserId++;
        String userId = "user" + uId;
        User newUser = new User();
        newUser.setInternalId(uId);
        List<String> userClearances = new ArrayList<String>();
        List<String> userResponsibilities = new ArrayList<String>();
        for (String property : userPropertiesMap.keySet()) {
            String currentProperty = property.substring(property.lastIndexOf("/") + 1,
                    property.length());
            if (this.debug) {
                System.out.println(currentProperty);
            }
            switch (currentProperty) {
                case "istFreigeschaltetFuer" : {
                    userClearances.addAll(userPropertiesMap.get(property));
                    break;
                }
                case "istBeschaeftigtAls" : {
                    String et = "Anlagenbediener";
                    // TODO: use Ontology entries in User.EmployeeType
                    String currentEmployeeType = userPropertiesMap.get(property).get(0);
                    if (currentEmployeeType.indexOf("Instandhalter") != -1) {
                        newUser.setEmployeeType("Instandhalter");
                    }
                    if (currentEmployeeType.indexOf("Fuehrungskraft") != -1) {
                        newUser.setEmployeeType("Führungskraft");
                    }
                    if (currentEmployeeType.indexOf("Anlagenfuehrer") != -1) {
                        newUser.setEmployeeType("Anlagenführer");
                    }
                    newUser.setEmployeeType(et);
                    break;
                }
                case "verantwortlichFuer" : {
                    userResponsibilities.addAll(userPropertiesMap.get(property));
                    break;
                }
                case "rdf-schema#label" : {
                    List<String> fullNameList = userPropertiesMap.get(property);
                    String fullName = fullNameList.get(0);
                    String[] fullNameSplit = fullName.split(" ");
                    if (fullNameSplit.length > 1) {
                        newUser.setFirstname(fullNameSplit[0]);
                        newUser.setLastname(fullNameSplit[1]);
                        userId = fullNameSplit[0] + fullNameSplit[1];
                        newUser.setId(userId);
                    }
                }
                default :
            }

        }
        newUser.setMeasureClearance(userClearances);
        newUser.setResponsibleFor(userResponsibilities);
        System.out.println("UserManager - created " + userId);
        this.userList.put(userId, newUser);

    }

    // delete a user
    public void deleteUser(int userId)
    {
        this.userList.remove(userId);
    }

    // returns the next user id
    public int getNextUserId()
    {
        return this.nextUserId;
    }

    // returns the list of users
    public Map<String, User> getUserList()
    {
        return this.userList;
    }

    // get all employees stored in the semantic wiki
    // there is no differentiation between employers yet.
    private void loadEmployees()
    {
        if (this.debug) {
            System.out.println("loadEmployees: trying to load users from database");
        }

        String sparqlQueryForEmployees = "PREFIX app: <"+this.ontologyUri+"> "
                + " SELECT DISTINCT ?m WHERE {?subclass rdfs:subClassOf* app:Benutzer . ?m a ?subclass}";
        Handler<Message<String>> employeeHandler = new Handler<Message<String>>()
        {
            public void handle(Message<String> reply)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(reply.body());
                    List<JsonNode> measureList = root.findValues("value");
                    for (JsonNode jsn : measureList) {
                        getInformationFromWiki(jsn.toString());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };

        sendSparQLQuery(sparqlQueryForEmployees, employeeHandler);
    }

    // method retrieves information from the semantic wiki
    // takes URI (ontologyURI+EMPLOYEENAME) of an employee individual as
    // argument
    private void getInformationFromWiki(String employeeIndividual)
    {

        employeeIndividual = employeeIndividual.substring(employeeIndividual
.lastIndexOf("/") + 1,
                employeeIndividual.length() - 1);
        String informationAboutEmployee = "PREFIX app: <"+this.ontologyUri+">  select distinct ?property ?object where {app:"
                + employeeIndividual + " ?property ?object }";
        // System.out.println("User Manager - Query sent: " + informationAboutEmployee);
        Handler<Message<String>> infoHandler = new Handler<Message<String>>()
        {
            public void handle(Message<String> reply)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(reply.body());
                    Map<String, List<String>> userMap = new HashMap<String, List<String>>();
                    List<JsonNode> bindings = root.findValues("bindings");
                    for (JsonNode j : bindings) {
                        // List<JsonNode> jProperties = j;
                        Iterator<JsonNode> jIterator = j.elements();
                        while (jIterator.hasNext()) {
                            JsonNode jNode = jIterator.next();
                            String currentPropertyValue = jNode.get("property").get("value")
                                    .textValue();
                            String currentObjectValue = jNode.get("object").get("value")
                                    .textValue();
                            if (userMap.containsKey(currentPropertyValue)) {
                                List<String> propertyList = userMap.get(currentPropertyValue);
                                propertyList.add(currentObjectValue);
                                userMap.put(currentPropertyValue, propertyList);
                            }
                            else {
                                List<String> newPropertyList = new ArrayList<String>();
                                newPropertyList.add(currentObjectValue);
                                userMap.put(currentPropertyValue, newPropertyList);
                            }
                            // userMap.put(jNode.get("property").get("value").textValue(),
                            // jNode.get("object").get("value").textValue());
                        }
                        createUser(userMap);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };
        sendSparQLQuery(informationAboutEmployee, infoHandler);

    }

    public EventBus getEventBus()
    {
        return this.eb;
    }

    public void setEventBus(EventBus eventBus)
    {
        this.eb = eventBus;
    }

    public void init()
    {
        if (!config.getBoolean("test")) {
            // load employees from ontology

            loadEmployees();
        }
        else {
            // create employees
            // createUser("Bastian", "Lohnert", "Brabant & Lehnert", "Anlagenführer");
            // createUser("Friedrich", "Tolle", "Festo", "Anlagenführer");
            // createUser("Fritz", "Tonker", "Festo", "Anlagenbediener");
            // createUser("Matthias", "Baber", "MBB", "Anlagenführer");
            // createUser("alice.", "tester", "MBB", "Anlagenbediener");
            int uId = this.nextUserId++;
            String userId = "alice.tester@example.com";

            User newUser = new User();
            newUser.setId(userId);
            newUser.setInternalId(uId);
            newUser.setFirstname("Alice");
            newUser.setLastname("Tester");
            newUser.setEmployer("MBB");
            newUser.setEmployeeType("Anlagenoperator");
            newUser.setEmployeenumber("MA" + uId);
            newUser.setDateofbirth(new Date());
            newUser.setCurrentWorkstate("Haupttaetigkeit");
            Set<String> wpg = new HashSet<String>();
            wpg.add(this.ontologyUri+"Produktionsanlagenbau");
            newUser.setWorkplaceGroups(wpg);
            newUser.addMeasureClearance(this.ontologyUri+"RezepturAnpassen");
            // newUser.setEmployeeType(User.EmployeeType.EMPLTYPE_AB);
            this.userList.put(userId, newUser);
            System.out.println("UserManager - created " + userId);

            userId = "bob.tester@example.com";
            User newUser2 = new User();
            newUser2.setId(userId);
            newUser2.setInternalId(uId);
            newUser2.setFirstname("bob");
            newUser2.setLastname("Tester");
            newUser2.setEmployer("MBB");
            newUser2.setEmployeeType("Anlagenbediener");
            newUser2.setEmployeenumber("MA" + uId);
            newUser2.setDateofbirth(new Date());
            newUser2.setCurrentWorkstate("Haupttaetigkeit");
            newUser2.setWorkplaceGroups(wpg);
            newUser2.addMeasureClearance(this.ontologyUri+
                    "cd65fffa-2e99-4fbf-9b10-e347e9ca7344/PROCESS_1");
            // newUser.setEmployeeType(User.EmployeeType.EMPLTYPE_AB);
            Set<String> dg = new HashSet<String>();
            dg.add(this.ontologyUri+"Anlagenoperator");
            newUser2.setDevelopementGoals(dg);
            this.userList.put(userId, newUser2);
            System.out.println("UserManager - created " + userId);

        }

    }

    // common code to send sparql queries to the sparql-connector via the eventbus
    // first argument - sparql query
    // second argument - a handler taking care of the result
    private void sendSparQLQuery(String queryString, Handler<Message<String>> resultHandler)
    {
        JsonObject message = new JsonObject();
        JsonObject sQuery = new JsonObject();
        sQuery.putString("query", queryString);
        message.putObject("sparql", sQuery);

        if (null != this.eb) {
            this.eb.send(this.eventbusPrefix + "requests:semwiki", message, resultHandler);
        }
    }

    // methods to manage list of users authenticated to the system

    // get all users who are logged in currently
    public List<String> getLoggedInUsers()
    {
        return loggedInUsers;
    }

    // setter function for logged in users
    public void setLoggedInUsers(List<String> loggedInUsers)
    {
        this.loggedInUsers = loggedInUsers;
    }

    // add one user who logged in (only if not already in the list)
    public void addLoggedInUser(String loggedInUser)
    {
        if (!this.loggedInUsers.contains(loggedInUser)) {
            this.loggedInUsers.add(loggedInUser);
        }
    }

    // remove a user (only if logged in at all)
    public void removeLoggedInUser(String loggedInUser)
    {
        if (this.loggedInUsers.contains(loggedInUser)) {
            this.loggedInUsers.remove(loggedInUser);
        }
    }

    // set configuration
    public void setConfiguration(JsonObject conf)
    {
        this.config = conf;
    }
}
