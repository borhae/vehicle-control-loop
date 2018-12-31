package de.joachim.haensel.phd.scenario.sumo2vrep;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import coppelia.FloatWA;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.triangulation.Earcut;
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

    public void createMapSizedRectangle(RoadMap roadMap, boolean isVisible)
    {
        try
        {
            createMapSizedVRepRectangle(roadMap, isVisible);
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }

    public void createMapSizedRectangleWithMapTexture(RoadMap roadMap)
    {
        try
        {
            XYMinMax minMax = roadMap.computeMapDimensions();
            int rectangleHandle = createMapSizedVRepRectangle(roadMap, false);
            
            BufferedImage img = createTexture(roadMap, minMax);
            File tmp = new File("tmpmap.png");
            if(tmp.exists())
            {
                tmp.delete();
            }
            ImageIO.write(img, "png", tmp);
            _vrepObjectCreator.putTextureOnRectangle(tmp, rectangleHandle);
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

    /** 
     * Creates a map in VRep out of basic shapes. Works well for small examples with not more than 100 elements.
     * @param roadMap The map information in SUMO format
     */
    public void createSimplesShapeBasedMap(RoadMap roadMap)
    {
        if(_elementNameCreator == null)
        {
            _elementNameCreator = new IDCreator();
        }
        IJunctionCreator junctionCreator = (junction) -> createVRepJunction(_vrep, _clientID, _elementNameCreator, junction);
        ILaneCreator laneCreator = (curLane, p1, p2) -> createVRepLane(_vrep, _clientID, _elementNameCreator, curLane, p1, p2);
        visitMap(roadMap, junctionCreator, laneCreator);
    }
    
    public void createMeshBasedMap(RoadMap roadMap) throws VRepException
    {
        if(_elementNameCreator == null)
        {
            _elementNameCreator = new IDCreator();
        }

        List<Point3D> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        IJunctionCreator junctionCreator = (junction) -> createVRepMeshJunctionPolygon(vertices, indices, junction);
        ILaneCreator laneCreator = (curLane, p1, p2) -> createVRepMeshLane(vertices, indices, curLane, p1, p2);
//        ILaneCreator laneCreator = (curLane, p1, p2) -> createVRepMeshLanePolygon(vertices, indices, curLane, p1, p2);
//        IWholeLaneCreator laneCreator = (curLane, curEdge, fromJunction, toJunction) -> createVRepMeshLane(vertices, indices, curLane, curEdge, fromJunction, toJunction);
//        visitMapStitchingAdjacentElements(roadMap, junctionCreator, laneCreator);
        visitMap(roadMap, junctionCreator, laneCreator);
        _vrepObjectCreator.createMesh(vertices, indices, "Map", false);
    }
    
    private void createVRepMeshLane(List<Point3D> vertices, List<Integer> indices, LaneType curLane, EdgeType curEdge, JunctionType fromJunction, JunctionType toJunction)
    {
        int startIndex = vertices.size();
        Position2D[] fromJunctionCoords = Position2D.valueOfString(fromJunction.getShape());
        Position2D[] toJunctionCoords = Position2D.valueOfString(toJunction.getShape());
        Position2D[] lanePoints = Position2D.valueOfString(curLane.getShape());
        Float width = curLane.getWidth();
        
        List<Vector2D> leftSide = new ArrayList<>();
        List<Vector2D> rightSide = new ArrayList<>();
        List<Vector2D> center = new ArrayList<>();
        for(int idx = 0; idx < lanePoints.length; idx += 2)
        {
            Position2D pos1 = lanePoints[idx];
            Position2D pos2 = lanePoints[idx + 1];
            Vector2D v = new Vector2D(pos1, pos2);
            Vector2D l = v.shift(width);
            Vector2D r = v.shift(-width);
            rightSide.add(r);
            leftSide.add(l);
        }
        Vector2D firstLeft = leftSide.get(0);
        Vector2D reverseFirstLeft = new Vector2D(firstLeft.getTip(), firstLeft.getBase());
        Position2D intersection = reverseFirstLeft.intersectPolygon(fromJunctionCoords);
    }

    private void garbage(List<Point3D> vertices, List<Integer> indices, LaneType curLane, EdgeType curEdge, JunctionType fromJunction, JunctionType toJunction)
    {
        List<Vector2D> leftSide = new ArrayList<>();
        List<Vector2D> rightSide = new ArrayList<>();
        Position2D[] fromJunctionCoords = Position2D.valueOfString(fromJunction.getShape());
        Position2D[] toJunctionCoords = Position2D.valueOfString(toJunction.getShape());
        
        Position2D leftFirst = leftSide.get(0).getBase();
        Position2D leftLast = leftSide.get(leftSide.size() - 1).getTip();
        Position2D rightFirst = rightSide.get(0).getBase();
        Position2D rightLast = rightSide.get(rightSide.size() - 1).getTip();

        Position2D leftFirstIntersection = fromJunctionCoords[Position2D.getClosestIdx(leftFirst, fromJunctionCoords)];
        Position2D leftLastIntersection = toJunctionCoords[Position2D.getClosestIdx(leftLast, toJunctionCoords)];
        
        Position2D rightFirstIntersection = fromJunctionCoords[Position2D.getClosestIdx(rightFirst, fromJunctionCoords)];
        Position2D rightLastIntersection = toJunctionCoords[Position2D.getClosestIdx(rightLast, toJunctionCoords)];
        
        if((leftFirstIntersection != rightFirstIntersection) || (leftLastIntersection != rightLastIntersection))
        {
            System.out.println("bummer, look here");
        }
        double leftFirstDist = Position2D.distance(leftFirst, leftFirstIntersection);
        double rightFirstDist = Position2D.distance(rightFirst, rightFirstIntersection);

        vertices.add(new Point3D(leftFirstIntersection));
        if(leftFirstDist < rightFirstDist)
        {
            
        }
        else
        {
            
        }

        double leftLastDist = Position2D.distance(leftLast, leftLastIntersection);
        double rightLastDist = Position2D.distance(rightLast, rightLastIntersection);

    }
    
    private void createVRepMeshLanePolygon(List<Point3D> vertices, List<Integer> indices, LaneType curLane, String p1, String p2)
    {
        int startingIndex = vertices.size();
        
        Vector2D v = new Vector2D(new Line2D(p1, p2));
        Vector2D l = v.shift(_streetWidth);
        Vector2D r = v.shift(-_streetWidth);
        
        vertices.add(r.getBase().toPoint3D());
        vertices.add(r.getTip().toPoint3D());

        vertices.add(l.getBase().toPoint3D());
        vertices.add(l.getTip().toPoint3D());
        
        indices.add(startingIndex + 0);
        indices.add(startingIndex + 1);
        indices.add(startingIndex + 3);

        indices.add(startingIndex + 3);
        indices.add(startingIndex + 2);
        indices.add(startingIndex + 0);
        
        String shape = curLane.getShape();
        String[] coordinatesString = shape.split("[ ,]");
        double[] coordinates = new double[coordinatesString.length];
        for (int idx = 0; idx < coordinatesString.length; idx++)
        {
            coordinates[idx] = Double.valueOf(coordinatesString[idx]);
        }
        List<Integer> triangles = Earcut.earcut(coordinates);
        for(int idx = 0; idx < coordinates.length; idx += 2)
        {
            vertices.add(new Point3D(coordinates[idx], coordinates[idx + 1]));
        }
        triangles.forEach(triangleIdx -> indices.add(triangleIdx + startingIndex));
    }

    private void createVRepMeshJunctionPolygon(List<Point3D> vertices, List<Integer> indices, JunctionType junction)
    {
        int startingIndex = vertices.size();
        String shape = junction.getShape();
        String[] coordinatesString = shape.split("[ ,]");
        double[] coordinates = new double[coordinatesString.length];
        for (int idx = 0; idx < coordinatesString.length; idx++)
        {
            coordinates[idx] = Double.valueOf(coordinatesString[idx]);
        }
        List<Integer> triangles = Earcut.earcut(coordinates);
        for(int idx = 0; idx < coordinates.length; idx += 2)
        {
            vertices.add(new Point3D(coordinates[idx], coordinates[idx + 1]));
        }
        triangles.forEach(triangleIdx -> indices.add(triangleIdx + startingIndex));
    }

    /**
     * For now create a quad, in future we read the shape
     * @param vertices
     * @param indices
     * @param junction
     */
    private void createVRepMeshJunctionQuads(List<Point3D> vertices, List<Integer> indices, JunctionType junction)
    {
        int startingIndex = vertices.size();
        
        float xPos = junction.getX();
        float yPos = junction.getY();
        double width = 10;
        double height = 10;
        double wH = width / 2.0;
        double hH = height / 2.0;
            
        vertices.add(new Point3D(xPos - wH, yPos - hH));
        vertices.add(new Point3D(xPos - wH, yPos + hH));
        vertices.add(new Point3D(xPos + wH, yPos + hH));
        vertices.add(new Point3D(xPos + wH, yPos - hH));
        
        indices.add(startingIndex + 0);
        indices.add(startingIndex + 1);
        indices.add(startingIndex + 2);

        indices.add(startingIndex + 2);
        indices.add(startingIndex + 3);
        indices.add(startingIndex + 0);
    }

    private void createVRepMeshLane(List<Point3D> vertices, List<Integer> indices, LaneType curLane, String p1, String p2)
    {
        int startingIndex = vertices.size();

        Vector2D v = new Vector2D(new Line2D(p1, p2));
        Vector2D l = v.shift(_streetWidth);
        Vector2D r = v.shift(-_streetWidth);
        
        vertices.add(r.getBase().toPoint3D());
        vertices.add(r.getTip().toPoint3D());

        vertices.add(l.getBase().toPoint3D());
        vertices.add(l.getTip().toPoint3D());
        
        indices.add(startingIndex + 0);
        indices.add(startingIndex + 1);
        indices.add(startingIndex + 3);

        indices.add(startingIndex + 3);
        indices.add(startingIndex + 2);
        indices.add(startingIndex + 0);
    }

    private BufferedImage createTexture(RoadMap roadMap, XYMinMax minMax)
    {
        double scaleY = 2.0;
        double scaleX = 2.0;
        int width = (int)minMax.distX();
        int height = (int)minMax.distY();
        BufferedImage img = new BufferedImage(width * (int)scaleX, height * (int)scaleY, BufferedImage.TYPE_INT_ARGB);
        AffineTransform transformation = new AffineTransform();
        transformation.scale(scaleX, scaleY);
        transformation.rotate(Math.toRadians(180), width/2, height/2);
        Graphics2D graphics2dObject = img.createGraphics();
        graphics2dObject.transform(transformation);
        graphics2dObject.setColor(Color.LIGHT_GRAY);
        graphics2dObject.fillRect(0, 0, width, height);
        graphics2dObject.setColor(Color.BLACK);
        
        int xCenter = (int) (minMax.distX()/2.0);
        int yCenter = (int) (minMax.distY()/2.0);
        IJunctionCreator junctionCreator = (junction) -> drawJunction(graphics2dObject, xCenter, yCenter, junction);
        ILaneCreator laneCreator = (lane, p1, p2) -> drawLane(graphics2dObject, xCenter, yCenter, lane, p1, p2);
        visitMap(roadMap, junctionCreator, laneCreator);
        return img;
    }

    private int createMapSizedVRepRectangle(RoadMap roadMap, boolean isVisible) throws VRepException
    {
        if(_elementNameCreator == null)
        {
            _elementNameCreator = new IDCreator();
        }
        XYMinMax minMax = roadMap.computeMapDimensions();
        _vrepObjectCreator.createMapCenter();
        ShapeParameters shapeParameters = new ShapeParameters();
        shapeParameters.setIsDynamic(false);
        shapeParameters.setIsRespondable(true);
        shapeParameters.setMass(10000);
        shapeParameters.setName(_elementNameCreator.createPlaneID());
        shapeParameters.setOrientation(0.0f, 0.0f, 0.0f);
        shapeParameters.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        float sizeX = (float) minMax.distX();
        float sizeY = (float) minMax.distY();
        float sizeZ = 20.0f;
        shapeParameters.setSize(sizeX, sizeY, sizeZ);
        float posX =  (float) (minMax.minX() + minMax.distX()/2.0);
        float posY = (float) (minMax.minY() + minMax.distY()/2.0);
        float posZ = (float) -sizeZ/2.0f;
        shapeParameters.setPosition(posX, posY, posZ);
        shapeParameters.setType(EVRepShapes.CUBOID);
        shapeParameters.setVisibility(isVisible);
        int rectangleHandle = _vrepObjectCreator.createPrimitive(shapeParameters);
        return rectangleHandle;
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
        int x = (int) ((xPos - width /2.0) + xCenter);
        int y = (int) ((yPos - height / 2.0) + yCenter);
        
        graphics2dObject.drawOval(x, y, width, height);
    }

    private void visitMap(RoadMap roadMap, IJunctionCreator junctionCreator, ILaneCreator laneCreator)
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
    
    private void visitMapStitchingAdjacentElements(RoadMap roadMap, IJunctionCreator junctionCreator, IWholeLaneCreator laneCreator)
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
                    JunctionType fromJunction = roadMap.getJunctionForName(curEdge.getFrom());
                    JunctionType toJunction = roadMap.getJunctionForName(curEdge.getTo());
                    
                    laneCreator.create(curLane, curEdge, fromJunction, toJunction);
                }
            }
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
