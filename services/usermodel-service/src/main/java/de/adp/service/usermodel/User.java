package de.adp.service.usermodel;

import java.util.*;

public class User
{

    public enum CompetencyLevel
    {
        // competency levels
        COMPLVLLOW("Kenner"), COMPLVLMEDIUM("Könner"),
 COMPLVLHIGH("Experte"), COMPLVLUNKNOWN(
                "Nicht spezifiziert");
        
        public String fullname = "";
        
        CompetencyLevel(String val) {
            this.fullname = val;
        }

    }

    public enum EmployeeType {
        // different types of employees
        EMPLTYPE_AB("Anlagenbediener"), EMPLTYPE_AF("Anlagenführer"), EMPLTYPE_FK("Führungskraft"), EMPLTYPE_IH(
                "Instandhalter");

        public String fullname = "";

        EmployeeType(String val)
        {
            this.fullname = val;
        }
    }

    // =================================================================
    // static user fields
    //
    // this section contains user fields which never change
    // or in rare occasions only
    //
    // =================================================================

    // user id
    public String id;

    private int internalId;

    // users first name
    private String firstname = "";

    // users middle name
    private String middlename = "";

    // users last name
    private String lastname = "";

    // users date of birth
    private Date dateofbirth = null;

    // users employer
    private String employer = "";

    // employeenumber (structure given by employer)
    private String employeenumber = "";

    // users mother tongue
    private String mothertongue = "";

    // users current level of training
    private String traininglevel = "";

    // specifies the type of the employee
    // see enum type definition at the top of class for possible values
    private String employeetype = null;

    // =================================================================
    // dynamic user fields
    //
    // this section contains user fields which never change often
    //
    // =================================================================

    // map of competencylevels for measures Map<String, String>
    // String#1 denotes measureId
    // String#2 one of (COMPLVLLOW, COMPLVLMEDIUM, COMPLVLHIGH)
    private Map<String, CompetencyLevel> measuresCompLvl;

    // map of competencylevels for activities Map<String, String>
    // String#1 denotes activityId
    // String#2 one of (COMPLVLLOW, COMPLVLMEDIUM, COMPLVLHIGH)
    private Map<String, CompetencyLevel> activitiesCompLvl;

    // map of competencylevels for actions Map<String, String>
    // String#1 denotes measureId
    // String#2 one of (COMPLVLLOW, COMPLVLMEDIUM, COMPLVLHIGH)
    private Map<String, CompetencyLevel> actionsCompLvl;

    // map of competencylevels for atomic steps Map<String, String>
    // String#1 denotes measureId
    // String#2 one of (COMPLVLLOW, COMPLVLMEDIUM, COMPLVLHIGH)
    private Map<String, CompetencyLevel> atomicStepsCompLvl;

    // viewed content map Map<String, Integer>
    // String denotes contentId
    // Integer is the number of views
    private Map<String, Integer> numContentViewed;

    // number of times a measure has been performed by the user
    // String denotes measureId
    // Integer is the number of times measure has been performed
    private Map<String, Integer> numMeasuresPerformed;

    // number of times an activity has been performed by the user
    // String denotes measureId
    // Integer is the number of times measure has been performed
    private Map<String, Integer> numActivitiesPerformed;

    // number of times an action has been performed by the user
    // String denotes measureId
    // Integer is the number of times measure has been performed
    private Map<String, Integer> numActionsPerformed;

    // number of times an activity has been performed by the user
    // String denotes measureId
    // Integer is the number of times measure has been performed
    private Map<String, Integer> numAtomicStepsPerformed;

    // list of measures user is allowed to perform
    private List<String> measureClearance;

    // list of measures user is allowed to perform with assistance
    private List<String> measureClearancesWithAssistance;
    // list of "Stationen" for which user is responsible
    private List<String> responsibleFor;

    // Workplacegroups user is assigned to
    private Set<String> workplaceGroups;

    // Developmentgoals which are set for user
    // consists of Occupationgroups and objects in production
    private Set<String> developmentGoals;

    // current workstate
    // one of (Haupttätigkeit/Nebentätigkeit)
    private String currentWorkstate;

    public User()
    {
        // default constructor

        this.activitiesCompLvl = new HashMap<String, CompetencyLevel>();
        this.measuresCompLvl = new HashMap<String, CompetencyLevel>();
        this.actionsCompLvl = new HashMap<String, CompetencyLevel>();
        this.atomicStepsCompLvl = new HashMap<String, CompetencyLevel>();
        this.numContentViewed = new HashMap<String, Integer>();
        this.numActionsPerformed = new HashMap<String, Integer>();
        this.numActivitiesPerformed = new HashMap<String, Integer>();
        this.numMeasuresPerformed = new HashMap<String, Integer>();
        this.numAtomicStepsPerformed = new HashMap<String, Integer>();
        this.setMeasureClearance(new ArrayList<String>());
        this.setMeasureClearancesWithAssistances(new ArrayList<String>());
        this.setResponsibleFor(new ArrayList<String>());
        this.setWorkplaceGroups(new HashSet<String>());
        this.setDevelopementGoals(new HashSet<String>());

    }

    // getter/setter methods for static user fields

    // returns (internal) user id

    public String getId()
    {
        return id;
    }

    // set user id
    public void setId(String id)
    {
        this.id = id;
    }

    // returns users firstname
    public String getFirstname()
    {
        return firstname;
    }

    // set users firstname
    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    // returns users middlename
    public String getMiddlename()
    {
        return middlename;
    }

    // set users middlename
    public void setMiddlename(String middlename)
    {
        this.middlename = middlename;
    }

    // returns users lastname
    public String getLastname()
    {
        return lastname;
    }

    // set users lastname
    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public String getFullName()
    {
        String temp = this.getFirstname() + " " + this.getLastname();
        return temp;
    }

    // returns date of birth
    public Date getDateofbirth()
    {
        return dateofbirth;
    }

    // set date of birth
    public void setDateofbirth(Date dateofbirth)
    {
        this.dateofbirth = dateofbirth;
    }

    // returns name of employer
    public String getEmployer()
    {
        return employer;
    }

    // set employer
    public void setEmployer(String employer)
    {
        this.employer = employer;
    }

    // returns employee type
    public String getEmployeeType()
    {
        return this.employeetype;
    }

    // set employee type
    public void setEmployeeType(String employeetype)
    {
        this.employeetype = employeetype;
    }

    // returns employeenumber
    public String getEmployeenumber()
    {
        return employeenumber;
    }

    // set employee number
    public void setEmployeenumber(String employeenumber)
    {
        this.employeenumber = employeenumber;
    }

    // returns the current traininglevel of user
    public String getTraininglevel()
    {
        return traininglevel;
    }

    // set the current traininglevel of user
    public void setTraininglevel(String traininglevel)
    {
        this.traininglevel = traininglevel;
    }

    // returns mother tongue
    public String getMothertongue()
    {
        return mothertongue;
    }

    // set mother tongue
    public void setMothertongue(String mothertongue)
    {
        this.mothertongue = mothertongue;
    }

    // methods section for dynamic fields
    // ==================
    // ACTIONS SECTION
    // ==================

    // returns CompetencyLevel for Actions
    public String getActionCompLvl(String actionId)
    {
        if (this.actionsCompLvl.containsKey(actionId)) {
            return this.actionsCompLvl.get(actionId).fullname;
        }
        return CompetencyLevel.COMPLVLUNKNOWN.fullname;
    }

    // set CompetencyLevel for Actions
    public void setActionCompLvl(String actionId, CompetencyLevel lvl)
    {
        this.actionsCompLvl.put(actionId, lvl);
    }

    // returns CompetencyLevel for all Actions

    public Map<String, CompetencyLevel> getActionsCompLvls()
    {
        return actionsCompLvl;
    }

    // returns the number of performances for the specified action or -1 if we have don't have
    // one
    public int getNumActionPerformed(String actionId)
    {
        if (this.numActionsPerformed.containsKey(actionId)) {
            return numActionsPerformed.get(actionId);
        }
        return -1;
    }

    // increases number of times the specified activity has been performed by the user
    public void actionPerformed(String actionId)
    {
        int numActionPerformed = this.getNumActionPerformed(actionId);
        if (numActionPerformed < 0) {
            numActionPerformed = 0;
        }

        this.numActionsPerformed.put(actionId, ++numActionPerformed);
    }

    // list actions performed by user
    public String listActionsPerformed()
    {
        MeasureManager mm = MeasureManager.getInstance();
        String resultHTML = "<table>";
        if (this.numActionsPerformed.keySet().size() > 0) {
            resultHTML += "<tr><th>ID der Aktion</th><th>Titel der Aktion</th><th>Anzahl Ausführungen</th></tr>";
        }
        for (String str : this.numActionsPerformed.keySet()) {
            // UserAction ua = mm.ge
            resultHTML += "<tr><td>" + str + "</td><td>" + this.numActionsPerformed.get(str)
                    + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;
    }



    // ===================
    // ACTIVITIES SECTION
    // ===================

    // returns map with all levels for all activities
    public Map<String, CompetencyLevel> getActivitiesCompLvls()
    {
        return activitiesCompLvl;
    }

    // return competencylevel for specified activity
    public String getActivityCompLvl(String activityId)
    {
        if (this.activitiesCompLvl.containsKey(activityId)) {
            return this.activitiesCompLvl.get(activityId).fullname;
        }
        return CompetencyLevel.COMPLVLUNKNOWN.fullname;
    }

    // set competencylevel for specified activity
    public void setActivityCompLvl(String activityId, CompetencyLevel lvl)
    {
        this.activitiesCompLvl.put(activityId, lvl);
    }

    // returns the number of performances for the specified activity or -1 if we have don't
    // have
    // one
    public int getNumActivityPerformed(String activityId)
    {
        if (this.numActivitiesPerformed.containsKey(activityId)) {
            return numActivitiesPerformed.get(activityId);
        }
        return -1;
    }

    // increases number of times the specified activity has been performed by the user
    public void activityPerformed(String activityId)
    {
        int numActivityPerformed = this.getNumActivityPerformed(activityId);
        if (numActivityPerformed < 0) {
            numActivityPerformed = 0;
        }

        this.numActivitiesPerformed.put(activityId, ++numActivityPerformed);
    }

    // list activities performed by user
    public String listActivitiesPerformed()
    {
        String resultHTML = "<table>";
        if (this.numActivitiesPerformed.keySet().size() > 0) {
            resultHTML += "<tr><th>ID der Aktivität</th><th>Titel der Aktivität</th><th>Anzahl Ausführungen</th></tr>";
        }
        for (String str : this.numActivitiesPerformed.keySet()) {
            resultHTML += "<tr><td>" + str + "</td><td>" + this.numActivitiesPerformed.get(str)
                    + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;
    }

    // =====================
    // ATOMIC STEPS SECTION
    // ===================

    // returns CompetencyLevel for Atomic Step
    public String getAtomicStepCompLvl(String atomicStepId)
    {
        if (this.atomicStepsCompLvl.containsKey(atomicStepId)) {
            return this.atomicStepsCompLvl.get(atomicStepId).fullname;
        }
        return CompetencyLevel.COMPLVLUNKNOWN.fullname;
    }

    // set CompetencyLevel for Actions
    public void setAtomicStepCompLvl(String atomicStepId, CompetencyLevel lvl)
    {
        this.atomicStepsCompLvl.put(atomicStepId, lvl);
    }

    // returns CompetencyLevel for all Actions

    public Map<String, CompetencyLevel> getAtomicStepsCompLvls()
    {
        return atomicStepsCompLvl;
    }

    // returns the number of performances for the specified atomic step or -1 if we have don't
    // have one
    public int getNumAtomicStepPerformed(String atomicStepId)
    {
        if (this.numActionsPerformed.containsKey(atomicStepId)) {
            return numActionsPerformed.get(atomicStepId);
        }
        return -1;
    }

    // increases number of times the specified atomic step has been performed by the user
    public void atomicStepPerformed(String atomicStepId)
    {
        int numAtomicStepPerformed = this.getNumAtomicStepPerformed(atomicStepId);
        if (numAtomicStepPerformed < 0) {
            numAtomicStepPerformed = 0;
        }

        this.numAtomicStepsPerformed.put(atomicStepId, ++numAtomicStepPerformed);
    }

    // list atomic steps performed by user
    public String listAtomicStepsPerformed()
    {
        String resultHTML = "<table>";
        if (this.numAtomicStepsPerformed.keySet().size() > 0) {
            resultHTML += "<tr><th>ID des elementaren Schrittes</th><th>Titel des elementaren Schrittes</th><th>Anzahl Ausführungen</th></tr>";
        }
        for (String str : this.numAtomicStepsPerformed.keySet()) {
            resultHTML += "<tr><td>" + str + "</td><td>" + this.numAtomicStepsPerformed.get(str)
                    + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;
    }

    // ========================
    // CONTENT RELATED SECTION
    // ========================

    // returns map with amount of views for all content items
    public Map<String, Integer> getContentViewed()
    {
        return numContentViewed;
    }

    // set amount of views for multiple content items
    public void setContentViewed(Map<String, Integer> contentViewed)
    {
        this.numContentViewed = contentViewed;
    }

    // returns number of views for specified content item or -1 if we have a new item
    public int getNumContentViews(String contentItemId)
    {
        if (this.numContentViewed.containsKey(contentItemId)) {
            return numContentViewed.get(contentItemId);
        }
        return -1;
    }

    // increases number of views for specified content item
    public void contentViewed(String contentItemId)
    {
        int numContentView = this.getNumContentViews(contentItemId);
        if (numContentView < 0) {
            numContentView = 0;
        }

        this.numContentViewed.put(contentItemId, ++numContentView);
    }

    // list contents seen by this user
    public String listContentsSeen()
    {
        String resultHTML = "<table>";
        for (String str : this.numContentViewed.keySet()) {
            resultHTML += "<tr><td>" + str + "</td><td>" + this.numContentViewed.get(str)
                    + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;

    }

    // ==================
    // MEASURES SECTION
    // ==================

    // return map with competencylevels for all measures
    public Map<String, CompetencyLevel> getMeasuresCompLvls()
    {
        return measuresCompLvl;
    }

    // return competencylevel for specified measure
    public String getMeasureCompLvl(String measureId)
    {
        if (this.measuresCompLvl.containsKey(measureId)) {
            return this.measuresCompLvl.get(measureId).fullname;
        }
        return CompetencyLevel.COMPLVLUNKNOWN.fullname;
    }

    // set competencylevel for specified measure
    public void setMeasureCompLvl(String measure, CompetencyLevel lvl)
    {
        this.measuresCompLvl.put(measure, lvl);
    }

    // returns number of performances for the specified measure or -1 if we have don't have one
    public int getNumMeasurePerformed(String measureId)
    {
        if (this.numMeasuresPerformed.containsKey(measureId)) {
            return numMeasuresPerformed.get(measureId);
        }
        return -1;
    }

    // increases number of times a measure has been performed by the user
    public void measurePerformed(String measureId)
    {
        int numMeasurePerformed = this.getNumMeasurePerformed(measureId);
        if (numMeasurePerformed < 0) {
            numMeasurePerformed = 0;
        }

        this.numMeasuresPerformed.put(measureId, ++numMeasurePerformed);
    }

    // list Measures performed by user
    public String listMeasuresPerformed()
    {
        MeasureManager mm = MeasureManager.getInstance();
        String resultHTML = "<table>";
        if (this.numMeasuresPerformed.keySet().size() > 0) {
            resultHTML += "<tr><th>ID der Maßnahme</th><th>Titel der Maßnahme</th><th>Anzahl Ausführungen</th></tr>";
        }

        for (String str : this.numMeasuresPerformed.keySet()) {
            Measure m = mm.getMeasure(str);

            resultHTML += "<tr><td>" + str + "</td><td>" + m.getTitle() + "</td><td>"
                    + this.numMeasuresPerformed.get(str)
                    + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;
    }

    // =======================================
    // MEASURE CLEARANCES (FREIGABEN) SECTION
    // =======================================

    // list measure clearances for user
    public String listMeasureClearances()
    {
        String resultHTML = "<table>";
        for (String str : this.measureClearance) {
            // resultHTML += "<tr><td>" + str + "</td><td>" + str + "</td></tr>";
            resultHTML += "<tr><td>" + str + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;

    }

    // returns list containing all measure IDs the user is allowed to perform
    public List<String> getMeasureClearance()
    {
        return measureClearance;
    }

    // set list of allowed measure IDs
    public void setMeasureClearance(List<String> measureClearance)
    {
        this.measureClearance = measureClearance;
    }


    // add a new allowed measure here
    public void addMeasureClearance(String measureId)
    {
        // TODO: perform sanity check for measureId here
        if (true) {
            this.measureClearance.add(measureId);
        }

    }

    // ======================================================
    // MEASURE CLEARANCES WITH ASSISTANCE (FREIGABEN) SECTION
    // ======================================================

    // list measure clearances for user
    public String listMeasureClearancesWithAssistance()
    {
        String resultHTML = "<table>";
        for (String str : this.measureClearancesWithAssistance) {
            // resultHTML += "<tr><td>" + str + "</td><td>" + str + "</td></tr>";
            resultHTML += "<tr><td>" + str + "</td></tr>";
        }
        resultHTML += "</table>";
        return resultHTML;

    }

    // returns list containing all measure IDs the user is allowed to perform
    public List<String> getMeasureClearancesWithAssistance()
    {
        return this.measureClearancesWithAssistance;
    }

    // set list of allowed measure IDs
    public void setMeasureClearancesWithAssistances(List<String> measureClearance)
    {
        this.measureClearancesWithAssistance = measureClearance;
    }

    // add a new allowed measure here
    public void addMeasureClearanceWithAssistance(String measureId)
    {
        // TODO: perform sanity check for measureId here
        if (true) {
            this.measureClearancesWithAssistance.add(measureId);
        }

    }

    // return list of stations
    public List<String> getResponsibleFor()
    {
        return responsibleFor;
    }

    // set stations user is responsibleFor
    public void setResponsibleFor(List<String> responsibleFor)
    {
        this.responsibleFor = responsibleFor;
    }

    ///////////////////////////////////////////////////////////
    // Arbeitsplatzgruppen (WorkplaceGroup section)

    // return list of workplacegroups users belongs to
    public Set<String> getWorkplaceGroups()
    {
        return this.workplaceGroups;
    }

    // set list of workplacegroups
    public void setWorkplaceGroups(Set<String> userWorkplaceGroups)
    {
        this.workplaceGroups = userWorkplaceGroups;
    }

    // add one workplacegroup
    public void addWorkplaceGroup(String newWorkplaceGroup)
    {
        this.workplaceGroups.add(newWorkplaceGroup);
    }

    //
    /////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////
    // Entwicklungsziele (DevlopementGoals section)

    // return list of developmentgoals users belongs to
    public String getCurrentWorkstate()
    {
        return this.currentWorkstate;
    }

    // add one workplacegroup
    public void setCurrentWorkstate(String userCurrentWorkstate)
    {
        this.currentWorkstate = userCurrentWorkstate;
    }

    //
    /////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////
    // Entwicklungsziele (DevlopementGoals section)

    // return list of developmentgoals users belongs to
    public Set<String> getDevelopmentGoals()
    {
        return this.developmentGoals;
    }

    // set list of developmentgoals
    public void setDevelopementGoals(Set<String> userDevelopementGoals)
    {
        this.developmentGoals = userDevelopementGoals;
    }

    // add one workplacegroup
    public void addDevelopmentGoals(String newDevelopmentGoal)
    {
        this.workplaceGroups.add(newDevelopmentGoal);
    }

    //
    /////////////////////////////////////////////////////////////

    public int getInternalId()
    {
        return internalId;
    }

    public void setInternalId(int internalId)
    {
        this.setId("User " + internalId);
        this.internalId = internalId;
    }

    public ArrayList<String> filterMeasures(ArrayList<String> measureListToFilter)
    {
        ArrayList<String> resultList = new ArrayList<String>();
        System.out.println("User - " + this.measureClearance);
        for (String measureId : measureListToFilter) {
            System.out.println("User.java - " + measureId);
            if (this.measureClearance.contains(measureId)
                    || this.measureClearancesWithAssistance.contains(measureId)) {
                System.out.println("new measure allowed:" + measureId);
                resultList.add(measureId);
            }
        }
        return resultList;
    }
}
