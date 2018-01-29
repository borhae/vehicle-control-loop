package de.joachim.haensel.vehicle;

import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehiclecontrol.reactive.CarControl;
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

    public static void main(String[] args)
    {
        try
        {
            VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
            int clientID;
            clientID = vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
            VRepObjectCreation creator = new VRepObjectCreation(vrep, clientID);
            float baseLength = (float) (VWPoloDimensions.getWheelbase() + 100.0)/1000;
            float baseWidth = (float) (VWPoloDimensions.getWidth() - 100)/1000;
            
            float height = 0.2f;
            int physicalBodyHandle = createCarBody(creator, baseLength, baseWidth, height);
            
            int damperRearLeft = createDamper(creator, physicalBodyHandle, "damperRearLeft", (float)-baseWidth/2 + DAMPER_INSET, (float)-baseLength/2 + 0.1f, DAMPER_LENGTH/4 + height/2, false);
            int damperRearRight = createDamper(creator, physicalBodyHandle, "damperRearRight", (float)baseWidth/2 - DAMPER_INSET, (float)-baseLength/2 + 0.1f, DAMPER_LENGTH/4 + height/2, false);
            int damperFrontLeft = createDamper(creator, physicalBodyHandle, "damperFrontLeft", (float)-baseWidth/2 + DAMPER_INSET, (float)baseLength/2 - 0.1f, DAMPER_LENGTH/4 + height/2, true);
            int damperFrontRight = createDamper(creator, physicalBodyHandle, "damperFrontRight", (float)baseWidth/2 - DAMPER_INSET, (float)baseLength/2 - 0.1f, DAMPER_LENGTH/4 + height/2, true);

            int steeringFrontLeft = createSteering(creator, "steeringFrontLeft", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
            int steeringFrontRight = createSteering(creator, "steeringFrontRight", - STEERING_LENGTH/2.0f, 0.0f, 0.0f);
            
            createConnector(creator, damperFrontLeft, steeringFrontLeft, "connectorDflSfl", 0.0f, 0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
            createConnector(creator, damperFrontRight, steeringFrontRight, "connectorDfrSfr", 0.0f, 0.0f, -DAMPER_LENGTH/2.0f, STEERING_DIAMETER * 2.0f, STEERING_DIAMETER * 1.7f);
            
            int motorFrontLeft = createMotor(creator, "motorFrontLeft", 0.0f, 0.0f, 0.0f);
            int motorFrontRight = createMotor(creator, "motorFrontRight", 0.0f, 0.0f, 0.0f);
            
            createConnector(creator, steeringFrontLeft, motorFrontLeft, "connectorSflMfl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            createConnector(creator, steeringFrontRight, motorFrontRight, "connectorSfrMfr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            
            int axisRearLeft = createAxis(creator, "axisRearLeft", 0.0f, 0.0f, 0.0f);
            int axisRearRight = createAxis(creator, "axisRearRight", 0.0f, 0.0f, 0.0f);
            
            createConnector(creator, damperRearLeft, axisRearLeft, "connectorDrlArl", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            createConnector(creator, damperRearRight, axisRearRight, "connectorDrrArr", 0.0f, 0.0f, - STEERING_LENGTH/2.0f, STEERING_DIAMETER * 1.8f, STEERING_DIAMETER * 1.8f);
            
            createWheel(creator, "frontLeftWheel", motorFrontLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
            createWheel(creator, "frontRightWheel", motorFrontRight, WHEEL_DIAMETER, WHEEL_WIDTH);
            createWheel(creator, "rearLeftWheel", axisRearLeft, WHEEL_DIAMETER, WHEEL_WIDTH);
            createWheel(creator, "rearRightWheel", axisRearRight, WHEEL_DIAMETER, WHEEL_WIDTH);

            CarControl car1 = new CarControl(creator, PHYSICAL_CAR_BODY_NAME, vrep, clientID, physicalBodyHandle);
            car1.initialize();
            vrep.simxStartSimulation(clientID, remoteApi.simx_opmode_blocking);
            
//            ReadingKeyboardinput keyboardReader = null;
//            try
//            {
//                keyboardReader = ReadingKeyboardinput.getKeystrokeProvider();
//                char nextChar = '.';
//                while(nextChar != 'q')
//                {
//                    nextChar = keyboardReader.getNextChar();
//                    switch (nextChar)
//                    {
//                        case 'e':
//                            car1.drive(3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
//                            break;
//                        case 'a':
////                            car1.drive(3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
//                            break;
//                        case 'f':
////                            car1.drive(3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
//                            break;
//                        case 'v':
//                            car1.drive(-3 * 20 * Math.PI / 180, 0.0 * 2 * Math.PI / 180);
//                            break;
//                        default:
//                            break;
//                    }
////                    car1.drive(3*20*Math.PI/180, 0.0*2*Math.PI/180);
//                }
//            }
//            catch (InterruptedException | ExecutionException e1)
//            {
//                e1.printStackTrace();
//            }
// 
//            vrep.simxStopSimulation(clientID, remoteApi.simx_opmode_blocking);
//            vrep.simxFinish(clientID);
//            if(keyboardReader != null)
//            {
//                keyboardReader.close();
//            }
            System.out.println("done");
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }

    private static int createCarBody(VRepObjectCreation creator, float baseLength, float baseWidth, float height)
            throws VRepException
    {
        ShapeParameters carBodyParams = new ShapeParameters();
        carBodyParams.setName(PHYSICAL_CAR_BODY_NAME);
        carBodyParams.setPosition(-3f, 0, height + 0.1f);
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

    private static int createWheel(VRepObjectCreation creator, String name, int parent, float diameter, float width) throws VRepException
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

    private static int createAxis(VRepObjectCreation creator, String name, float x, float y, float z) throws VRepException
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

    private static int createMotor(VRepObjectCreation creator, String name, float x, float y, float z) throws VRepException
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

    private static int createConnector(VRepObjectCreation creator, int parent, int child, String name, float x, float y, float z, float diameter, float height) throws VRepException
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

    private static int createSteering(VRepObjectCreation creator, String name, float posX, float posY, float posZ) throws VRepException
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

    private static int createDamper(VRepObjectCreation creator, int physicalBodyHandle, String name, float posX, float posY, float posZ, boolean front) throws VRepException
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
}
