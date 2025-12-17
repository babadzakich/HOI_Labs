package ru.nsu.chuvashov;

import jakarta.xml.bind.annotation.*;
import java.util.*;

/**
 * Complete person data with all available information consolidated from multiple XML entries
 */
@XmlRootElement(name = "person")
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonData {
    @XmlAttribute
    public String id;
    
    @XmlElement
    public String firstName;
    
    @XmlElement
    public String lastName;
    
    @XmlElement
    public Gender gender;
    
    @XmlElement
    public String spouse;
    
    @XmlElement
    public String mother;
    
    @XmlElement
    public String father;
    
    @XmlTransient
    public Set<String> brothers = new HashSet<>();
    
    @XmlTransient
    public Set<String> sisters = new HashSet<>();
    
    public Set<String> sons = new HashSet<>();
    
    public Set<String> daughters = new HashSet<>();
    
    @XmlTransient
    public Integer expectedChildrenCount;
    
    @XmlTransient
    public Integer expectedSiblingsCount;
    
    public PersonData() {}
    
    public PersonData(String id) {
        this.id = id;
    }
    
    // Helper methods
    public int getActualChildrenCount() {
        return sons.size() + daughters.size();
    }
    
    public int getActualSiblingsCount() {
        return brothers.size() + sisters.size();
    }
    
    public Set<String> getAllChildren() {
        Set<String> allChildren = new HashSet<>();
        allChildren.addAll(sons);
        allChildren.addAll(daughters);
        return allChildren;
    }
    
    public Set<String> getAllSiblings() {
        Set<String> allSiblings = new HashSet<>();
        allSiblings.addAll(brothers);
        allSiblings.addAll(sisters);
        return allSiblings;
    }
    
    public List<String> getParents() {
        List<String> parents = new ArrayList<>();
        if (mother != null) parents.add(mother);
        if (father != null) parents.add(father);
        return parents;
    }
    
    /**
     * Merge data from another PersonData object into this one
     */
    public void merge(PersonData other) {
        if (other == null) return;
        
        if (id == null) id = other.id;
        if (firstName == null) firstName = other.firstName;
        if (lastName == null) lastName = other.lastName;
        if (gender == null) gender = other.gender;
        if (spouse == null) spouse = other.spouse;
        if (mother == null) mother = other.mother;
        if (father == null) father = other.father;
        if (expectedChildrenCount == null) expectedChildrenCount = other.expectedChildrenCount;
        if (expectedSiblingsCount == null) expectedSiblingsCount = other.expectedSiblingsCount;
        
        brothers.addAll(other.brothers);
        sisters.addAll(other.sisters);
        sons.addAll(other.sons);
        daughters.addAll(other.daughters);
    }
    
    @Override
    public String toString() {
        return "PersonData{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", spouse='" + spouse + '\'' +
                ", brothers=" + brothers.size() +
                ", sisters=" + sisters.size() +
                ", sons=" + sons.size() +
                ", daughters=" + daughters.size() +
                ", mother='" + mother + '\'' +
                ", father='" + father + '\'' +
                '}';
    }
}
