package de.joachim.haensel.vrepshapecreation;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.vrepshapecreation.dummy.DummyParameters;
import de.joachim.haensel.vrepshapecreation.joints.JointParameters;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class VRepObjectCreation
{
    private static final String SCRIPT_LOADING_LUA_FUNCTION = "loadCode";
    private static final String OBJECT_CREATION_SCRIPT_FILE_NAME = "./lua/VRepObjectCreation.lua";
    private static final String SPRING_DAMPER_SCRIPT_FILE_NAME = "./lua/SpringDamperControlScript.lua";
    private static final String STEERING_CONTROL_SCRIPT_FILE_NAME = "./lua/SteeringControlScript.lua";
    private static final String STEERING_VISUALIZATION_SCRIPT_FILE_NAME = "./lua/SteeringVisualizationScript.lua";
    
    
    public static final String VREP_LOADING_SCRIPT_PARENT_OBJECT = "ScriptLoader";
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private String _springDamperScript;
    private String _steeringScript;
    private String _visualizationScript;

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


    public int createDummy(DummyParameters params) throws VRepException
    {
        IntWA callParamsI = params.getInts();
        FloatWA callParamsF = params.getFloats();
        StringWA callParamsS = params.getStrings();
        IntWA returnInt = new IntWA(1);
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDummy", callParamsI, callParamsF, callParamsS, null, returnInt, null, null, null, remoteApi.simx_opmode_blocking);
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
    

    public void attachVisualizationScript(int dummyHandle) throws VRepException
    {
        if(_visualizationScript == null)
        {
            _visualizationScript = loadScript(STEERING_VISUALIZATION_SCRIPT_FILE_NAME);
        }
        IntWA callParamsI = new IntWA(2);
        int[] paramArray = callParamsI.getArray();
        paramArray[0] = dummyHandle;
        paramArray[1] = remoteApi.sim_scripttype_childscript;
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = _visualizationScript;
        int scriptType = remoteApi.sim_scripttype_customizationscript;
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, scriptType, "addAndAttachScriptNonThreaded", callParamsI, null, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
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
                scriptText = new String(Files.readAllBytes(Paths.get(OBJECT_CREATION_SCRIPT_FILE_NAME)));
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

    public void createMapCenter() throws VRepException
    {
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", 6, "createCenter", null, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);            
    }
    
    public void createLine(Line2D line, float downScaleFactor, float width, float height, String name, Color color) throws VRepException
    {
        FloatWA callParamsF = new FloatWA(10);
        StringWA callParamsS = new StringWA(1);
        
        float[] floatParameters = callParamsF.getArray();
        
        floatParameters[0] = (float) (line.getX1()/downScaleFactor);
        floatParameters[1] = (float) (line.getY1()/downScaleFactor);
        
        floatParameters[2] = (float) (line.getX2()/downScaleFactor);
        floatParameters[3] = (float) (line.getY2()/downScaleFactor);
        floatParameters[4] = (float) (line.length()/downScaleFactor);
        floatParameters[5] = width;
        floatParameters[6] = height;
        floatParameters[7] = (float)color.getRed()/255.0f;
        floatParameters[8] = (float)color.getGreen()/255.0f;
        floatParameters[9] = (float)color.getBlue()/255.0f;
        
        String[] stringParameters = callParamsS.getArray();
        stringParameters[0] = name;
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, "createLine", null, callParamsF, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void deleteAll() throws VRepException
    {
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", 6, "deleteCreated", null, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void putTextureOnRectangle(File tmp, int rectangleHandle) throws VRepException
    {
        IntWA callParamsI = new IntWA(1);
        StringWA callParamsS = new StringWA(2);
        
        int[] intParameters = callParamsI.getArray();
        intParameters[0] = rectangleHandle;
        
        String[] stringParameters = callParamsS.getArray();
        String absolutePath = tmp.getAbsolutePath();
        stringParameters[0] = tmp.getName();
        stringParameters[1] = absolutePath;
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, "textureOnRectangle", callParamsI, null, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void createMesh(List<Point3D> vertices, List<Integer> indices, String name) throws VRepException
    {
        IntWA callParamsI = new IntWA(indices.size());
        int[] intParameters = callParamsI.getArray();
        
        for(int idx = 0; idx < intParameters.length; idx++)
        {
            intParameters[idx] = indices.get(idx);
        }
        
        FloatWA callParamsF = new FloatWA(vertices.size() * 3);
        float[] floatParameters = callParamsF.getArray();
        
        for (int idx = 0; idx < vertices.size(); idx++)
        {
            int arrayIndex = idx * 3;
            double[] verticeArray = vertices.get(idx).getArray();
            floatParameters[arrayIndex] = (float)verticeArray[0];
            floatParameters[arrayIndex + 1] = (float)verticeArray[1];
            floatParameters[arrayIndex + 2] = (float)verticeArray[2];
        }
        StringWA callParamsS = new StringWA(1);
        String[] stringParameters = callParamsS.getArray();
        stringParameters[0] = name;
        
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, "createMesh", callParamsI, callParamsF, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void deleteObjects(List<Integer> handles) throws VRepException
    {
        List<VRepException> exceptions = new ArrayList<>();
        Consumer<? super Integer> removeObject = handle -> {
            try
            {
                _vrep.simxRemoveObject(_clientID, handle, remoteApi.simx_opmode_blocking);
            }
            catch (VRepException exc)
            {
                exceptions.add(exc);
            }
        };
        if(exceptions.isEmpty())
        {
            handles.forEach(removeObject);
        }
        else
        {
            StringBuilder exceptionString = new StringBuilder();
            exceptions.forEach(excpetion -> exceptionString.append(excpetion.getMessage()));
            throw new VRepException(exceptionString.toString());
        }
    }
    
    public void deleteScripts(List<Integer> handles) throws VRepException
    {
        List<VRepException> exceptions = new ArrayList<>();
        Consumer<? super Integer> removeObject = handle -> {
            try
            {
                IntWA callParamsI = new IntWA(1);
                callParamsI.getArray()[0] = handle;
                _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, "simxRemoveScript", callParamsI, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
            }
            catch (VRepException exc)
            {

                exceptions.add(exc);
            }
        };
        if(exceptions.isEmpty())
        {
            handles.forEach(removeObject);
        }
        else
        {
            StringBuilder exceptionString = new StringBuilder();
            exceptions.forEach(excpetion -> exceptionString.append(excpetion.getMessage()));
            throw new VRepException(exceptionString.toString());
        }
    }

    public void addToDeletionList(List<Integer> handlesToDelete) throws VRepException
    {
        IntWA callParamsI = new IntWA(handlesToDelete.size());
        int[] paramArray = callParamsI.getArray();
        Consumer<? super IndexAdder<Integer>> addToArray = cur -> 
        {
            paramArray[cur.idx()] = cur.v();
        };
        handlesToDelete.stream().map(IndexAdder.indexed()).forEachOrdered(addToArray);
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, "addToDeletionList", callParamsI, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public int getScriptAssociatedWithObject(int objectHandle) throws VRepException
    {
        IntWA callParamsI = new IntWA(1);
        callParamsI.getArray()[0] = objectHandle;
        IntWA callParamsO = new IntWA(1);
        _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, 6, "simxGetScriptAssociatedWithObject", callParamsI, null, null, null, callParamsO, null, null, null, remoteApi.simx_opmode_blocking);
        return callParamsO.getArray()[0];
    }
}
