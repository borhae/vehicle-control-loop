/**
 * @author dummy
 *
 */
module vrepvehicle
{
    exports de.joachim.haensel.phd.scenario.experiment.runner;
    
    exports de.joachim.haensel.phd.scenario.experiment.evaluation.database;
    exports de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;
    exports de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;
    
    exports de.joachim.haensel.vehiclecreation;
    exports de.joachim.haensel.vrepshapecreation;
    exports de.joachim.haensel.vrepshapecreation.shapes;
    exports de.joachim.haensel.vwpoloproperties;
    exports de.joachim.haensel.vrepshapecreation.joints;
    exports de.joachim.haensel.vrepshapecreation.parameters;
    exports sumobindings;
    
    exports de.joachim.haensel.phd.scenario.vehicle.navigation;
    exports de.joachim.haensel.phd.scenario.math.geometry;
    exports de.joachim.haensel.phd.scenario.profile.equivalenceclasses;

    opens sumobindings to java.xml.bind;
    opens de.joachim.haensel.phd.scenario.math.geometry to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.vehicle.navigation to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.vehicle.experiment to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.profile.equivalenceclasses to com.fasterxml.jackson.databind;
    opens de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

    requires transitive coppelialib;
    requires jzy3d.api;
    requires jzy3d.jdt.core;
    requires typesafe.config;

    requires java.logging;
    
    requires java.xml;
    requires java.xml.bind;
    requires org.hamcrest;
    requires org.junit.jupiter;
    requires org.junit.jupiter.params;
    requires org.junit.platform.commons;
    requires java.desktop;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	
	requires org.mongodb.driver.sync.client;
	requires org.mongodb.driver.core;
	requires org.mongodb.bson;
}
