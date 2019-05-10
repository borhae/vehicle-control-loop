package de.joachim.haensel.phd.scenario.sumobindings.tostring;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import sumobindings.JunctionType;

public class Sumobindings
{
    public static String toString(JunctionType junction)
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(JunctionType.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            QName qName = new QName("bla.bla.bla", "junction");
            JAXBElement<JunctionType> root = new JAXBElement<JunctionType>(qName, JunctionType.class, junction);
            StringWriter writer = new StringWriter();
            marshaller.marshal(root, writer);
            return writer.toString();
        } 
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
