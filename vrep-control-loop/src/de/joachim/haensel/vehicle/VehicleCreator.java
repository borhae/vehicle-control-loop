package de.joachim.haensel.vehicle;

import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.CarControlInterface;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.dummy.DummyParameters;
import de.joachim.haensel.vrepshapecreation.joints.EVRepJointModes;
import de.joachim.haensel.vrepshapecreation.joints.EVRepJointTypes;
import de.joachim.haensel.vrepshapecreation.joints.JointParameters;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import de.joachim.haensel.vwpoloproperties.VWPoloDimensions;

public class VehicleCreator
{

    private static final String PHYSICAL_CAR_BODY_NAME = "physicalCarBody";

    private static float REAR_AXIS_DIAMETER = 0.05f;
    private static float REAR_AXIS_LENGTH = 0.2f;
    
    private static float MOTOR_SIZE_DIAMETER = 0.05f;
    private static float MOTOR_SIZE_LENGTH = 0.2f;

    private static float MAIN_BODY_MASS = 1000.0f;
    private static float WHEEL_MASS = 10.0f;
    
    private static float WHEEL_WIDTH = 0.2f;
    private static float WHEEL_DIAMETER = 0.5f;

    private static float STEERING_DIAMETER = 0.1f;
    private static float STEERING_LENGTH = 0.25f;
    
    private static float DAMPER_DIAMETER = 0.075f;
    private static float DAMPER_LENGTH = 0.3f;
    private static float DAMPER_INSET_X = DAMPER_DIAMETER/2.5f;
    private static float DAMPER_INSET_Y = 0.1f;
    private static float DAMPER_DAMPING_COEFFICIENT_C = 1000.0f;
    private static float DAMPER_SPRING_CONSTANT_K = 20000.0f;
    private static float DAMPER_INTERVAL_MAX = 1.0f;
    private static float DAMPER_INTERVAL_MIN = -0.5f;
    private static float DAMPER_TARGET_POSITION_BACK = -0.04f;
    private static float DAMPER_TARGET_POSITION_FRONT = -0.05f;
    
    private static VRepRemoteAPI _vrep;
    private  int _clientID;
    private float _carHeight;
    private VRepObjectCreation _objectCreator;
    private float _scaleFactor;
    
    public VehicleCreator(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, float scaleFactor)
    {
        _scaleFactor = scaleFactor;
        _vrep = vrep;
        _clientID = clientID;
        _carHeight = 0.2f;
        _carHeight *= scaleFactor;
        _objectCreator = objectCreator;
        WHEEL_WIDTH *= scaleFactor;
        WHEEL_DIAMETER *= scaleFactor;
        STEERING_DIAMETER *= scaleFactor;
        STEERING_LENGTH *= scaleFactor;
        DAMPER_DIAMETER *= scaleFactor;
        DAMPER_LENGTH *= scaleFactor;
        DAMPER_INSET_X *= scaleFactor;
        DAMPER_INSET_Y *= scaleFactor;
        MOTOR_SIZE_DIAMETER *= scaleFactor;
        MOTOR_SIZE_LENGTH *= scaleFactor;
        REAR_AXIS_DIAMETER *= scaleFactor;
        REAR_AXIS_LENGTH *= scaleFactor;

//        WHEEL_MASS *= scaleFactor; // TODO should we mess with this? what are the effects
//        MAIN_BODY_MASS *= scaleFactor; // TODO should we mess with this? what are the effects
//        DAMPER_DAMPING_COEFFICIENT_C *= scaleFactor; // TODO should we mess with this? what are the effects
//        DAMPER_SPRING_CONSTANT_K *= scaleFactor; // TODO should we mess with this? what are the effects
        WHEEL_MASS *= 1.0; // TODO experiment: what is the right amount?
        MAIN_BODY_MASS *= 1.0; // TODO experiment: what is the right amount?
        DAMPER_DAMPING_COEFFICIENT_C *= 1.0; // TODO experiment: what is the right amount?
        DAMPER_SPRING_CONSTANT_K *= 1.0; // TODO experiment: what is the right amount?

        DAMPER_INTERVAL_MAX *= scaleFactor;
        DAMPER_INTERVAL_MIN *= scaleFactor;
//        DAMPER_TARGET_POSITION_BACK *= scaleFactor; // TODO should we mess with this? what are the effects
//        DAMPER_TARGET_POSITION_FRONT *= scaleFactor; // TODO should we mess with this? what are the effects
        DAMPER_TARGET_POSITION_BACK *= 1.0; // TODO experiment: what is the right amount?
        DAMPER_TARGET_POSITION_FRONT *= 1.0; // TODO experiment: what is the right amount?
    }

    public Vehicle createAt(float x, float y, float z, RoadMap roadMap, IUpperLayerFactory uppperLayerFactory, ILowerLayerFactory lowerLayerFactory)
    {
        try
        {
            VehicleHandles vehicleHandles = new VehicleHandles();

            float baseLength = (float) (VWPoloDimensions.getWheelbase() + 100.0)/1000;
            baseLength *= _scaleFactor;
            float baseWidth = (float) (VWPoloDimensions.getWidth() - 100)/1000;
            baseWidth *= _scaleFactor;
            
            int physicalBodyHandle = createCarBody(_objectCreator, baseLength, baseWidth, _carHeight, x, y, z);
            
            int damperRearLeft = createDamper(_objectCreator, physicalBodyHandle, "damperRearLeft", (float)-baseWidth/2 + DAMPER_INSET_X, (float)-baseLength/2 + DAMPER_INSET_Y, DAMPER_LENGTH/4 + _carHeight/2, false);
            int damperRearRight = createDamper(_objectCreator, physicalBodyHandle, "damperRearRight", (float)baseWidth/2 - DAMPER_INSET_X, (float)-baseLength/2 + DAMPER_INSET_Y, DAMPER_LENGTH/4 + _carHeight/2, false);
            int damperFrontLeft = createDamper(_objectCreator, physicalBodyHandle, "damperFrontLeft", (float)-baseWidth/2 + DAMPER_INSET_X, (float)baseLength/2 - DAMPER_INSET_Y, DAMPER_LENGTH/4 + _carHeight/2, true);
            int damperFrontRight = createDamper(_objectCreator, physicalBodyHandle, "damperFrontRight", (float)baseWidth/2 - DAMPER_INSET_X, (float)baseLength/2 - DAMPER_INSET_Y, DAMPER_LENGTH/4 + _carHeight/2, true);
            int rearWheelDummy = createObjectAttachedVisualization(_objectCreator, physicalBodyHandle, "rearWheelVisualization", 0.0f, (float)-baseLength/2 + DAMPER_INSET_Y, 0.0f, (float) (Math.PI/2.0), (float) (Math.PI/2.0), 0.0f);

            int steeringFrontLeft = createSteering(_objectCreator, "steeringFrontLeft", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
            int steeringFrontRight = createSteering(_objectCreator, "steeringFrontRight", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
            
            createConnector(_objectCreator, damperFrontLeft, steeringFrontLeft, "connectorDflSfl", 0.0f, 0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
            createConnector(_objectCreator, damperFrontRight, steeringFrontRight, "connectorDfrSfr", 0.0f, 0.0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
            
            int motorFrontLeft = createMotor(_objectCreator, "motorFrontLeft", 0.0f, 0.0f, 0.0f);
            int motorFrontRight = createMotor(_objectCreator, "motorFrontRight", 0.0f, 0.0f, 0.0f);
            
            int connectorSflMfl = createConnector(_objectCreator, steeringFrontLeft, motorFrontLeft, "connectorSflMfl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            int connectorSfrMfr = createConnector(_objectCreator, steeringFrontRight, motorFrontRight, "connectorSfrMfr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            int frontLeftWheelDummy = createObjectAttachedVisualization(_objectCreator, connectorSflMfl, "frontLeftWheelDummy", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            int frontRightWheelDummy = createObjectAttachedVisualization(_objectCreator, connectorSfrMfr, "frontRightWheelDummy", 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            
            int axisRearLeft = createAxis(_objectCreator, "axisRearLeft", 0.0f, 0.0f, 0.0f);
            int axisRearRight = createAxis(_objectCreator, "axisRearRight", 0.0f, 0.0f, 0.0f);
            
            createConnector(_objectCreator, damperRearLeft, axisRearLeft, "connectorDrlArl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            createConnector(_objectCreator, damperRearRight, axisRearRight, "connectorDrrArr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            
            int frontLeftWheel = createWheel(_objectCreator, "frontLeftWheel", motorFrontLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
            int frontRightWheel = createWheel(_objectCreator, "frontRightWheel", motorFrontRight, WHEEL_DIAMETER, WHEEL_WIDTH);
            int rearLeftWheel = createWheel(_objectCreator, "rearLeftWheel", axisRearLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
            int rearRightWheel = createWheel(_objectCreator, "rearRightWheel", axisRearRight, WHEEL_DIAMETER, WHEEL_WIDTH);

            CarControlInterface car = new CarControlInterface(_objectCreator, PHYSICAL_CAR_BODY_NAME, _vrep, _clientID, physicalBodyHandle);
            
            vehicleHandles.setPhysicalBody(physicalBodyHandle).setRearLeftWheel(rearLeftWheel).setRearRightWheel(rearRightWheel).setFrontLeftWheel(frontLeftWheel).setFrontRightWheel(frontRightWheel);
            vehicleHandles.setRearWheelVisualizationDummy(rearWheelDummy);
            car.initialize();
            return new Vehicle(_objectCreator, _vrep, _clientID, vehicleHandles, car, roadMap, uppperLayerFactory, lowerLayerFactory);
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private int createObjectAttachedVisualization(VRepObjectCreation objectCreator, int parentHandle, String name, float posX, float posY, float posZ, float alpha, float beta, float gamma) throws VRepException
    {
        DummyParameters params = new DummyParameters();
        params.setName(name);
        params.setPosition(posX, posY, posZ);
        params.setOrientation(alpha, beta, gamma);
        params.setSize(0.05f);
        int dummyHandle = objectCreator.createDummy(params);
        objectCreator.setParentForChild(parentHandle, dummyHandle, false);
        objectCreator.attachVisualizationScript(dummyHandle);
        return dummyHandle;
    }

    private int createCarBody(VRepObjectCreation creator, float baseLength, float baseWidth, float height, float posX, float posY, float posZ)
            throws VRepException
    {
        ShapeParameters carBodyParams = new ShapeParameters();
        carBodyParams.setName(PHYSICAL_CAR_BODY_NAME);
        carBodyParams.setPosition(posX, posY, posZ);
        carBodyParams.setOrientation(0.0f, 0.0f, 0.0f);
        carBodyParams.setSize(baseWidth, baseLength, height);
        carBodyParams.setMass(MAIN_BODY_MASS);
        carBodyParams.setType(EVRepShapes.CUBOID);
        carBodyParams.setIsRespondable(true);
        carBodyParams.setRespondableMask(ShapeParameters.GLOBAL_ONLY_RESPONDABLE_MASK);
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
        params.setMass(WHEEL_MASS);
        params.setType(EVRepShapes.CYLINDER);
        params.setIsRespondable(true);
        params.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        params.setIsDynamic(true);
        int wheel = creator.createPrimitive(params);
        creator.setParentForChild(parent, wheel, false);
        return wheel;
    }

    private  int createAxis(VRepObjectCreation creator, String name, float x, float y, float z) throws VRepException
    {
        JointParameters params = new JointParameters();
        params.setPosition(x, y, z);
        params.setSize(REAR_AXIS_LENGTH, REAR_AXIS_DIAMETER);
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
        params.setSize(MOTOR_SIZE_LENGTH, MOTOR_SIZE_DIAMETER);
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
        params.setInterval(new float[] {DAMPER_INTERVAL_MIN, DAMPER_INTERVAL_MAX});
        params.setMotorEnabled(true);
        params.setTargetVelocity(0.5f);
        params.setMaximumForce(20000.0f);
        params.setTargetPosition(front ? DAMPER_TARGET_POSITION_FRONT : DAMPER_TARGET_POSITION_BACK);
        params.setControlLoopEnabled(true);
        params.setSpringDamperMode(true);
        params.setSpringConstantK(DAMPER_SPRING_CONSTANT_K);
        params.setDampingCoefficientC(DAMPER_DAMPING_COEFFICIENT_C);

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
