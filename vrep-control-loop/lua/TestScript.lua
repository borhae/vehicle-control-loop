createdObjects = {}

testSomething = function()
      simAddStatusbarMessage("Testing loaded code") 
      -- SHAPE
      local CUBOID = 0
      -- OPTIONS
      local EDGE_VIS = 2  -- bit 1 edges visible
      local SHAPE_RESP = 8 -- bit 3 shape is respondable
      local SHAPE_STAT = 16 -- bit 4 shape is static
      local testObjSize = {0.1, 0.1, 0.1}
      objectHandle = simCreatePureShape(CUBOID, EDGE_VIS + SHAPE_RESP + SHAPE_STAT, testObjSize, 0, nil)
      return {}, {}, {}, "" 
end


deleteCreated = function(inInts, inFloats, inStrings, inBuffer)
	while(next(createdObjects) ~= nil) do
		sim.removeObject(table.remove(createdObjects))
	end
	return {}, {}, {}, "" 
end
