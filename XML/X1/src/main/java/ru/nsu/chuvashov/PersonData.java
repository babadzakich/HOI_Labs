package ru.nsu.chuvashov;

import java.util.*;

/**
 * Complete person data with all available information consolidated from multiple XML entries
 */
public class PersonData {
    public String id;
    public String firstName;
    public String lastName;
    public Gender gender;
    public String spouse;
    
    // Relationships
    public Set<String> brothers = new HashSet<>();
    public Set<String> sisters = new HashSet<>();
    public Set<String> sons = new HashSet<>();
    public Set<String> daughters = new HashSet<>();
    public String mother;
    public String father;
    
    // Validation markers
    public Integer expectedChildrenCount;
    public Integer expectedSiblingsCount;
    
    public PersonData() {}
    
    public PersonData(String id) {
        this.id = id;
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
