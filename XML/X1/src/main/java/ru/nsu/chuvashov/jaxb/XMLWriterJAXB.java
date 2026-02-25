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

public class XMLWriterJAXB {

    private final JAXBContext context;
    private Schema schema;

    public XMLWriterJAXB() throws JAXBException {
        this.context = JAXBContext.newInstance(People.class);
    }

    public void loadSchema(String schemaPath) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schema = schemaFactory.newSchema(new File(schemaPath));
    }

    public void writeXML(Map<String, PersonData> personDataMap, String outputPath) throws JAXBException {
        Map<String, FinalPerson> finalPersonMap = ModelConverter.convertToFinalPersons(personDataMap);

        People people = new People();
        people.people.addAll(finalPersonMap.values());
        people.count = people.people.size();

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        if (schema != null) {
            marshaller.setSchema(schema);
        }

        marshaller.marshal(people, new File(outputPath));
    }

    public void validateXML(String xmlPath) throws JAXBException {
        if (schema == null) {
            throw new IllegalStateException("Schema not loaded. Call loadSchema() first.");
        }

        var unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(schema);

        unmarshaller.unmarshal(new File(xmlPath));
    }
}
