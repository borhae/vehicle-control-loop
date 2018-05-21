createdObjects = {}

deleteCreated = function(inInts, inFloats, inStrings, inBuffer)
 sim.addStatusbarMessage("createdObjects is this:"..table_to_string(createdObjects))
  
	while(next(createdObjects)) do
	  local handleToRemove = table.remove(createdObjects)
	  sim.addStatusbarMessage("about to delete object with handle:"..handleToRemove)
	  local name = sim.getObjectName(handleToRemove)
	  sim.addStatusbarMessage("with name: "..name)
		sim.removeObject(handleToRemove)
	end
	return {}, {}, {}, "" 
end

addAllToDeletionList = function(inInts, inFloats, inStrings, inBuffer)
  for _, curHandle in ipairs(inInts) do
    addToDeletionList(curHandle, "addAllToDeletionList")
  end
  return {}, {}, {}, "" 
end

addToDeletionList = function(objectHandle, callerName)
    local name = sim.getObjectName(objectHandle)
    sim.addStatusbarMessage(callerName.."wants to add handle to deletion list: "..objectHandle..", with name: "..name)
  table.insert(createdObjects, objectHandle)
end

simxGetScriptAssociatedWithObject = function(inInts, inFloats, inStrings, inBuffer)
  return {sim.getScriptAssociatedWithObject(inInts[1])}, {}, {}, "" 
end

simxRemoveScript = function(inInts, inFloats, inStrings, inBuffer)
  sim.removeScript(inInts[1])
  return {}, {}, {}, "" 
end

createEdge = function(inInts, inFloats, inStrings, inBuffer)
--      simAddStatusbarMessage("Creating edge at: "..table_to_string(inFloats))
      local x1 = inFloats[1]
      local y1 = inFloats[2]
      local x2 = inFloats[3]
      local y2 = inFloats[4]
      local width = inFloats[6]
      local height = inFloats[7]
      local dx = x2 - x1
      local dy = y2 - y1
      
      local length = math.sqrt(dx * dx + dy * dy)
      
      local xPos = dx/2 + x1
      local yPos = dy/2 + y1
      local position = {xPos, yPos, height/2}      
      
      local atanBase = dy/dx
      local angle = math.atan(atanBase) + (math.pi /2)
      -- SHAPE
      local CUBOID = 0
      local SPHERE = 1
      local CYLINDER = 2
      local CONE = 3
      -- OPTIONS
      local BACK_CULL = 1 -- bit 0 backface culling
      local EDGE_VIS = 2  -- bit 1 edges visible
      local SHAPE_SMO = 4 -- bit 2 shape is smoothed 
      local SHAPE_RESP = 8 -- bit 3 shape is respondable
      local SHAPE_STAT = 16 -- bit 4 shape is static
      local CYL_OE = 32 -- bit 5 cylinder has open ends
      local testObjSize = {width, length, height}
      local objectHandle = sim.createPureShape(CUBOID, EDGE_VIS + SHAPE_RESP + SHAPE_STAT, testObjSize, 0, nil)
      
--    POSITION
      local posResult = sim.setObjectPosition(objectHandle, sim_handle_parent, position)

--    ORIENTATION
      sim.setObjectOrientation(objectHandle, -1, {0.0, 0.0, angle})
      
--    NAMING
      local equalXSign = x1/math.abs(x1) == x2/math.abs(x2)
      local equalYSign = y1/math.abs(y1) == y2/math.abs(y2)
      local signInf = tostring(equalXSign)..tostring(equalYSign)
      
      sim.setObjectName(objectHandle, inStrings[1])
      sim.addStatusbarMessage(inStrings[1].."; "..x1.."; "..y1.."; "..x2.."; "..y2.."; "..dx.."; "..dy.."; "..atanBase.."; "..angle.."; "..length)

      addToDeletionList(objectHandle, "createEdge") -- for later removal
      return {}, {}, {}, "" 
end

createLine = function(inInts, inFloats, inStrings, inBuffer)
--      simAddStatusbarMessage("Creating edge at: "..table_to_string(inFloats))
      local x1 = inFloats[1]
      local y1 = inFloats[2]
      local x2 = inFloats[3]
      local y2 = inFloats[4]
      local width = inFloats[6]
      local height = inFloats[7]
      local red = inFloats[8]
      local green = inFloats[9]
      local blue = inFloats[10]

      local dx = x2 - x1
      local dy = y2 - y1
      
      local length = math.sqrt(dx * dx + dy * dy)
      
      local xPos = dx/2 + x1
      local yPos = dy/2 + y1
      local position = {xPos, yPos, height/2}      
      
      local atanBase = dy/dx
      local angle = math.atan(atanBase) + (math.pi /2)
      -- SHAPE
      local CUBOID = 0
      local SPHERE = 1
      local CYLINDER = 2
      local CONE = 3
      -- OPTIONS
      local BACK_CULL = 1 -- bit 0 backface culling
      local EDGE_VIS = 2  -- bit 1 edges visible
      local SHAPE_SMO = 4 -- bit 2 shape is smoothed 
      local SHAPE_RESP = 8 -- bit 3 shape is respondable
      local SHAPE_STAT = 16 -- bit 4 shape is static
      local CYL_OE = 32 -- bit 5 cylinder has open ends
      local testObjSize = {width, length, height}
      local objectHandle = sim.createPureShape(CUBOID, EDGE_VIS + SHAPE_RESP + SHAPE_STAT, testObjSize, 0, nil)
      
--    POSITION
      local posResult = sim.setObjectPosition(objectHandle, sim_handle_parent, position)

--    ORIENTATION
      sim.setObjectOrientation(objectHandle, -1, {0.0, 0.0, angle})
      
--	  COLOR
      sim.setShapeColor(objectHandle, null, sim.colorcomponent_ambient_diffuse, {red, green, blue})
      
--    NAMING
      local equalXSign = x1/math.abs(x1) == x2/math.abs(x2)
      local equalYSign = y1/math.abs(y1) == y2/math.abs(y2)
      local signInf = tostring(equalXSign)..tostring(equalYSign)
      
      sim.setObjectName(objectHandle, inStrings[1])
      sim.addStatusbarMessage(inStrings[1].."; "..x1.."; "..y1.."; "..x2.."; "..y2.."; "..dx.."; "..dy.."; "..atanBase.."; "..angle.."; "..length)

      addToDeletionList(objectHandle, "createLine") -- for later removal
      return {}, {}, {}, "" 
end

createJunction = function(inInts, inFloats, inStrings, inBuffer)
      local coordinates = inFloats
      local width = inFloats[4] * 4
      local height = inFloats[5]
      -- SHAPE
      local CUBOID = 0
      local SPHERE = 1
      local CYLINDER = 2
      local CONE = 3
      -- OPTIONS
      local BACK_CULL = 1 -- bit 0 backface culling
      local EDGE_VIS = 2  -- bit 1 edges visible
      local SHAPE_SMO = 4 -- bit 2 shape is smoothed 
      local SHAPE_RESP = 8 -- bit 3 shape is respondable
      local SHAPE_STAT = 16 -- bit 4 shape is static
      local CYL_OE = 32 -- bit 5 cylinder has open ends
      local testObjSize = {width, width, height}
      local objectHandle = sim.createPureShape(CYLINDER, EDGE_VIS + SHAPE_RESP + SHAPE_STAT, testObjSize, 0, nil)
      sim.setObjectName(objectHandle, inStrings[1])

      sim.addStatusbarMessage("Creating junction at: "..table_to_string(coordinates))
      local posResult = sim.setObjectPosition(objectHandle, sim_handle_parent, {coordinates[1], coordinates[2], height/2})

      addToDeletionList(objectHandle, "createJunction") -- for later removal
      return {}, {}, {}, "" 
end

createCenter = function(inInts, inFloats, inStrings, inBuffer)
      -- SHAPE
      local CUBOID = 0
      local SPHERE = 1
      local CYLINDER = 2
      local CONE = 3
      -- OPTIONS
      local BACK_CULL = 1 -- bit 0 backface culling
      local EDGE_VIS = 2  -- bit 1 edges visible
      local SHAPE_SMO = 4 -- bit 2 shape is smoothed 
      local SHAPE_RESP = 8 -- bit 3 shape is respondable
      local SHAPE_STAT = 16 -- bit 4 shape is static
      local CYL_OE = 32 -- bit 5 cylinder has open ends
      local testObjSize = {0.1, 0.1, 0.05}
      local objectHandle = sim.createPureShape(CYLINDER, EDGE_VIS + SHAPE_RESP + SHAPE_STAT, testObjSize, 0, nil)
      sim.setShapeColor(objectHandle, null, sim_colorcomponent_ambient_diffuse, {200, 0, 0})
      sim.addStatusbarMessage("Creating center marker at coordinates (0, 0, 0): "..table_to_string({0, 0, 0}))
      
      local relativeToObjHandle =  sim_handle_parent
      local coordinates = {0, 0, 0}
      sim.addStatusbarMessage("objectHandle:"..type(objectHandle)..", relativeToObjHandle:"..type(relativeToObjHandle)..", coordinates:"..type(coordinates))
      local posResult = sim.setObjectPosition(objectHandle, relativeToObjHandle, coordinates)

      addToDeletionList(objectHandle, "createCenter") -- for later removal
      return {}, {}, {}, "" 
end

createPrimitive = function(inInts, inFloats, inStrings, inBuffer)
      local position = {inFloats[1], inFloats[2], inFloats[3]}
      local angle = {inFloats[4], inFloats[5], inFloats[6]}
      local size = {inFloats[7], inFloats[8], inFloats[9]}
      local mass = inFloats[10]
      
      local shape = inInts[1] -- 0 for a cuboid, 1 for a sphere, 2 for a cylinder and 3 for a coned
      local isDynamic = inInts[2] -- 1 for yes, 0 for no
      local isRespondable = inInts[3] -- 1 for yes, 0 for no
      local respondableMask = inInts[4] -- a bit mask for local and global respondability
      local visibilityLayerMask = inInts[5] -- a bit mask defining the layers on which this object will be camera visible
      
      
      local name = inStrings[1]
      -- OPTIONS
      local BACK_CULL = 1 -- bit 0 backface culling
      local EDGE_VIS = 2  -- bit 1 edges visible
      local SHAPE_SMO = 4 -- bit 2 shape is smoothed 
      local SHAPE_RESP = 8 -- bit 3 shape is respondable
      local SHAPE_STAT = 16 -- bit 4 shape is static
      local CYL_OE = 32 -- bit 5 cylinder has open ends 
      
      local shapeIsStatic = 0
      if isDynamic == 1 then 
        shapeIsStatic = 0 
      else 
        shapeIsStatic = SHAPE_STAT 
      end
      
      local shapeIsRespondable = 0
      if isRespondable == 1 then 
          shapeIsRespondable = SHAPE_RESP 
      else 
          shapeIsRespondable = 0 
      end

      local objectHandle = simCreatePureShape(shape, EDGE_VIS + shapeIsStatic + shapeIsRespondable, size, mass, nil)
--    POSITION
      local posResult = simSetObjectPosition(objectHandle, sim_handle_parent, position)
      
--    RESPONDABLE MASK
      simSetObjectInt32Parameter(objectHandle, sim_shapeintparam_respondable_mask, respondableMask)
      
--    CAMERA VISIBILITY       
      sim.setObjectInt32Parameter(objectHandle, sim.objintparam_visibility_layer, visibilityLayerMask)            
      
      
--    ORIENTATION
      simSetObjectOrientation(objectHandle, -1, angle)
--    NAMING
      simSetObjectName(objectHandle, name)
      addToDeletionList(objectHandle, "createPrimitive") -- for later removal
      
      return {objectHandle}, {}, {}, "" 
end

createJoint = function(inInts, inFloats, inStrings, inBuffer)
      local position = {inFloats[1], inFloats[2], inFloats[3]}
      local angle = {inFloats[4], inFloats[5], inFloats[6]}
      local size = {inFloats[7], inFloats[8]}
      local interval = {inFloats[9], inFloats[10]}
      local targetVelocity = inFloats[11]
      local maximumForce = inFloats[12]
      local targetPosition = inFloats[13]
      
      local jointType = inInts[1] -- sim_joint_revolute_subtype, sim_joint_prismatic_subtype or sim_joint_spherical_subtype
      local jointMode = inInts[2] -- sim_jointmode_passive, sim_jointmode_motion_deprecated, sim_jointmode_ik, sim_jointmode_ikdependent, sim_jointmode_dependent, sim_jointmode_force
      local isCyclicInt = inInts[3]
      local isCyclic = false
      if isCyclicInt == 0 then
        isCyclic = false
      else
        isCyclic = true
      end
      local options = 0;
      
      local name = inStrings[1]
--    no colors, just staying with the default ones
      local objectHandle = simCreateJoint(jointType, jointMode, options, size, nil, nil)
      
--    cyclic?      
      simAddStatusbarMessage("is cyclic? "..tostring(isCyclic))
      simAddStatusbarMessage("float 9 "..inFloats[9])
      simAddStatusbarMessage("float 10 "..inFloats[10])
      simAddStatusbarMessage("interval: ("..table_to_string(interval)..")")
      
      simSetJointInterval(objectHandle, isCyclic, interval)
--    POSITION
      local posResult = simSetObjectPosition(objectHandle, sim_handle_parent, position)

--    ORIENTATION
      simSetObjectOrientation(objectHandle, -1, angle)
      
--    TARGET VELOCITY
      simSetJointTargetVelocity(objectHandle, targetVelocity)      
      
--    TARGET POSITION
      simSetJointTargetPosition(objectHandle, targetPosition)
      
--    MAXIMUM FORCE
      simSetJointForce(objectHandle, maximumForce)      
--    NAMING
      simSetObjectName(objectHandle, name)
      
      addToDeletionList(objectHandle, "createJoint") -- for later removal
      
      return {objectHandle}, {}, {}, "" 
end

setParentForChild = function(inInts, inFloats, inStrings, inBuffer)
  local childHandle = inInts[2]
  local parentHandle = inInts[1]
  local keepInPlaceInt = inInts[3]
  local keepInPlace = false
  if keepInPlaceInt == 1 then 
    keepInPlace = true;
  end   
  
  simSetObjectParent(childHandle, parentHandle, keepInPlace)
  return {objectHandle}, {}, {}, "" 
end

createDummy = function(inInts, inFloats, inStrings, inBuffer)
      local position = {inFloats[1], inFloats[2], inFloats[3]}
      local angle = {inFloats[4], inFloats[5], inFloats[6]}
      local size = inFloats[7]
      
      local name = inStrings[1]

      simAddStatusbarMessage("values set, creating dummy")
      local objectHandle = sim.createDummy(size, nil)
      simAddStatusbarMessage("created, now positioning dummy")
--    POSITION
      local posResult = simSetObjectPosition(objectHandle, sim_handle_parent, position)
      simAddStatusbarMessage("positioned, now giving orientation")

--    ORIENTATION
      simSetObjectOrientation(objectHandle, -1, angle)
      simAddStatusbarMessage("oriented, now giving it a name")
--    NAMING
      simAddStatusbarMessage("name: "..name)

      simSetObjectName(objectHandle, name)
      simAddStatusbarMessage("name added, now returning")
      addToDeletionList(objectHandle, "createDummy") -- for later removal
      
      return {objectHandle}, {}, {}, "" 
end


setIntParameter = function(inInts, inFloats, inStrings, inBuffer)
  local objectID = inInts[1]
  local parameterID = inInts[2]
  local value = inInts[3]

  simSetObjectInt32Parameter(objectID, parameterID, value)
  return {objectHandle}, {}, {}, ""   
end 

setFloatParameter = function(inInts, inFloats, inStrings, inBuffer)
  local objectID = inInts[1]
  local parameterID = inInts[2]
  local value = inFloats[1]

  simSetObjectFloatParameter(objectID, parameterID, value)
  return {objectHandle}, {}, {}, "" 
end 

addAndAttachScript = function(inInts, inFloats, inStrings, inBuffer)
--  sim.addStatusbarMessage("about to attach script")

  local objectHandle = inInts[1]
  local scriptType = inInts[2]
  local scriptContent = inStrings[1]
  
  local scriptHandle = sim.addScript(scriptType + sim_scripttype_threaded)
  
  sim.setScriptText(scriptHandle, scriptContent)
  
  sim.associateScriptWithObject(scriptHandle, objectHandle)
  
  return {scriptHandle}, {}, {}, "" 
end

addAndAttachScriptNonThreaded = function(inInts, inFloats, inStrings, inBuffer)
--  sim.addStatusbarMessage("about to attach script")

  local objectHandle = inInts[1]
  local scriptType = inInts[2]
  local scriptContent = inStrings[1]
  
  local scriptHandle = sim.addScript(scriptType)
  
  sim.setScriptText(scriptHandle, scriptContent)
  
  sim.associateScriptWithObject(scriptHandle, objectHandle)
  
  return {scriptHandle}, {}, {}, "" 
end

textureOnRectangle = function(inInts, inFloats, inStrings, inBuffer)
	local handle = inInts[1]
	local textureName = inStrings[1]
	local textureFileName = inStrings[2]
	sim.addStatusbarMessage("absolute filename: "..textureFileName)
	
	local textureShapeHandle = sim.createTexture(textureFileName, 2, {1, 1}, nil, nil, 0, nil)
	if (textureShapeHandle ~= -1) then
		local textureId = sim.getShapeTextureId(textureShapeHandle)
		if (textureId ~= -1) then
			if (handle ~= -1) then
			    -- I have no idea why the 400, 400 scaling delivers the required result 
				sim.setShapeTexture(handle, textureId, sim.texturemap_plane, 1, {5225, 5225}, nil, nil)
			end
		end
	end
	addToDeletionList(textureShapeHandle, "textureOnRectangle")
	return {}, {}, {}, "" 
end

createMesh = function(inInts, inFloats, inStrings, inBuffer)
	local indices = inInts
	local meshHandle = sim.createMeshShape(2, 20*math.pi/180, inFloats, inInts)
	sim.setObjectName(meshHandle, inStrings[1])
  addToDeletionList(meshHandle, "createMeshHandle")
	return {}, {}, {}, "" 
end

createMeshInSimulation = function(inInts, inFloats, inStrings, inBuffer)
  local indices = inInts
  local meshHandle = sim.createMeshShape(2, 20*math.pi/180, inFloats, inInts)
  sim.setObjectName(meshHandle, inStrings[1])
  return {}, {}, {}, "" 
end

table_to_string = function(tbl) -- Added for debugging, Convert a lua table into a lua syntactically correct string
    local result = "{"
    for k, v in pairs(tbl) do
        -- Check the key type (ignore any numerical keys - assume its an array)
        if type(k) == "string" then
            result = result.."[\""..k.."\"]".."="
        end

        -- Check the value type
        if type(v) == "table" then
            result = result..table_to_string(v)
        elseif type(v) == "boolean" then
            result = result..tostring(v)
        else
            result = result.."\""..v.."\""
        end
        result = result..","
    end
    -- Remove leading commas from the result
    if result ~= "" then
        result = result:sub(1, result:len()-1)
    end
    return result.."}"
end

-- ------------------------- TODO: THIS SHOULD GO TO IT'S OWN FILE (simulation control) --------------

simulationState =  function(inInts, inFloats, inStrings, inBuffer)
  local simulationState = sim.getSimulationState()
  return {simulationState}, {}, {}, "" 
end

-- ------------------------- TODO: THIS SHOULD GO TO IT'S OWN FILE (debugging) --------------

createDrawingObjectLine = function(inInts, inFloats, inStrings, inBuffer)
	local size = 2
	local emissiveColor = {inFloats[1], inFloats[2], inFloats[3]}
	local handle = sim.addDrawingObject(sim.drawing_lines, size, 0, -1, 2, nil, nil, nil, emissiveColor)
	return {handle}, {}, {}, "" 
end

createDrawingObjectCircle = function(inInts, inFloats, inStrings, inBuffer)
	local emissiveColor = {inFloats[1], inFloats[2], inFloats[3]}
	local pointSize = 0.01
	local handle = sim.addDrawingObject(sim.drawing_spherepoints, pointSize, 0, -1, 99999999, nil, nil, nil, emissiveColor)
	return {handle}, {}, {}, ""
end

removeDrawingObject = function(inInts, inFloats, inStrings, inBuffer)
	local handle = inInts[1]
	sim.removeDrawingObject(handle)
	return {}, {}, {}, "" 
end

redrawLine = function(inInts, inFloats, inStrings, inBuffer)
	local handle = inInts[1]
	local x1 = inFloats[1]
	local y1 = inFloats[2]
	local z1 = inFloats[3]
	local x2 = inFloats[4]
	local y2 = inFloats[5]
	local z2 = inFloats[6]

	sim.addDrawingObjectItem(handle, nil)
	sim.addDrawingObjectItem(handle, {x1, y1, z1, x2, y2, z2})
	
	return {}, {}, {}, "" 
end

redrawCircle = function(inInts, inFloats, inStrings, inBuffer)
	local handle = inInts[1]
	sim.addDrawingObjectItem(handle, nil)
	local pointDistance = 0.5
	local centerX = inFloats[1]
	local centerY = inFloats[2]
	local centerZ = inFloats[3]
	local radius = inFloats[4]
	
	local currentPoint = {0.0, 0.0, 0.0}
	currentPoint[3] = centerZ

	for idx = 0, 360, pointDistance do
		currentPoint[1] = centerX + radius * math.cos(idx * math.pi / 180)
		currentPoint[2] = centerY + radius * math.sin(idx * math.pi / 180)
		sim.addDrawingObjectItem(handle, currentPoint)
	end
	return {}, {}, {}, "" 
end
