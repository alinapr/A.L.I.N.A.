package de.adp.service.usermodel;

import java.util.ArrayList;
import java.util.List;

public class UserActivity
{
    String id = "";

    String title = "";

    List<UserAction> activityUserActions = null;

    public UserActivity()
    {
        this.activityUserActions = new ArrayList<UserAction>();
    }

    public UserActivity(String id, String title)
    {
        this.id = id;
        this.title = title;
        this.activityUserActions = new ArrayList<UserAction>();
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

    public List<UserAction> getActivityUserActions()
    {
        return activityUserActions;
    }

    public void setActivityUserActions(List<UserAction> activityUserActions)
    {
        this.activityUserActions = activityUserActions;
    }
}
