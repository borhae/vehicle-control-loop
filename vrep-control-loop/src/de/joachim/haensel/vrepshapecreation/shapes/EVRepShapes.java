package de.joachim.haensel.vrepshapecreation.shapes;


public enum EVRepShapes
{
    CUBOID(0), SPHERE(1), CYLINDER(2), CONE(3);
    
    private int value;
    
    private EVRepShapes(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
