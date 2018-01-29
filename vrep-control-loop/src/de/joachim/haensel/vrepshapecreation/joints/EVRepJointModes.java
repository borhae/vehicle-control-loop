package de.joachim.haensel.vrepshapecreation.joints;

import coppelia.remoteApi;

public enum EVRepJointModes
{
    PASSIVE(remoteApi.sim_jointmode_passive),
    MOTION(remoteApi.sim_jointmode_motion),
    IK(remoteApi.sim_jointmode_ik),
    IK_DEPENDENT(remoteApi.sim_jointmode_ikdependent),
    DEPENDENT(remoteApi.sim_jointmode_dependent),
    FORCE(remoteApi.sim_jointmode_force);
    
    private int _value;

    private EVRepJointModes(int value)
    {
        _value = value;
    }

    public int getValue()
    {
        return _value;
    }
}
