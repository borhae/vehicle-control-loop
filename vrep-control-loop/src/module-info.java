/**
 * 
 */
/**
 * @author dummy
 *
 */
module vrepvehicle
{
    exports de.joachim.haensel.vehiclecreation;
    exports de.joachim.haensel.sumo2vrep;
    exports de.joachim.haensel.vehicle;
    exports de.joachim.haensel.vrepshapecreation;
    exports de.joachim.haensel.vrepshapecreation.shapes;
    exports de.joachim.haensel.vwpoloproperties;
    exports de.joachim.haensel.vrepshapecreation.joints;
    exports de.joachim.haensel.vrepshapecreation.parameters;
    exports de.joachim.haensel.vehiclecontrol.reactive;
    exports sumobindings;

    requires transitive coppelialib;
    requires java.xml;
    requires transitive java.xml.bind;
    requires junit;
}