package de.joachim.haensel.vrepshapecreation.joints;

import coppelia.remoteApi;


public enum EVRepJointTypes
{
    REVOLUTE(remoteApi.sim_joint_revolute_subtype),
    PRISMATIC(remoteApi.sim_joint_prismatic_subtype),
    SPHERICALE(remoteApi.sim_joint_spherical_subtype);

    private int _value;

    private EVRepJointTypes(int value)
    {
        _value = value;
    }

    public int getValue()
    {
        return _value;
    }
}
