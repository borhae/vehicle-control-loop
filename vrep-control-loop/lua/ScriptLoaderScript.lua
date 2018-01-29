-- This is a customization script. It is intended to be used to customize a scene in
-- various ways, mainly when simulation is not running. When simulation is running,
-- do not use customization scripts, but rather child scripts if possible

-- Variable sim_call_type is handed over from the system

-- DO NOT WRITE CODE OUTSIDE OF THE if-then-end SECTIONS BELOW!! (unless the code is a function definition)

if (sim_call_type==sim_customizationscriptcall_initialization) then
    -- this is called just after this script was created (or reinitialized)
    -- Do some initialization here

    -- By default we disable customization script execution during simulation, in order
    -- to run simulations faster:
    simSetScriptAttribute(sim_handle_self,sim_customizationscriptattribute_activeduringsimulation,false)
    PORT = 19999

    objectHandle = -2
    simAddStatusbarMessage("set handle to -2")
    simExtRemoteApiStart(PORT)
end

if (sim_call_type==sim_customizationscriptcall_nonsimulation) then
    -- This is called on a regular basis when simulation is not running.
    -- This is where you would typically write the main code of
    -- a customization script
    -- show that we have been loaded properly    
    if(objectHandle == -2) then
      createCubeElement()
    end
end

createCubeElement = function()
      simAddStatusbarMessage("creating cube") 
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
      local testObjSize = {0.1, 0.1, 0.1}
      objectHandle = simCreatePureShape(CUBOID, EDGE_VIS + SHAPE_RESP + SHAPE_STAT, testObjSize, 0, nil)
      -- Function didn't produce expected return values, i.e. an int table, a float table, a string table and a buffer string. (simCallScriptFunctionEx on createRoadElement@RoadCreation)
      return {}, {}, {}, "" 
end

loadCode = function(inInts, inFloats, inStrings, inBuffer)
  if #inStrings>=1 then
     -- Execute the code stored in inStrings[1]:
    return {},{},{loadstring(inStrings[1])()},'' -- return a string that contains the return value of the code execution
  end
end

if (sim_call_type==sim_customizationscriptcall_lastbeforesimulation) then
    -- This is called just before a simulation starts
end

if (sim_call_type==sim_customizationscriptcall_simulationactuation) then
    -- This is called by default from the main script, in the "actuation" phase.
    -- but only if you have previously not disabled this script to be active during
    -- simulation (see the script's initialization code above)
end

if (sim_call_type==sim_customizationscriptcall_simulationsensing) then
    -- This is called by default from the main script, in the "sensing" phase,
    -- but only if you have previously not disabled this script to be active during
    -- simulation (see the script's initialization code above)
end

if (sim_call_type==sim_customizationscriptcall_simulationpausefirst) then
    -- This is called just after entering simulation pause
end

if (sim_call_type==sim_customizationscriptcall_simulationpause) then
    -- This is called on a regular basis when simulation is paused
end

if (sim_call_type==sim_customizationscriptcall_simulationpauselast) then
    -- This is called just before leaving simulation pause
end

if (sim_call_type==sim_customizationscriptcall_firstaftersimulation) then
    -- This is called just after a simulation ended
end

if (sim_call_type==sim_customizationscriptcall_lastbeforeinstanceswitch) then
    -- This is called just before an instance switch (switch to another scene)
end

if (sim_call_type==sim_customizationscriptcall_firstafterinstanceswitch) then
    -- This is called just after an instance switch (switch to another scene)
end

if (sim_call_type==sim_customizationscriptcall_cleanup) then
    -- this is called just before this script gets destroyed (or reinitialized) 
    -- Do some clean-up here
    simExtRemoteApiStop(PORT)
end
