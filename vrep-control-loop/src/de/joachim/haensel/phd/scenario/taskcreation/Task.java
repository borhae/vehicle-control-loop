package de.joachim.haensel.phd.scenario.taskcreation;

public class Task
{
    private double _x;
    
    public Task(ITaskCreatorConfig config)
    {
        //TODO uncomment and handle error
//        if(config.isRandomSoureAndTarget())
//        {
//            
//        }
    }

    public double getX()
    {
        return _x;
    }

    public void setX(double x)
    {
        _x = x;
    }

    @Override
    public boolean equals(Object obj)
    {
        //TODO work on the fields, once we have them
        if(obj instanceof Task)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
