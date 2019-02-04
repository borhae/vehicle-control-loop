package de.joachim.haensel.phd.scenario.lua.test;


import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestMeshDrawing
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;
    private static final String VREP_LOADING_SCRIPT_PARENT_OBJECT = "ScriptLoader";

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterAll
    public static void tearDownVrep() 
    {
        _vrep.simxFinish(_clientID);
    }
    
    @AfterEach
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testOneTriangle() throws VRepException
    {
        List<Point3D> vertices = new ArrayList<>();  
        vertices.add(new Point3D(0.0, 3.0, 0.0));
        vertices.add(new Point3D(0.0, 0.0, 0.0));
        vertices.add(new Point3D(3.0, 0.0, 0.0));
        
        List<Integer> indices = new ArrayList<>();
        indices.add(2);
        indices.add(0);
        indices.add(1);
        
        _objectCreator.createMesh(vertices, indices, "MyMesh", false);
        System.out.println("can you see me in vrep?");
    }

    @Test
    public void testTwoTriangles() throws VRepException
    {
        List<Point3D> vertices = new ArrayList<>();  
        vertices.add(new Point3D(0.0, 0.0, 0.0));
        vertices.add(new Point3D(3.0, 0.0, 0.0));
        vertices.add(new Point3D(0.0, 3.0, 0.0));
        vertices.add(new Point3D(3.0, 3.0, 0.0));
        
        List<Integer> indices = new ArrayList<>();
        //first
        indices.add(2);
        indices.add(0);
        indices.add(1);
        //second
        indices.add(1);
        indices.add(3);
        indices.add(2);
        
        _objectCreator.createMesh(vertices, indices, "MyMesh", false);
        System.out.println("can you see me in vrep?");
    }
}
