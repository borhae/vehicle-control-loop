function sysCall_init() 
    h=sim.getObjectAssociatedWithScript(sim.handle_self)
    dh=sim.addDrawingObject(sim.drawing_lines,2,0,h,2,nil,nil,nil,{1,0,0})
    m=sim.getObjectMatrix(h,-1)
    pt1={0,0,20}
    pt2={0,0,-20}
    pt1=sim.multiplyVector(m,pt1)
    pt2=sim.multiplyVector(m,pt2)
    lineData={pt1[1],pt1[2],pt1[3],pt2[1],pt2[2],pt2[3]}
    sim.addDrawingObjectItem(dh,lineData)
end

function sysCall_cleanup() 
    sim.removeDrawingObject(dh)
end 
