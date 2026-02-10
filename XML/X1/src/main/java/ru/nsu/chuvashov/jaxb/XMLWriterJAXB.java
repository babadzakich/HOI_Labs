package ru.nsu.chuvashov.jaxb;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.xml.sax.SAXException;
import ru.nsu.chuvashov.PersonData;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.Map;

/**
 * Writes structured person data using JAXB with schema validation
 */
public class XMLWriterJAXB {

    private final JAXBContext context;
    private Schema schema;

    public XMLWriterJAXB() throws JAXBException {
        this.context = JAXBContext.newInstance(People.class);
    }

    /**
     * Load XSD schema for validation
     */
    public void loadSchema(String schemaPath) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schema = schemaFactory.newSchema(new File(schemaPath));
    }

    /**
     * Write person data to XML using JAXB with optional schema validation
     */
    public void writeXML(Map<String, PersonData> personDataMap, String outputPath) throws JAXBException {
        // Convert PersonData to FinalPerson
        Map<String, FinalPerson> finalPersonMap = ModelConverter.convertToFinalPersons(personDataMap);

        // Create root element
        People people = new People();
        people.people.addAll(finalPersonMap.values());
        people.count = people.people.size();

        // Marshall to XML
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        // Set schema for validation if loaded
        if (schema != null) {
            marshaller.setSchema(schema);
        }

        marshaller.marshal(people, new File(outputPath));
    }

    /**
     * Validate an existing XML file against the loaded schema
     */
    public void validateXML(String xmlPath) throws JAXBException {
        if (schema == null) {
            throw new IllegalStateException("Schema not loaded. Call loadSchema() first.");
        }

        var unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(schema);

        // This will throw an exception if validation fails
        unmarshaller.unmarshal(new File(xmlPath));
    }
}
