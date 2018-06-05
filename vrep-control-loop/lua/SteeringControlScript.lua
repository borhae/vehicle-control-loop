function sysCall_threadmain()
  init()
  controlLoop()
end

controlLoop = function()
    while simGetSimulationState()~=sim_simulation_advancing_abouttostop do
      -- Steering (Ackermann steering):
      steeringAngleLeft = math.atan(l/(-d+l/math.tan(desiredSteeringAngle)))
      steeringAngleRight = math.atan(l/(d+l/math.tan(desiredSteeringAngle)))
      sim.setJointTargetPosition(steeringLeft,steeringAngleLeft)
      sim.setJointTargetPosition(steeringRight,steeringAngleRight)
  
      -- Wheel rotation speed:
      sim.setJointTargetVelocity(motorLeft,desiredWheelRotSpeed)
      sim.setJointTargetVelocity(motorRight,desiredWheelRotSpeed)
  
      -- Compute current location stats for reading from external control:
      local fLWP = sim.getObjectPosition(frontLeftWheel, -1)
      local fRWP = sim.getObjectPosition(frontRightWheel, -1)
  
      local rLWP = sim.getObjectPosition(rearLeftWheel, -1)
      local rRWP = sim.getObjectPosition(rearRightWheel, -1)
  
      local posi = sim.getObjectPosition(physicalBody, -1)
      local v, av = sim.getObjectVelocity(physicalBody) 
  
      local rWCP = {(rLWP[1] + rRWP[1])/2, (rLWP[2] + rRWP[2])/2}
      local fWCP = {(fLWP[1] + fRWP[1])/2, (fLWP[2] + fRWP[2])/2}
      local carPos = {posi[1], posi[2]}
      -- car center position, front wheel center position, rear wheel center position (2D), velocity (3D)
      positions = {carPos[1], carPos[2], fWCP[1], fWCP[2], rWCP[1], rWCP[2], v[1], v[2], v[3]}
  
      -- Since this script is threaded, don't waste time here:
      simSwitchThread() -- Resume the script at next simulation loop start
  end
end

init = function()
  -- Retrieving handles and setting initial values:
  steeringLeft = sim.getObjectHandle('steeringFrontLeft')
  steeringRight = sim.getObjectHandle('steeringFrontRight')

  motorLeft = sim.getObjectHandle('motorFrontLeft')
  motorRight = sim.getObjectHandle('motorFrontRight')

  frontLeftWheel = sim.getObjectHandle('frontLeftWheel')
  frontRightWheel = sim.getObjectHandle('frontRightWheel')
  rearLeftWheel = sim.getObjectHandle('rearLeftWheel')
  rearRightWheel = sim.getObjectHandle('rearRightWheel')

  physicalBody = sim.getObjectHandle('physicalCarBody')

  desiredSteeringAngle = 0
  desiredWheelRotSpeed = 0

  local outerDistWheels = -1
  local wheelsThickness = -1
  errVal, wheelThickMin = sim.getObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_min_z)
  errVal, wheelThickMax = sim.getObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_max_z)

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
    simAddStatusbarMessage("error while trying to read left-right wheel distance: "..errVal)
  end
  
  local lengthFR = -1 
  local wheelDiam = -1 
  
  errVal, wheelDiamMin = sim.getObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_min_x)
  errVal, wheelDiamMax = sim.getObjectFloatParameter(frontLeftWheel, sim_objfloatparam_objbbox_max_x)
  
  if errVal == 0 or 1 then
    wheelDiam = wheelDiamMax - wheelDiamMin
  else
    simAddStatusbarMessage("error while trying to read wheel diameter: "..errVal)
  end
  
  local middleDistWheelsFR = -1
  local outerDistWheelsFR = -1
  errVal, distanceData = sim.checkDistance(frontRightWheel, rearRightWheel, 100)
  if errVal == 0 or 1 then
    outerDistWheelsFR = distanceData[7]
    middleDistWheelsFR = outerDistWheelsFR + wheelDiam
  else
    sim.addStatusbarMessage("error while trying to read front-back wheel distance: "..errVal)
  end
  
  --d = 0.755 -- 2*d = distance between left and right wheels
  d = middleDistWheels / 2 -- 2*d = distance between left and right wheels
  --l = 2.5772 -- l = distance between front and rear wheels
  l = middleDistWheelsFR -- l = distance between front and rear wheels
  positions = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
end

control = function(inInts, inFloats, inStrings, inBuffer)
  local targetAngle = inFloats[1]
  local targetVelocity = inFloats[2]
  
  -- simAddStatusbarMessage("setting new steering val: "..targetAngle..", was "..desiredSteeringAngle.." before")
  
  desiredSteeringAngle = targetAngle
  desiredWheelRotSpeed = targetVelocity
  return {}, {}, {}, "" 
end

sense = function(inInts, inFloats, inStrings, inBuffer)
  -- positions computed in main loop, provided for external usage
  if (positions == nil) then
    positions = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
  end 
  return {}, {positions[1], positions[2], positions[3], positions[4], positions[5], positions[6], positions[7], positions[8], positions[9]}, {}, "" 
end

debugCircle = function(inInts, inFloats, inStrings, inBuffer)
	local pointSize = 0.05
  local parentHandle = inInts[1]
  local emissiveColor = {inFloats[2], inFloats[3], inFloats[4]}
  local handle = sim.addDrawingObject(sim.drawing_spherepoints, pointSize, 0, parentHandle, 99999999, nil, nil, nil, emissiveColor)

	sim.addDrawingObjectItem(handle, nil)
	local pointDistance = 0.5
  local parentHandlePos = sim.getObjectPosition(parentHandle, -1)
  
  local centerX = parentHandlePos[1]
  local centerY = parentHandlePos[2]
  local centerZ = parentHandlePos[3]
  local radius = inFloats[1]
	
	local currentPoint = {0.0, 0.0, 0.0}
	currentPoint[3] = centerZ

	for idx = 0, 360, pointDistance do
		currentPoint[1] = centerX + radius * math.cos(idx * math.pi / 180)
		currentPoint[2] = centerY + radius * math.sin(idx * math.pi / 180)
		sim.addDrawingObjectItem(handle, currentPoint)
	end
  return {}, {}, {}, "" 
end 
