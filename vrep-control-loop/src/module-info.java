/**
 * @author dummy
 *
 */
module vrepvehicle
{
    exports de.joachim.haensel.vehiclecreation;
    exports de.joachim.haensel.vrepshapecreation;
    exports de.joachim.haensel.vrepshapecreation.shapes;
    exports de.joachim.haensel.vwpoloproperties;
    exports de.joachim.haensel.vrepshapecreation.joints;
    exports de.joachim.haensel.vrepshapecreation.parameters;
    exports sumobindings;

    opens sumobindings to java.xml.bind;
    opens de.joachim.haensel.phd.scenario.vehicle.navigation to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.math.geometry to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.profile.collection to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.vehicle.experiment to com.fasterxml.jackson.databind;
    
    requires transitive coppelialib;
    requires java.xml;
    requires java.xml.bind;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.hamcrest;
    requires java.desktop;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires java.lsh;
}
