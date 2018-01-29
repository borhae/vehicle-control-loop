-- This script is threaded! It is a very simple example of how Ackermann steering can be handled.
-- Normally, one would use a non-threaded script for that

threadFunction = function()
    while simGetSimulationState()~=sim_simulation_advancing_abouttostop do
        -- Read the keyboard messages (make sure the focus is on the main window, scene view):
        message,auxiliaryData = simGetSimulatorMessage()
        while message~=-1 do
            if (message==sim_message_keypress) then
                if (auxiliaryData[1]==2007) then
                    -- up key
                    desiredWheelRotSpeed=desiredWheelRotSpeed+wheelRotSpeedDx
                end
                if (auxiliaryData[1]==2008) then
                    -- down key
                    desiredWheelRotSpeed = desiredWheelRotSpeed-wheelRotSpeedDx
                end
                if (auxiliaryData[1]==2009) then
                    -- left key
                    desiredSteeringAngle = desiredSteeringAngle+steeringAngleDx
                    if (desiredSteeringAngle>45*math.pi/180) then
                        desiredSteeringAngle = 45*math.pi/180
                    end
                end
                if (auxiliaryData[1]==2010) then
                    -- right key
                    desiredSteeringAngle = desiredSteeringAngle-steeringAngleDx
                    if (desiredSteeringAngle<-45*math.pi/180) then
                        desiredSteeringAngle = -45*math.pi/180
                    end
                end
            end
            message,auxiliaryData = simGetSimulatorMessage()
        end

        -- Steering (Ackermann steering):
        steeringAngleLeft = math.atan(l/(-d+l/math.tan(desiredSteeringAngle)))
        steeringAngleRight = math.atan(l/(d+l/math.tan(desiredSteeringAngle)))
        simSetJointTargetPosition(steeringLeft,steeringAngleLeft)
        simSetJointTargetPosition(steeringRight,steeringAngleRight)

        -- Wheel rotation speed:
        simSetJointTargetVelocity(motorLeft,desiredWheelRotSpeed)
        simSetJointTargetVelocity(motorRight,desiredWheelRotSpeed)

        -- Since this script is threaded, don't waste time here:
        simSwitchThread() -- Resume the script at next simulation loop start
    end
end

control = function(inInts, inFloats, inStrings, inBuffer)
  local targetAngle = inFloats[1]
  local targetVelocity = inFloats[2]
  
  simAddStatusbarMessage("setting new steering val: "..targetAngle..", was "..desiredSteeringAngle.." before")
  
  desiredSteeringAngle = targetAngle
  desiredWheelRotSpeed = targetVelocity
  return {}, {}, {}, "" 
end

-- Initialization
-- Retrieving handles and setting initial values:
steeringLeft = simGetObjectHandle('steeringFrontLeft')
steeringRight = simGetObjectHandle('steeringFrontRight')

motorLeft = simGetObjectHandle('motorFrontLeft')
motorRight = simGetObjectHandle('motorFrontRight')

frontLeftWheel = simGetObjectHandle('frontLeftWheel')
frontRightWheel = simGetObjectHandle('frontRightWheel')
rearRightWheel = simGetObjectHandle('rearRightWheel')

desiredSteeringAngle = 0
desiredWheelRotSpeed = 0

steeringAngleDx = 2*math.pi/180
wheelRotSpeedDx = 20*math.pi/180

local outerDistWheels = -1
local wheelsThickness = -1
errVal, wheelThickMin = simGetObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_min_z)
errVal, wheelThickMax = simGetObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_max_z)

if errVal == 0 or 1 then
  wheelsThickness = wheelThickMax - wheelThickMin
else
  simAddStatusbarMessage("error while trying to read wheel thickness: "..errVal)
end

local middleDistWheels = -1
errVal, distanceData = simCheckDistance(frontLeftWheel, frontRightWheel, 100)
if errVal == 0 or 1 then
  outerDistWheels = distanceData[7]
  middleDistWheels = outerDistWheels + wheelsThickness
else
  simAddStatusbarMessage("error while trying to read distance: "..errVal)
end

local lengthFR = -1 
local wheelDiam = -1 

errVal, wheelDiamMin = simGetObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_min_x)
errVal, wheelDiamMax = simGetObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_max_x)

if errVal == 0 or 1 then
  wheelDiam = wheelDiamMax - wheelDiamMin
else
  simAddStatusbarMessage("error while trying to read wheel diameter: "..errVal)
end

local middleDistWheelsFR = -1
local outerDistWheelsFR = -1
errVal, distanceData = simCheckDistance(frontRightWheel, rearRightWheel, 100)
if errVal == 0 or 1 then
  outerDistWheelsFR = distanceData[7]
  middleDistWheelsFR = outerDistWheelsFR + wheelDiam
else
  simAddStatusbarMessage("error while trying to read distance: "..errVal)
end

--d = 0.755 -- 2*d = distance between left and right wheels
d = middleDistWheels / 2 -- 2*d = distance between left and right wheels
--l = 2.5772 -- l = distance between front and rear wheels
l = middleDistWheelsFR -- l = distance between front and rear wheels

-- Execution of regular thread code
res,err = xpcall(threadFunction, function(err) return debug.traceback(err) end)
if not res then
    simAddStatusbarMessage('Lua runtime error: '..err)
end
