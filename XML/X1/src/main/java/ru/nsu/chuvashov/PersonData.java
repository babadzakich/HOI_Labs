package ru.nsu.chuvashov;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

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
    public List<String> siblings = new ArrayList<>();
    
    @XmlTransient
    public List<String> children = new ArrayList<>();
    
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
    
    // Временные списки имен для разрешения связей
    @XmlTransient
    public List<String> childrenNames = new ArrayList<>();
    
    @XmlTransient
    public List<String> parentNames = new ArrayList<>();
    
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
        Set<String> allChildren = new HashSet<>(children);
        allChildren.addAll(sons);
        allChildren.addAll(daughters);
        return allChildren;
    }
    
    public Set<String> getAllSiblings() {
        Set<String> allSiblings = new HashSet<>(siblings);
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
