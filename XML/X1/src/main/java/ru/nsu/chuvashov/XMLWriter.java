package ru.nsu.chuvashov;

import javax.xml.stream.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Writes structured person data to XML file
 */
public class XMLWriter {
    
    /**
     * Write consolidated person data to XML file
     */
    public void writeXML(Map<String, PersonData> personMap, String outputPath) 
            throws XMLStreamException, IOException {
        
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(fos, "UTF-8");
            
            // Start document
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            
            // Root element
            writer.writeStartElement("people");
            writer.writeAttribute("count", String.valueOf(personMap.size()));
            writer.writeCharacters("\n");
            
            // Write each person
            for (PersonData person : personMap.values()) {
                writePerson(writer, person);
            }
            
            // End root element
            writer.writeEndElement(); // people
            writer.writeCharacters("\n");
            
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
    }
    
    private void writePerson(XMLStreamWriter writer, PersonData person) throws XMLStreamException {
        writer.writeCharacters("  ");
        writer.writeStartElement("person");
        
        // Write ID attribute
        if (person.getId() != null) {
            writer.writeAttribute("id", person.getId());
        }
        
        writer.writeCharacters("\n");
        
        // Write basic information
        if (person.getFirstName() != null) {
            writeElement(writer, "firstName", person.getFirstName(), 4);
        }
        
        if (person.getLastName() != null) {
            writeElement(writer, "lastName", person.getLastName(), 4);
        }
        
        if (person.getGender() != null) {
            writeElement(writer, "gender", person.getGender().name().toLowerCase(), 4);
        }
        
        // Write spouse
        if (person.getSpouse() != null) {
            writeElement(writer, "spouse", person.getSpouse(), 4);
        }
        
        // Write parents
        if (person.getMother() != null || person.getFather() != null) {
            writer.writeCharacters("    ");
            writer.writeStartElement("parents");
            writer.writeCharacters("\n");
            
            if (person.getMother() != null) {
                writeElement(writer, "mother", person.getMother(), 6);
            }
            
            if (person.getFather() != null) {
                writeElement(writer, "father", person.getFather(), 6);
            }
            
            writer.writeCharacters("    ");
            writer.writeEndElement(); // parents
            writer.writeCharacters("\n");
        }
        
        // Write children
        if (!person.getSons().isEmpty() || !person.getDaughters().isEmpty()) {
            writer.writeCharacters("    ");
            writer.writeStartElement("children");
            
            int totalChildren = person.getSons().size() + person.getDaughters().size();
            writer.writeAttribute("count", String.valueOf(totalChildren));
            writer.writeCharacters("\n");
            
            // Write sons
            for (String sonId : person.getSons()) {
                writeElement(writer, "son", sonId, 6);
            }
            
            // Write daughters
            for (String daughterId : person.getDaughters()) {
                writeElement(writer, "daughter", daughterId, 6);
            }
            
            writer.writeCharacters("    ");
            writer.writeEndElement(); // children
            writer.writeCharacters("\n");
        }
        
        // Write siblings
        if (!person.getBrothers().isEmpty() || !person.getSisters().isEmpty()) {
            writer.writeCharacters("    ");
            writer.writeStartElement("siblings");
            
            int totalSiblings = person.getBrothers().size() + person.getSisters().size();
            writer.writeAttribute("count", String.valueOf(totalSiblings));
            writer.writeCharacters("\n");
            
            // Write brothers
            for (String brotherId : person.getBrothers()) {
                writeElement(writer, "brother", brotherId, 6);
            }
            
            // Write sisters
            for (String sisterId : person.getSisters()) {
                writeElement(writer, "sister", sisterId, 6);
            }
            
            writer.writeCharacters("    ");
            writer.writeEndElement(); // siblings
            writer.writeCharacters("\n");
        }
        
        writer.writeCharacters("  ");
        writer.writeEndElement(); // person
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
