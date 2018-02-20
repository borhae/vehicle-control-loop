package de.joachim.haensel.vehicle;

import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehiclecontrol.reactive.CarControlInterface;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.joints.EVRepJointModes;
import de.joachim.haensel.vrepshapecreation.joints.EVRepJointTypes;
import de.joachim.haensel.vrepshapecreation.joints.JointParameters;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import de.joachim.haensel.vwpoloproperties.VWPoloDimensions;

public class VehicleCreator
{
    private static final String PHYSICAL_CAR_BODY_NAME = "physicalCarBody";
    private static final int GLOBAL_ONLY_RESPONDABLE_MASK = 0b1111_1111__0000_0000;
    private static final int GLOBAL_AND_LOCAL_RESPONDABLE_MASK = 0b1111_1111__1111_1111;
    private static final float WHEEL_WIDTH = 0.2f;
    private static final float WHEEL_DIAMETER = 0.5f;
    private static final float STEERING_DIAMETER = 0.1f;
    private static final float STEERING_LENGTH = 0.25f;
    
    private static final float DAMPER_DIAMETER = 0.075f;
    private static final float DAMPER_LENGTH = 0.3f;
    private static final float DAMPER_INSET = DAMPER_DIAMETER/2.5f;
    private static VRepRemoteAPI _vrep;
    private  int _clientID;
    private float _carHeight;
    private VRepObjectCreation _objectCreator;
    
    public VehicleCreator(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator)
    {
        _vrep = vrep;
        _clientID = clientID;
        _carHeight = 0.2f;
        _objectCreator = objectCreator;
    }

    public Vehicle createAt(float x, float y, float z, RoadMap roadMap)
    {
        try
        {
            VehicleHandles vehicleHandles = new VehicleHandles();

            float baseLength = (float) (VWPoloDimensions.getWheelbase() + 100.0)/1000;
            float baseWidth = (float) (VWPoloDimensions.getWidth() - 100)/1000;
            
            int physicalBodyHandle = createCarBody(_objectCreator, baseLength, baseWidth, _carHeight, x, y, z);
            
            int damperRearLeft = createDamper(_objectCreator, physicalBodyHandle, "damperRearLeft", (float)-baseWidth/2 + DAMPER_INSET, (float)-baseLength/2 + 0.1f, DAMPER_LENGTH/4 + _carHeight/2, false);
            int damperRearRight = createDamper(_objectCreator, physicalBodyHandle, "damperRearRight", (float)baseWidth/2 - DAMPER_INSET, (float)-baseLength/2 + 0.1f, DAMPER_LENGTH/4 + _carHeight/2, false);
            int damperFrontLeft = createDamper(_objectCreator, physicalBodyHandle, "damperFrontLeft", (float)-baseWidth/2 + DAMPER_INSET, (float)baseLength/2 - 0.1f, DAMPER_LENGTH/4 + _carHeight/2, true);
            int damperFrontRight = createDamper(_objectCreator, physicalBodyHandle, "damperFrontRight", (float)baseWidth/2 - DAMPER_INSET, (float)baseLength/2 - 0.1f, DAMPER_LENGTH/4 + _carHeight/2, true);

            int steeringFrontLeft = createSteering(_objectCreator, "steeringFrontLeft", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
            int steeringFrontRight = createSteering(_objectCreator, "steeringFrontRight", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
            
            createConnector(_objectCreator, damperFrontLeft, steeringFrontLeft, "connectorDflSfl", 0.0f, 0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
            createConnector(_objectCreator, damperFrontRight, steeringFrontRight, "connectorDfrSfr", 0.0f, 0.0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
            
            int motorFrontLeft = createMotor(_objectCreator, "motorFrontLeft", 0.0f, 0.0f, 0.0f);
            int motorFrontRight = createMotor(_objectCreator, "motorFrontRight", 0.0f, 0.0f, 0.0f);
            
            createConnector(_objectCreator, steeringFrontLeft, motorFrontLeft, "connectorSflMfl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            createConnector(_objectCreator, steeringFrontRight, motorFrontRight, "connectorSfrMfr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            
            int axisRearLeft = createAxis(_objectCreator, "axisRearLeft", 0.0f, 0.0f, 0.0f);
            int axisRearRight = createAxis(_objectCreator, "axisRearRight", 0.0f, 0.0f, 0.0f);
            
            createConnector(_objectCreator, damperRearLeft, axisRearLeft, "connectorDrlArl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            createConnector(_objectCreator, damperRearRight, axisRearRight, "connectorDrrArr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            
            int frontLeftWheel = createWheel(_objectCreator, "frontLeftWheel", motorFrontLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
            int frontRightWheel = createWheel(_objectCreator, "frontRightWheel", motorFrontRight, WHEEL_DIAMETER, WHEEL_WIDTH);
            int rearLeftWheel = createWheel(_objectCreator, "rearLeftWheel", axisRearLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
            int rearRightWheel = createWheel(_objectCreator, "rearRightWheel", axisRearRight, WHEEL_DIAMETER, WHEEL_WIDTH);

            CarControlInterface car1 = new CarControlInterface(_objectCreator, PHYSICAL_CAR_BODY_NAME, _vrep, _clientID, physicalBodyHandle);
            
            vehicleHandles.setPhysicalBody(physicalBodyHandle).setRearLeftWheel(rearLeftWheel).setRearRightWheel(rearRightWheel);
            car1.initialize();
            return new Vehicle(_objectCreator, _vrep, _clientID, vehicleHandles, car1, roadMap);
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private int createCarBody(VRepObjectCreation creator, float baseLength, float baseWidth, float height, float posX, float posY, float posZ)
            throws VRepException
    {
        ShapeParameters carBodyParams = new ShapeParameters();
        carBodyParams.setName(PHYSICAL_CAR_BODY_NAME);
        carBodyParams.setPosition(posX, posY, posZ);
        carBodyParams.setOrientation(0.0f, 0.0f, 0.0f);
        carBodyParams.setSize(baseWidth, baseLength, height);
        carBodyParams.setMass(1000.0f);
        carBodyParams.setType(EVRepShapes.CUBOID);
        carBodyParams.setIsRespondable(true);
        carBodyParams.setRespndableMask(GLOBAL_ONLY_RESPONDABLE_MASK);
        carBodyParams.setIsDynamic(true);

        int physicalBodyHandle = creator.createPrimitive(carBodyParams);
        return physicalBodyHandle;
    }

    private int createWheel(VRepObjectCreation creator, String name, int parent, float diameter, float width) throws VRepException
    {
        ShapeParameters params = new ShapeParameters();
        params.setName(name);
        params.setPosition(0f, 0f, 0f);
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setSize(diameter, width, width);
        params.setMass(10.0f);
        params.setType(EVRepShapes.CYLINDER);
        params.setIsRespondable(true);
        params.setRespndableMask(GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        params.setIsDynamic(true);
        int wheel = creator.createPrimitive(params);
        creator.setParentForChild(parent, wheel, false);
        return wheel;
    }

    private  int createAxis(VRepObjectCreation creator, String name, float x, float y, float z) throws VRepException
    {
        JointParameters params = new JointParameters();
        params.setPosition(x, y, z);
        params.setSize(0.2f, 0.05f);
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setCyclic(true);
        params.setInterval(new float[]{0.0f, 0.0f});
        params.setType(EVRepJointTypes.REVOLUTE);
        params.setMode(EVRepJointModes.FORCE);
        params.setName(name);
        return creator.createJoint(params);
    }

    private  int createMotor(VRepObjectCreation creator, String name, float x, float y, float z) throws VRepException
    {
        JointParameters params = new JointParameters();
        params.setPosition(x, y, z);
        params.setSize(0.2f, 0.05f);
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setCyclic(true);
        params.setMotorEnabled(true);
        params.setMaximumForce(200.0f);
        params.setTargetVelocity(1000.0f);
        params.setInterval(new float[]{0.0f, 0.0f});
        params.setType(EVRepJointTypes.REVOLUTE);
        params.setMode(EVRepJointModes.FORCE);
        params.setName(name);
        return creator.createJoint(params);
    }

    private  int createConnector(VRepObjectCreation creator, int parent, int child, String name, float x, float y, float z, float diameter, float height) throws VRepException
    {
        ShapeParameters params = new ShapeParameters();
        params.setName(name);
        params.setPosition(x, y, z);
        params.setOrientation(0f, (float)Math.PI/2, 0f);
        params.setSize(height, diameter, diameter);
        params.setMass(10.0f);
        params.setType(EVRepShapes.CYLINDER);
        params.setIsRespondable(false);
        params.setIsDynamic(true);
        int connector = creator.createPrimitive(params);
        creator.setIntParameter(connector, remoteApi.sim_shapeintparam_respondable, 0);
        creator.setParentForChild(parent, connector, false);
        creator.setParentForChild(connector, child, false);
        return connector;
    }

    private  int createSteering(VRepObjectCreation creator, String name, float posX, float posY, float posZ) throws VRepException
    {
        JointParameters params = new JointParameters();
        params.setName(name);
        params.setPosition(posX, posY, posZ);
        params.setSize(STEERING_LENGTH, STEERING_DIAMETER);
        params.setOrientation(0.0f, -(float)Math.PI/2, 0.0f);
        params.setType(EVRepJointTypes.REVOLUTE);
        params.setMode(EVRepJointModes.FORCE);
        params.setMotorEnabled(true);
        params.setCyclic(true);
        params.setMaximumForce(10000.0f);
        
        params.setControlLoopEnabled(true);
        
        return creator.createJoint(params);
    }

    private  int createDamper(VRepObjectCreation creator, int physicalBodyHandle, String name, float posX, float posY, float posZ, boolean front) throws VRepException
    {
        JointParameters params = new JointParameters();
        params.setName(name);
        params.setPosition(posX, posY, posZ);
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setSize(DAMPER_LENGTH, DAMPER_DIAMETER);
        params.setType(EVRepJointTypes.PRISMATIC);
        params.setMode(EVRepJointModes.FORCE);
        params.setInterval(new float[] {-0.5f, 1.0f});
        params.setMotorEnabled(true);
        params.setTargetVelocity(0.5f);
        params.setMaximumForce(20000.0f);
        params.setTargetPosition(front ? -0.05f : -0.04f);
        params.setControlLoopEnabled(true);
        params.setSpringDamperMode(true);
        params.setSpringConstantK(20000.0f);
        params.setDampingCoefficientC(1000.0f);

        int springDamperHandle = creator.createJoint(params);
        creator.setParentForChild(physicalBodyHandle, springDamperHandle, false);
//        TODO (haebor) reintroduce or replace with some sort of activation of spring damper behaviour
//        creator.attachSpringDamperScript(springDamperHandle);
        return springDamperHandle;
    }

    public float getVehicleHeight()
    {
        return _carHeight;
    }


//  public static void main(String[] args)
//  {
//      try
//      {
//          _vrep = VRepRemoteAPI.INSTANCE;
//          _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
//          VRepObjectCreation creator = new VRepObjectCreation(_vrep, _clientID);
//          float baseLength = (float) (VWPoloDimensions.getWheelbase() + 100.0)/1000;
//          float baseWidth = (float) (VWPoloDimensions.getWidth() - 100)/1000;
//          
//          float height = 0.2f;
//          int physicalBodyHandle = createCarBody(creator, baseLength, baseWidth, height);
//          
//          int damperRearLeft = createDamper(creator, physicalBodyHandle, "damperRearLeft", (float)-baseWidth/2 + DAMPER_INSET, (float)-baseLength/2 + 0.1f, DAMPER_LENGTH/4 + height/2, false);
//          int damperRearRight = createDamper(creator, physicalBodyHandle, "damperRearRight", (float)baseWidth/2 - DAMPER_INSET, (float)-baseLength/2 + 0.1f, DAMPER_LENGTH/4 + height/2, false);
//          int damperFrontLeft = createDamper(creator, physicalBodyHandle, "damperFrontLeft", (float)-baseWidth/2 + DAMPER_INSET, (float)baseLength/2 - 0.1f, DAMPER_LENGTH/4 + height/2, true);
//          int damperFrontRight = createDamper(creator, physicalBodyHandle, "damperFrontRight", (float)baseWidth/2 - DAMPER_INSET, (float)baseLength/2 - 0.1f, DAMPER_LENGTH/4 + height/2, true);
//
//          int steeringFrontLeft = createSteering(creator, "steeringFrontLeft", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
//          int steeringFrontRight = createSteering(creator, "steeringFrontRight", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
//          
//          createConnector(creator, damperFrontLeft, steeringFrontLeft, "connectorDflSfl", 0.0f, 0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
//          createConnector(creator, damperFrontRight, steeringFrontRight, "connectorDfrSfr", 0.0f, 0.0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
//          
//          int motorFrontLeft = createMotor(creator, "motorFrontLeft", 0.0f, 0.0f, 0.0f);
//          int motorFrontRight = createMotor(creator, "motorFrontRight", 0.0f, 0.0f, 0.0f);
//          
//          createConnector(creator, steeringFrontLeft, motorFrontLeft, "connectorSflMfl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
//          createConnector(creator, steeringFrontRight, motorFrontRight, "connectorSfrMfr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
//          
//          int axisRearLeft = createAxis(creator, "axisRearLeft", 0.0f, 0.0f, 0.0f);
//          int axisRearRight = createAxis(creator, "axisRearRight", 0.0f, 0.0f, 0.0f);
//          
//          createConnector(creator, damperRearLeft, axisRearLeft, "connectorDrlArl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
//          createConnector(creator, damperRearRight, axisRearRight, "connectorDrrArr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
//          
//          createWheel(creator, "frontLeftWheel", motorFrontLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
//          createWheel(creator, "frontRightWheel", motorFrontRight, WHEEL_DIAMETER, WHEEL_WIDTH);
//          createWheel(creator, "rearLeftWheel", axisRearLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
//          createWheel(creator, "rearRightWheel", axisRearRight, WHEEL_DIAMETER, WHEEL_WIDTH);
//
//          CarControl car1 = new CarControl(creator, PHYSICAL_CAR_BODY_NAME, _vrep, _clientID, physicalBodyHandle);
//          car1.initialize();
//          _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
//          
////          ReadingKeyboardinput keyboardReader = null;
////          try
////          {
////              keyboardReader = ReadingKeyboardinput.getKeystrokeProvider();
////              char nextChar = '.';
////              while(nextChar != 'q')
////              {
////                  nextChar = keyboardReader.getNextChar();
////                  switch (nextChar)
////                  {
////                      case 'e':
////                          car1.drive(3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
////                          break;
////                      case 'a':
//////                          car1.drive(3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
////                          break;
////                      case 'f':
//////                          car1.drive(3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
////                          break;
////                      case 'v':
////                          car1.drive(-3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
////                          break;
////                      default:
////                          break;
////                  }
//////                  car1.drive(3*20*Math.PI/180, 0.0*2*Math.PI/180);
////              }
////          }
////          catch (InterruptedException | ExecutionException e1)
////          {
////              e1.printStackTrace();
////          }
////
////          vrep.simxStopSimulation(clientID, remoteApi.simx_opmode_blocking);
////          vrep.simxFinish(clientID);
////          if(keyboardReader != null)
////          {
////              keyboardReader.close();
////          }
//          System.out.println("done");
//      }
//      catch (VRepException e)
//      {
//          e.printStackTrace();
//      }
//  }
}
