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
    requires transitive java.xml.bind;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.hamcrest;
    requires java.desktop;
	requires transitive com.fasterxml.jackson.core;
}
