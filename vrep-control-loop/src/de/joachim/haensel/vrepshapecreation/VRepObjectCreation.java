package de.joachim.haensel.vrepshapecreation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.joints.JointParameters;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class VRepObjectCreation
{
    private static final String SCRIPT_LOADING_LUA_FUNCTION = "loadCode";
    private static final String LOADED_SCRIPT_OBJECT_CREATION = "./src/main/lua/VRepObjectCreation.lua";
    private static final String SPRING_DAMPER_SCRIPT_FILE_NAME = "./src/main/lua/TestSpringDamperControlScript.lua";
    private static final String STEERING_CONTROL_SCRIPT_FILE_NAME = "./src/main/lua/SteeringControlScript.lua";
    
    
    private static final String VREP_LOADING_SCRIPT_PARENT_OBJECT = "ScriptLoader";
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private String _springDamperScript;
    private String _steeringScript;

    public VRepObjectCreation(VRepRemoteAPI vrep, int clientID)
    {
        _vrep = vrep;
        _clientID = clientID;
        loadFunctions(vrep, clientID);
    }

    /**
     * 
     * Creates a primitive object in vrep.
     * 
     * @param name object name
     * @param x x-position
     * @param y y-position
     * @param z z-position
     * @param width object width
     * @param height object height
     * @param depth object depth
     * @param angleAlpha euler angle alpha
     * @param angleBeta euler angle beta
     * @param angleGamma euler angle gamma
     * @param mass object mass
     * @param type type of object
     * @param isDynamic 
     * @param isRespondable 
     * @return returns the vrep object handle
     * @throws VRepException
     */
    public int createPrimitive(ShapeParameters params) throws VRepException
    {
        IntWA callParamsI = params.getInts();
        FloatWA callParamsF = params.getFloats();
        StringWA callParamsS = params.getStrings();
        IntWA returnInt = new IntWA(1);
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createPrimitive", callParamsI, callParamsF, callParamsS, null, returnInt, null, null, null, remoteApi.simx_opmode_blocking);
        return returnInt.getArray()[0];
    }


    public int createJoint(JointParameters params) throws VRepException
    {
        IntWA returnInt = new IntWA(1);
        FloatWA inFloats = params.getFloats();
        IntWA inInts = params.getInts();
        StringWA inStrings = params.getStrings();
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createJoint", inInts, inFloats, inStrings, null, returnInt, null, null, null, remoteApi.simx_opmode_blocking);
        int jointHandle = returnInt.getArray()[0];
        if(params.isMotorEnabled())
        {
            setIntParameter(jointHandle, remoteApi.sim_jointintparam_motor_enabled, 1);
        }
        if(params.isCtrlLoopEnabled())
        {
            setIntParameter(jointHandle, remoteApi.sim_jointintparam_ctrl_enabled, 1);
        }
        if(params.isInSpringDamperMode())
        {
            setFloatParameter(jointHandle, remoteApi.sim_jointfloatparam_kc_k, params.getSpringConstantK());
            setFloatParameter(jointHandle, remoteApi.sim_jointfloatparam_kc_c, params.getDampingCoefficientC());
            attachSpringDamperScript(jointHandle);
        }
        return jointHandle;
    }

    public void setParentForChild(int parent, int child, boolean keepInPlace) throws VRepException
    {
        IntWA callParamsI = new IntWA(3);
        callParamsI.getArray()[0] = parent;
        callParamsI.getArray()[1] = child;
        callParamsI.getArray()[2] = keepInPlace ? 1 : 0;

        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "setParentForChild", callParamsI, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void setIntParameter(int objectID, int paramID, int value) throws VRepException
    {
        IntWA callParamsI = new IntWA(3);
        int[] paramArray = callParamsI.getArray();
        paramArray[0] = objectID;
        paramArray[1] = paramID;
        paramArray[2] = value;

        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "setIntParameter", callParamsI, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void setFloatParameter(int objectID, int paramID, double value) throws VRepException
    {
        IntWA callParamsI = new IntWA(2);
        int[] paramArray = callParamsI.getArray();
        paramArray[0] = objectID;
        paramArray[1] = paramID;
        
        FloatWA callParamsF = new FloatWA(1);
        callParamsF.getArray()[0] = (float)value;

        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "setFloatParameter", callParamsI, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }
    
    public void attachControlScript(int physicalBodyHandle) throws VRepException
    {
        if(_steeringScript == null)
        {
            _steeringScript = loadScript(STEERING_CONTROL_SCRIPT_FILE_NAME);
        }
        IntWA callParamsI = new IntWA(2);
        int[] paramArray = callParamsI.getArray();
        paramArray[0] = physicalBodyHandle;
        paramArray[1] = remoteApi.sim_scripttype_childscript;
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = _steeringScript;
        int scriptType = remoteApi.sim_scripttype_customizationscript;
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, scriptType, "addAndAttachScript", callParamsI, null, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    /**
     * Needs to be reworked. Don't use for now, an update of vrep might make this irrelevant
     * @param springDamperHandle
     * @throws VRepException
     */
    public void attachSpringDamperScript(int springDamperHandle) throws VRepException
    {
        IntWA callParamsI = new IntWA(2);
        int[] paramArray = callParamsI.getArray();
        paramArray[0] = springDamperHandle;
//        paramArray[1] = remoteApi.sim_scripttype_jointctrlcallback & remoteApi.sim_scripttype_childscript;
        paramArray[1] = remoteApi.sim_scripttype_childscript;
        if(_springDamperScript == null)
        {
            _springDamperScript = loadScript(SPRING_DAMPER_SCRIPT_FILE_NAME);
        }
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = _springDamperScript;
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "addAndAttachScript", callParamsI, null, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    private String loadScript(String scriptsFileName)
    {
        String result = null;
        try
        {
            result = new String(Files.readAllBytes(Paths.get(scriptsFileName)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private void loadFunctions(VRepRemoteAPI vrep, int clientID)
    {
        StringWA inParamsString = new StringWA(1);
        String scriptText = null;
        try
        {
            try
            {
                scriptText = new String(Files.readAllBytes(Paths.get(LOADED_SCRIPT_OBJECT_CREATION)));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
            inParamsString.getArray()[0] = scriptText;
            StringWA returnStrings = new StringWA(1);
            vrep.simxCallScriptFunction(clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, SCRIPT_LOADING_LUA_FUNCTION, null, null, inParamsString, null, null, null, returnStrings, null, remoteApi.simx_opmode_blocking);
            if (returnStrings.getArray().length >= 1)
            {
                String loadReturnValue = returnStrings.getArray()[0];
                System.out.println("script handle: " + loadReturnValue);
            }
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }
}
