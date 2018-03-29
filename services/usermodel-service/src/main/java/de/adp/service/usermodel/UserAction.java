package de.adp.service.usermodel;

import java.util.ArrayList;
import java.util.List;

public class UserAction
{
    String id = "";

    String title = "";

    List<UserAtomicStep> actionAtomicSteps = null;

    public UserAction()
    {
        this.actionAtomicSteps = new ArrayList<UserAtomicStep>();
    }

    public UserAction(String id, String title)
    {
        this.id = id;
        this.title = title;
        this.actionAtomicSteps = new ArrayList<UserAtomicStep>();
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

    public List<UserAtomicStep> getActionAtomicSteps()
    {
        return actionAtomicSteps;
    }

    public void setActionAtomicSteps(List<UserAtomicStep> actionAtomicSteps)
    {
        this.actionAtomicSteps = actionAtomicSteps;
    }
}
