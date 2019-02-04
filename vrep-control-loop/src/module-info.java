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
}
