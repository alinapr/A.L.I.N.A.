package de.adp.service.usermodel;

import java.util.*;

public class MeasureManager
{
    private static MeasureManager _instance = new MeasureManager();
    private Map<String, Measure> measureList;
    private int nextMeasureId = 1;

    public static MeasureManager getInstance()
    {
        return _instance;
    }

    private MeasureManager()
    {
        measureList = new HashMap<String, Measure>();
        addSomeMeasures();
    }

    public Measure getMeasure(String measureId)
    {
        if (this.measureList.containsKey(measureId)) {
            return this.measureList.get(measureId);
        }
        return null;
    }

    // create a new user
    public void createMeasure(String mId, String mTitle)
    {
        int uId = this.nextMeasureId++;
        Measure newMeasure = new Measure(mId, mTitle);

        ArrayList<UserActivity> activitiesList = new ArrayList<UserActivity>();
        UserActivity activityOne = new UserActivity("ACTY13", "Tür öffnen");
        ArrayList<UserAction> actionsList = new ArrayList<UserAction>();
        actionsList.add(new UserAction("ACTN1", "Station auf Handschaltung umstellen"));
        actionsList.add(new UserAction("ACTN2", "Türfreigabe anfordern"));
        actionsList.add(new UserAction("ACTN3", "Tür öffnen"));
        activityOne.setActivityUserActions(actionsList);
        UserActivity activityTwo = new UserActivity("ACTY17", "Loctite Behälter wechseln");
        UserActivity activityThree = new UserActivity("ACTY25", "Flasche tauschen");
        UserActivity activityFour = new UserActivity("ACTY48", "Tür schließen");
        activitiesList.add(activityOne);
        activitiesList.add(activityTwo);
        activitiesList.add(activityThree);
        activitiesList.add(activityFour);

        newMeasure.setMeasureUserActivities(activitiesList);

        this.measureList.put(mId, newMeasure);
    }

    // delete a user
    public void deleteUser(int userId)
    {
        this.measureList.remove(userId);
    }

    // returns the next user id
    public int getNextUserId()
    {
        return this.nextMeasureId;
    }

    public Map<String, Measure> getMeasureList()
    {
        return this.measureList;
    }
    // create some users to play with
    private void addSomeMeasures()
    {
        createMeasure("M91", "Loctite Flasche wechseln");

        System.out.println(this.nextMeasureId + " measures created");
    }


}
