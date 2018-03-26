package de.joachim.haensel.phd.scenario.lua.test;

import static org.junit.Assert.fail;

import java.awt.Color;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestRemoteLuaDrawingFunctions
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    private static final String VREP_LOADING_SCRIPT_PARENT_OBJECT = "ScriptLoader";

    @BeforeClass
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterClass
    public static void tearDownVrep() 
    {
        _vrep.simxFinish(_clientID);
    }
    
    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }

    /** 
     * Use debug to see if line shows up in simulator
     */
    @Test
    public void testDrawAndRemoveVector()
    {

        try
        {
            Color c = Color.CYAN;
            FloatWA createDrawingObjectLineLuaFloats = new FloatWA(3);
            float[] createDrawingObjectLineFloats = createDrawingObjectLineLuaFloats.getArray();
            createDrawingObjectLineFloats[0] = (float) c.getRed() / 255.0f;
            createDrawingObjectLineFloats[1] = (float) c.getGreen() / 255.0f;
            createDrawingObjectLineFloats[2] = (float) c.getBlue() / 255.0f;

            //_vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "drawVector", inInts, inFloats, inStrings, inBuffer, outInts, outFloats, outStrings, outBuffer, remoteApi.simx_opmode_blocking);
            IntWA result = new IntWA(1);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectLine", null, createDrawingObjectLineLuaFloats, null, null, result, null, null, null, remoteApi.simx_opmode_blocking);
            int handle = result.getArray()[0];
            System.out.println("handle: " + handle);

            Vector2D v = new Vector2D(0.0, 0.0, 50.0, 50.0);
            FloatWA redrawLineLuaFloats = new FloatWA(6);
            float[] redrawLineFloats = redrawLineLuaFloats.getArray();
            redrawLineFloats[0] = (float) v.getBase().getX();
            redrawLineFloats[1] = (float) v.getBase().getY();
            redrawLineFloats[2] = 0.0f;
            redrawLineFloats[3] = (float) v.getTip().getX();
            redrawLineFloats[4] = (float) v.getTip().getY();
            redrawLineFloats[5] = 0.0f;
            IntWA inHandle = new IntWA(1);
            inHandle.getArray()[0] = handle;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawLine", inHandle, redrawLineLuaFloats, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", inHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            fail();
            exc.printStackTrace();
        }
    }

    /** 
     * Use debug to see if line shows up in simulator
     */
    @Test
    public void testDrawUpdateAndRemoveVector()
    {
        Vector2D v = new Vector2D(0.0, 0.0, 50.0, 50.0);
        Color c = Color.RED;
        FloatWA callParamsF = new FloatWA(6);
        float[] floatParamsArray = callParamsF.getArray();
        floatParamsArray[0] = (float) v.getBase().getX();
        floatParamsArray[1] = (float) v.getBase().getY();
        floatParamsArray[2] = 0.0f;
        floatParamsArray[3] = (float) v.getTip().getX();
        floatParamsArray[4] = (float) v.getTip().getY();
        floatParamsArray[5] = 0.0f;
        try
        {
            //_vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "drawVector", inInts, inFloats, inStrings, inBuffer, outInts, outFloats, outStrings, outBuffer, remoteApi.simx_opmode_blocking);
            IntWA result = new IntWA(1);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectLine", null, callParamsF, null, null, result, null, null, null, remoteApi.simx_opmode_blocking);
            int handle = result.getArray()[0];
            System.out.println("handle: " + handle);
            IntWA inHandle = new IntWA(1);
            inHandle.getArray()[0] = handle;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawLine", inHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
            
            callParamsF = new FloatWA(6);
            floatParamsArray = callParamsF.getArray();
            floatParamsArray[0] = (float) v.getBase().getX() + 10.0f;
            floatParamsArray[1] = (float) v.getBase().getY() + 10.0f;
            floatParamsArray[2] = 0.0f;
            floatParamsArray[3] = (float) v.getTip().getX() + 10.0f;
            floatParamsArray[4] = (float) v.getTip().getY() + 10.0f;
            floatParamsArray[5] = 0.0f;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawLine", inHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", inHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            fail();
            exc.printStackTrace();
        }
    }
    
    @Test
    public void testDrawCircle()
    {
        Position2D center = new Position2D(0.0, 0.0);
        float radius = 3.0f;
        Color c = Color.MAGENTA;
        try
        {
            FloatWA createDrawingObjectCircleLuaFloats = new FloatWA(3);
            float[] createDrawingObjectCircleFloats = createDrawingObjectCircleLuaFloats.getArray();
            createDrawingObjectCircleFloats[0] = c.getRed();
            createDrawingObjectCircleFloats[1] = c.getGreen();
            createDrawingObjectCircleFloats[2] = c.getBlue();
            IntWA result = new IntWA(1);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectCircle", null, createDrawingObjectCircleLuaFloats, null, null, result, null, null, null, remoteApi.simx_opmode_blocking);
            int handle = result.getArray()[0];
            System.out.println("handle: " + handle);

            
            IntWA inHandle = new IntWA(1);
            inHandle.getArray()[0] = handle;
            FloatWA redrawCircleLuaFloats = new FloatWA(6);
            float[] redrawCircleFloats = redrawCircleLuaFloats.getArray();
            redrawCircleFloats[0] = (float) center.getX();
            redrawCircleFloats[1] = (float) center.getY();
            redrawCircleFloats[2] = 0.0f;
            redrawCircleFloats[3] = radius;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawCircle", inHandle, redrawCircleLuaFloats, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", inHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            fail();
            exc.printStackTrace();
        }
    }
  
    @Test
    public void testDrawMoveCircle()
    {
        Position2D center = new Position2D(0.0, 0.0);
        float radius = 3.0f;
        Color c = Color.RED;
        FloatWA callParamsF = new FloatWA(6);
        float[] floatParamsArray = callParamsF.getArray();
        floatParamsArray[0] = (float) center.getX();
        floatParamsArray[1] = (float) center.getY();
        floatParamsArray[2] = 0.0f;
        floatParamsArray[3] = radius;
        try
        {
            IntWA result = new IntWA(1);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectCircle", null, null, null, null, result, null, null, null, remoteApi.simx_opmode_blocking);
            int handle = result.getArray()[0];
            System.out.println("handle: " + handle);
            IntWA inHandle = new IntWA(1);
            inHandle.getArray()[0] = handle;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawCircle", inHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

            callParamsF = new FloatWA(6);
            floatParamsArray = callParamsF.getArray();
            floatParamsArray[0] = (float) center.getX() + 5;
            floatParamsArray[1] = (float) center.getY() + 5;
            floatParamsArray[2] = 0.0f;
            floatParamsArray[3] = radius;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawCircle", inHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", inHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            fail();
            exc.printStackTrace();
        }
    }

    /** 
     * Use debug to see if line shows up in simulator
     */
    @Test
    public void testDrawCircleAndLine()
    {
        Position2D center = new Position2D(0.0, 0.0);
        float radius = 3.0f;
        Color c = Color.RED;
        FloatWA luaFloatsCircle = new FloatWA(6);
        float[] circleArray = luaFloatsCircle.getArray();
        circleArray[0] = (float) center.getX();
        circleArray[1] = (float) center.getY();
        circleArray[2] = 0.0f;
        circleArray[3] = radius;

        Vector2D v = new Vector2D(-3.0, 0.0, 6.0, 0.0);
        FloatWA callParamsF = new FloatWA(6);
        float[] floatParamsArray = callParamsF.getArray();
        floatParamsArray[0] = (float) v.getBase().getX();
        floatParamsArray[1] = (float) v.getBase().getY();
        floatParamsArray[2] = 0.0f;
        floatParamsArray[3] = (float) v.getTip().getX();
        floatParamsArray[4] = (float) v.getTip().getY();
        floatParamsArray[5] = 0.0f;
        try
        {
            IntWA luaCircleResult = new IntWA(1);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectCircle", null, null, null, null, luaCircleResult, null, null, null, remoteApi.simx_opmode_blocking);
            int circleHandle = luaCircleResult.getArray()[0];
            System.out.println("handle: " + circleHandle);
            IntWA luaCircleHandle = new IntWA(1);

            IntWA luaLineResult = new IntWA(1);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectLine", null, callParamsF, null, null, luaLineResult, null, null, null, remoteApi.simx_opmode_blocking);
            int lineHandle = luaLineResult.getArray()[0];
            System.out.println("handle: " + lineHandle);
            IntWA luaLineHandle = new IntWA(1);
            luaLineHandle.getArray()[0] = lineHandle;

            luaCircleHandle.getArray()[0] = circleHandle;
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawCircle", luaCircleHandle, luaFloatsCircle, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "redrawLine", luaLineHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", luaCircleHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
            _vrep.simxCallScriptFunction(_clientID, VREP_LOADING_SCRIPT_PARENT_OBJECT, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", luaLineHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            fail();
            exc.printStackTrace();
        }
    }
}
