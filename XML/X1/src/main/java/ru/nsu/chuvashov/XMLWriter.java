package ru.nsu.chuvashov;

import javax.xml.stream.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class XMLWriter {
    
    public void writeXML(Map<String, PersonData> personMap, String outputPath)
            throws XMLStreamException, IOException {
        
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(fos, "UTF-8");
            
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            
            writer.writeStartElement("people");
            writer.writeAttribute("count", String.valueOf(personMap.size()));
            writer.writeCharacters("\n");
            
            for (PersonData person : personMap.values()) {
                writePerson(writer, person);
            }
            
            writer.writeEndElement();
            writer.writeCharacters("\n");
            
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
    }
    
    private void writePerson(XMLStreamWriter writer, PersonData person) throws XMLStreamException {
        writer.writeCharacters("  ");
        writer.writeStartElement("person");
        
        if (person.id != null) {
            writer.writeAttribute("id", person.id);
        }
        
        writer.writeCharacters("\n");
        
        if (person.firstName != null) {
            writeElement(writer, "firstName", person.firstName, 4);
        }
        
        if (person.lastName != null) {
            writeElement(writer, "lastName", person.lastName, 4);
        }
        
        if (person.gender != null) {
            writeElement(writer, "gender", person.gender.name().toLowerCase(), 4);
        }
        
        if (person.spouse != null) {
            writeElement(writer, "spouse", person.spouse, 4);
        }
        
        if (person.mother != null || person.father != null) {
            writer.writeCharacters("    ");
            writer.writeStartElement("parents");
            writer.writeCharacters("\n");
            
            if (person.mother != null) {
                writeElement(writer, "mother", person.mother, 6);
            }
            
            if (person.father != null) {
                writeElement(writer, "father", person.father, 6);
            }
            
            writer.writeCharacters("    ");
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
        
        if (!person.sons.isEmpty() || !person.daughters.isEmpty()) {
            writer.writeCharacters("    ");
            writer.writeStartElement("children");
            
            int totalChildren = person.sons.size() + person.daughters.size();
            writer.writeAttribute("count", String.valueOf(totalChildren));
            writer.writeCharacters("\n");
            
            for (String sonId : person.sons) {
                writeElement(writer, "son", sonId, 6);
            }
            
            for (String daughterId : person.daughters) {
                writeElement(writer, "daughter", daughterId, 6);
            }
            
            writer.writeCharacters("    ");
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
        
        if (!person.brothers.isEmpty() || !person.sisters.isEmpty()) {
            writer.writeCharacters("    ");
            writer.writeStartElement("siblings");
            
            int totalSiblings = person.brothers.size() + person.sisters.size();
            writer.writeAttribute("count", String.valueOf(totalSiblings));
            writer.writeCharacters("\n");
            
            for (String brotherId : person.brothers) {
                writeElement(writer, "brother", brotherId, 6);
            }
            
            for (String sisterId : person.sisters) {
                writeElement(writer, "sister", sisterId, 6);
            }
            
            writer.writeCharacters("    ");
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
        
        writer.writeCharacters("  ");
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }
    
    private void writeElement(XMLStreamWriter writer, String name, String value, int indent) 
            throws XMLStreamException {
        String indentStr = " ".repeat(indent);
        writer.writeCharacters(indentStr);
        writer.writeStartElement(name);
        writer.writeCharacters(value);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }
}
