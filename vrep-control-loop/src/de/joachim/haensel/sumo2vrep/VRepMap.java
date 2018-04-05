package de.joachim.haensel.sumo2vrep;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import coppelia.FloatWA;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class VRepMap
{
    private float _streetWidth;
    private float _streetHeight;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _vrepObjectCreator;
    private IDCreator _elementNameCreator;
    
    public VRepMap(float streetWidth, float streetHeight, VRepRemoteAPI vrep, int clientID, VRepObjectCreation vrepObjectCreator)
    {   
        _streetWidth = streetWidth;
        _streetHeight = streetHeight;
        _vrep = vrep;
        _clientID = clientID;
        _vrepObjectCreator = vrepObjectCreator;
    }

    public void createMapSizedPlane(RoadMap roadMap)
    {
        if(_elementNameCreator == null)
        {
            _elementNameCreator = new IDCreator();
        }
        try
        {
            XYMinMax minMax = roadMap.computeMapDimensions();
            _vrepObjectCreator.createMapCenter();
            ShapeParameters shapeParameters = new ShapeParameters();
            shapeParameters.setIsDynamic(false);
            shapeParameters.setIsRespondable(true);
            shapeParameters.setMass(10);
            shapeParameters.setName(_elementNameCreator.createPlaneID());
            shapeParameters.setOrientation(0.0f, 0.0f, 0.0f);
            shapeParameters.setRespondableMask(ShapeParameters.GLOBAL_ONLY_RESPONDABLE_MASK);
            float sizeX = minMax.distX();
            float sizeY = minMax.distY();
            System.out.println(minMax);
            float sizeZ = 0.2f;
            shapeParameters.setSize(sizeX, sizeY, sizeZ);
            float posX =  (float) (minMax.minX() + minMax.distX()/2.0);
            float posY = (float) (minMax.minY() + minMax.distY()/2.0);
            shapeParameters.setPosition(posX, posY, 0.0f);
            shapeParameters.setType(EVRepShapes.CUBOID);
            _vrepObjectCreator.createPrimitive(shapeParameters);
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }

    public void createTextureAndSingleRectangleBasedMap(RoadMap roadMap)
    {
        if(_elementNameCreator == null)
        {
            _elementNameCreator = new IDCreator();
        }
        try
        {
            XYMinMax minMax = roadMap.computeMapDimensions();
            _vrepObjectCreator.createMapCenter();
            ShapeParameters shapeParameters = new ShapeParameters();
            shapeParameters.setIsDynamic(false);
            shapeParameters.setIsRespondable(true);
            shapeParameters.setMass(10);
            shapeParameters.setName(_elementNameCreator.createPlaneID());
            shapeParameters.setOrientation(0.0f, 0.0f, 0.0f);
            shapeParameters.setRespondableMask(ShapeParameters.GLOBAL_ONLY_RESPONDABLE_MASK);
            float sizeX = minMax.distX();
            float sizeY = minMax.distY();
            System.out.println(minMax);
            float sizeZ = 0.2f;
            shapeParameters.setSize(sizeX, sizeY, sizeZ);
            float posX =  (float) (minMax.minX() + minMax.distX()/2.0);
            float posY = (float) (minMax.minY() + minMax.distY()/2.0);
            shapeParameters.setPosition(posX, posY, 0.0f);
            shapeParameters.setType(EVRepShapes.CUBOID);
            _vrepObjectCreator.createPrimitive(shapeParameters);
            
            int width = (int)minMax.distX();
            int height = (int)minMax.distY();
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2dObject = img.createGraphics();
            graphics2dObject.setColor(Color.LIGHT_GRAY);
            graphics2dObject.fillRect(0, 0, width, height);
            graphics2dObject.setColor(Color.BLACK);
            
            int xCenter = (int) (minMax.distX()/2.0);
            int yCenter = (int) (minMax.distY()/2.0);
            IJunctionCreator junctionCreator = (junction) -> drawJunction(graphics2dObject, xCenter, yCenter, junction);
            ILaneCreator laneCreator = (lane, p1, p2) -> drawLane(graphics2dObject, xCenter, yCenter, lane, p1, p2);
            createMapStructure(roadMap, junctionCreator, laneCreator);
            File tmp = new File("tmpmap.png");
            if(tmp.exists())
            {
                tmp.delete();
            }
            ImageIO.write(img, "png", tmp);
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    private void drawLane(Graphics2D graphics2dObject, int xCenter, int yCenter, LaneType lane, String p1, String p2)
    {
        String[] coordinate1 = p1.split(",");
        String[] coordinate2 = p2.split(",");
        
        double x1 = Double.parseDouble(coordinate1[0]) + xCenter;
        double y1 = Double.parseDouble(coordinate1[1]) + yCenter;

        double x2 = Double.parseDouble(coordinate2[0]) + xCenter;
        double y2 = Double.parseDouble(coordinate2[1]) + yCenter;
        double width = _streetWidth;
        
        Stroke oldStroke = graphics2dObject.getStroke();
        graphics2dObject.setStroke(new BasicStroke((int)width));
        
        graphics2dObject.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
        
        graphics2dObject.setStroke(oldStroke);
    }

    private void drawJunction(Graphics2D graphics2dObject, int xCenter, int yCenter, JunctionType junction)
    {
        float xPos = junction.getX();
        float yPos = junction.getY();
        int width = 10;
        int height = 10;
        int x = (int) (xPos - width /2.0) + xCenter;
        int y = (int) (yPos - height / 2.0) + yCenter;
        
        graphics2dObject.drawOval(x, y, width, height);
    }

    public void createSimplesShapeBasedMap(RoadMap roadMap)
    {
        if(_elementNameCreator == null)
        {
            _elementNameCreator = new IDCreator();
        }
        IJunctionCreator junctionCreator = (junction) -> createVRepJunction(_vrep, _clientID, _elementNameCreator, junction);
        ILaneCreator laneCreator = (curLane, p1, p2) -> createVRepLane(_vrep, _clientID, _elementNameCreator, curLane, p1, p2);
        createMapStructure(roadMap, junctionCreator, laneCreator);
    }

    private void createMapStructure(RoadMap roadMap, IJunctionCreator junctionCreator, ILaneCreator laneCreator)
    {
        try
        {
            List<JunctionType> junctions = roadMap.getJunctions();
            for (JunctionType curJunction : junctions)
            {
                if(curJunction.getType().equals("internal"))
                {
                    continue;
                }
                junctionCreator.create(curJunction);
            }
            _vrepObjectCreator.createMapCenter();
            List<EdgeType> edges = roadMap.getEdges();
            int numOfLanes = computeNumOfLanes(edges);
            System.out.println("about to create: " + numOfLanes + " lanes!!");
            for (EdgeType curEdge : edges)
            {
                String function = curEdge.getFunction();
                if(function == null || function.isEmpty())
                {
                    List<LaneType> lanes = curEdge.getLane();
                    for (LaneType curLane : lanes)
                    {
                        String shape = curLane.getShape();
                        String[] lineCoordinates = shape.split(" ");
                        int numberCoordinates = lineCoordinates.length;
                        if(numberCoordinates == 2)
                        {
                            String p1 = lineCoordinates[0];
                            String p2 = lineCoordinates[1];
                            laneCreator.create(curLane, p1, p2);
                        }
                        else
                        {
                            createLaneRecursive(laneCreator, curLane, Arrays.asList(lineCoordinates));
                        }
                    }
                }
            }
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }
    
    private void createLaneRecursive(ILaneCreator laneCreator, LaneType curLane, List<String> lineCoordinates) throws VRepException
    {
        int listSize = lineCoordinates.size();
        if(listSize >= 2)
        {
            laneCreator.create(curLane, lineCoordinates.get(0), lineCoordinates.get(1));
            if(listSize >= 3)
            {
                createLaneRecursive(laneCreator, curLane, lineCoordinates.subList(1, lineCoordinates.size()));
            }
        }
    }

    private int computeNumOfLanes(List<EdgeType> edges)
    { 
        int result = 0;
        for (EdgeType curEdge : edges)
        {
            String function = curEdge.getFunction();
            if(function == null || function.isEmpty())
            {
                List<LaneType> lanes = curEdge.getLane();
                for (LaneType curLane : lanes)
                {
                    String shape = curLane.getShape();
                    String[] lineCoordinates = shape.split(" ");
                    result += lineCoordinates.length - 1;
                }
            }
        }

        return result;
    }

    public void createVRepJunction(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, JunctionType junction) 
    {
        float xPos = junction.getX();
        float yPos = junction.getY();

        FloatWA callParamsFA = new FloatWA(5); 
        float[] floatParameters = callParamsFA.getArray();
        floatParameters[0] = xPos;
        floatParameters[1] = yPos;
        floatParameters[2] = 0; // zPos
        floatParameters[3] = _streetWidth;
        floatParameters[4] = _streetHeight;
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = elementNameCreator.createJunctionID(junction);
        
        try
        {
            vrep.simxCallScriptFunction(clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "createJunction", null, callParamsFA, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    public void createVRepLane(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, LaneType curLane, String p1, String p2) 
    {
        FloatWA callParamsF = new FloatWA(7);
        StringWA callParamsS = new StringWA(1);
        
        float[] floatParameters = callParamsF.getArray();
        String[] coordinate1 = p1.split(",");
        String[] coordinate2 = p2.split(",");
        
        floatParameters[0] = Float.parseFloat(coordinate1[0]);
        floatParameters[1] = Float.parseFloat(coordinate1[1]);

        floatParameters[2] = Float.parseFloat(coordinate2[0]);
        floatParameters[3] = Float.parseFloat(coordinate2[1]);
        floatParameters[4] = curLane.getLength();
        floatParameters[5] = _streetWidth;
        floatParameters[6] = _streetHeight;
        
        String[] stringParameters = callParamsS.getArray();
        stringParameters[0] = elementNameCreator.createLaneID(curLane);
        try
        {
            vrep.simxCallScriptFunction(clientID, "ScriptLoader", 6, "createEdge", null, callParamsF, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }
    
    public void deleteAll() throws VRepException
    {
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", 6, "deleteCreated", null, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public IDCreator getIDMapper()
    {
        return _elementNameCreator;
    }


    public void setStreetWidthAndHeight(float streetWidth, float streetHeight)
    {
        _streetWidth = streetWidth;
        _streetHeight = streetHeight;
    }
}
