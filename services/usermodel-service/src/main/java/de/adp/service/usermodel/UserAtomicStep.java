package de.adp.service.usermodel;

public class UserAtomicStep
{
    String id = "";

    String title = "";

    public UserAtomicStep()
    {

    }

    public UserAtomicStep(String id, String title)
    {
        this.id = id;
        this.title = title;
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
}
