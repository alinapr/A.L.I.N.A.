package de.adp.service.usermodel;

import java.util.ArrayList;
import java.util.List;

public class Measure
{
    String id = "";

    String title = "";

    List<UserActivity> measureUserActivities = null;

    public Measure()
    {
        this.measureUserActivities = new ArrayList<UserActivity>();
    }

    public Measure(String id, String title)
    {
        this.id = id;
        this.title = title;
        this.measureUserActivities = new ArrayList<UserActivity>();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<UserActivity> getMeasureUserActivities()
    {
        return measureUserActivities;
    }

    public void setMeasureUserActivities(List<UserActivity> measureUserActivities)
    {
        this.measureUserActivities = measureUserActivities;
    }
}
