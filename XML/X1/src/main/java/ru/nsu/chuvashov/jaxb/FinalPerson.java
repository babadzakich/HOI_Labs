package ru.nsu.chuvashov.jaxb;

import jakarta.xml.bind.annotation.*;
import ru.nsu.chuvashov.Gender;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"firstName", "familyName", "gender", "mother", "father", "husband", "wife", "brothers", "sisters", "sons", "daughters"})
public class FinalPerson {

    @XmlAttribute(name = "id")
    @XmlID
    public String id;

    @XmlElement(required = true)
    public String firstName;

    @XmlElement(required = true)
    public String familyName;

    @XmlElement
    public Gender gender;

    @XmlElement
    @XmlIDREF
    public FinalPerson mother;

    @XmlElement
    @XmlIDREF
    public FinalPerson father;

    @XmlElement
    @XmlIDREF
    public FinalPerson husband;

    @XmlElement
    @XmlIDREF
    public FinalPerson wife;

    @XmlElementWrapper(name = "brothers")
    @XmlElement(name = "brother")
    @XmlIDREF
    public List<FinalPerson> brothers = new ArrayList<>();

    @XmlElementWrapper(name = "sisters")
    @XmlElement(name = "sister")
    @XmlIDREF
    public List<FinalPerson> sisters = new ArrayList<>();

    @XmlElementWrapper(name = "sons")
    @XmlElement(name = "son")
    @XmlIDREF
    public List<FinalPerson> sons = new ArrayList<>();

    @XmlElementWrapper(name = "daughters")
    @XmlElement(name = "daughter")
    @XmlIDREF
    public List<FinalPerson> daughters = new ArrayList<>();

    public FinalPerson() {}

    public FinalPerson(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FinalPerson{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", gender=" + gender +
                '}';
    }
}
