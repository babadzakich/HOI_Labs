package ru.nsu.chuvashov.jaxb;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Root element for JAXB marshalling
 */
@XmlRootElement(name = "people")
@XmlAccessorType(XmlAccessType.FIELD)
public class People {

    @XmlAttribute(name = "count")
    public int count;

    @XmlElement(name = "person")
    public List<FinalPerson> people = new ArrayList<>();

    public People() {}

    public People(List<FinalPerson> people) {
        this.people = people;
        this.count = people.size();
    }
}
