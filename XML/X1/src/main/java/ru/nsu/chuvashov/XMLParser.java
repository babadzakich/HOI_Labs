package ru.nsu.chuvashov;

import javax.xml.stream.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * StAX-based XML parser for reading person data from XML file
 */
public class XMLParser {
    
    /**
     * Parse XML file and return list of PersonData objects (may contain duplicates/partial data)
     */
    public List<PersonData> parseXML(String filePath) throws XMLStreamException, IOException {
        List<PersonData> persons = new ArrayList<>();
        
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (InputStream fis = getClass().getClassLoader().getResourceAsStream(filePath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
            
            PersonData currentPerson = null;
            String currentElement = null;
            StringBuilder textContent = new StringBuilder();
            
            while (reader.hasNext()) {
                int event = reader.next();
                
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        
                        if ("person".equals(elementName)) {
                            currentPerson = new PersonData();
                            
                            // Check for attributes on person element
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                String attrName = reader.getAttributeLocalName(i);
                                String attrValue = reader.getAttributeValue(i);
                                
                                if ("id".equals(attrName)) {
                                    currentPerson.id = attrValue;
                                } else if ("name".equals(attrName)) {
                                    parseFullName(attrValue, currentPerson);
                                }
                            }
                        } else if (currentPerson != null) {
                            currentElement = elementName;
                            textContent.setLength(0);
                            
                            // Process element attributes
                            processElementAttributes(reader, currentPerson, elementName);
                        }
                        break;
                        
                    case XMLStreamConstants.CHARACTERS:
                        if (currentElement != null) {
                            textContent.append(reader.getText());
                        }
                        break;
                        
                    case XMLStreamConstants.END_ELEMENT:
                        elementName = reader.getLocalName();
                        
                        if ("person".equals(elementName)) {
                            if (currentPerson != null) {
                                persons.add(currentPerson);
                                currentPerson = null;
                            }
                        } else if (currentPerson != null && currentElement != null) {
                            processElementText(currentElement, textContent.toString().trim(), currentPerson);
                            currentElement = null;
                        }
                        break;
                }
            }
            
            reader.close();
        }
        
        return persons;
    }
    
    private void processElementAttributes(XMLStreamReader reader, PersonData person, String elementName) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attrName = reader.getAttributeLocalName(i);
            String attrValue = reader.getAttributeValue(i);
            
            switch (elementName) {
                case "id":
                    if ("value".equals(attrName)) {
                        person.id = attrValue;
                    }
                    break;
                case "firstname":
                    if ("value".equals(attrName)) {
                        person.firstName = attrValue;
                    }
                    break;
                case "surname":
                    if ("value".equals(attrName)) {
                        person.lastName = attrValue;
                    }
                    break;
                case "gender":
                    if ("value".equals(attrName)) {
                        person.gender = parseGender(attrValue);
                    }
                    break;
                case "wife":
                case "husband":
                case "spouce":
                case "spouse":
                    if ("value".equals(attrName)) {
                        person.spouse = attrValue;
                    }
                    break;
                case "parent":
                    if ("value".equals(attrName)) {
                        parseParent(attrValue, person);
                    }
                    break;
                case "siblings":
                    if ("val".equals(attrName)) {
                        parseSiblings(attrValue, person);
                    }
                    break;
                case "son":
                    if ("id".equals(attrName)) {
                        person.sons.add(attrValue);
                    }
                    break;
                case "daughter":
                    if ("id".equals(attrName)) {
                        person.daughters.add(attrValue);
                    }
                    break;
                case "brother":
                    if ("id".equals(attrName)) {
                        person.brothers.add(attrValue);
                    }
                    break;
                case "sister":
                    if ("id".equals(attrName)) {
                        person.sisters.add(attrValue);
                    }
                    break;
                case "children-number":
                    if ("value".equals(attrName)) {
                        try {
                            person.expectedChildrenCount = Integer.parseInt(attrValue);
                        } catch (NumberFormatException e) {
                            // Ignore invalid numbers
                        }
                    }
                    break;
                case "siblings-number":
                    if ("value".equals(attrName)) {
                        try {
                            person.expectedSiblingsCount = Integer.parseInt(attrValue);
                        } catch (NumberFormatException e) {
                            // Ignore invalid numbers
                        }
                    }
                    break;
            }
        }
    }
    
    private void processElementText(String elementName, String text, PersonData person) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        switch (elementName) {
            case "firstname":
            case "first":
                person.firstName = text;
                break;
            case "surname":
            case "family":
                person.lastName = text;
                break;
            case "gender":
                person.gender = parseGender(text);
                break;
            case "mother":
                person.mother = text;
                break;
            case "father":
                person.father = text;
                break;
            case "parent":
                parseParent(text, person);
                break;
            case "brother":
                person.brothers.add(text);
                break;
            case "sister":
                person.sisters.add(text);
                break;
        }
    }
    
    private Gender parseGender(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        value = value.trim().toUpperCase();
        
        if (value.equals("M") || value.equals("MALE")) {
            return Gender.MALE;
        } else if (value.equals("F") || value.equals("FEMALE")) {
            return Gender.FEMALE;
        }
        
        return null;
    }
    
    private void parseParent(String value, PersonData person) {
        if (value == null || value.equals("UNKNOWN") || value.trim().isEmpty()) {
            return;
        }
        
        value = value.trim();
        
        // If it's a person ID (starts with P), we can't determine if it's mother or father
        // Store as generic parent - will be resolved later if possible
        if (value.startsWith("P")) {
            // This is an ID reference - we'll handle this in post-processing
            // For now, just skip as we don't know the gender
            return;
        }
        
        // If it's a name, try to infer from context (this is imperfect)
        // We'll just store it as a parent and hope other entries clarify
    }
    
    private void parseSiblings(String value, PersonData person) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        
        // Siblings can be space-separated IDs
        String[] siblingIds = value.trim().split("\\s+");
        for (String siblingId : siblingIds) {
            if (!siblingId.isEmpty() && siblingId.startsWith("P")) {
                // We don't know gender yet, will be resolved in post-processing
                // For now, we need to store them somewhere
                // We'll add a method to handle unresolved siblings
                // Temporarily add as brothers (will be corrected later)
                person.brothers.add(siblingId);
            }
        }
    }
    
    private void parseFullName(String fullName, PersonData person) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return;
        }
        
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            person.firstName =(parts[0]);
            person.lastName =(parts[parts.length - 1]);
        } else if (parts.length == 1) {
            person.firstName =(parts[0]);
        }
    }
}
