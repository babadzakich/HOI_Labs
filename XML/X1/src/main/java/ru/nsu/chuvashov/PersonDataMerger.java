package ru.nsu.chuvashov;

import java.util.*;

/**
 * Consolidates multiple partial person entries into complete person records
 */
public class PersonDataMerger {
    
    /**
     * Merge multiple person entries into consolidated map of unique persons
     */
    public Map<String, PersonData> mergePersons(List<PersonData> personList) {
        // Maps to help identify persons
        Map<String, PersonData> byId = new HashMap<>();
        Map<String, List<PersonData>> byName = new HashMap<>();
        List<PersonData> noIdentifier = new ArrayList<>();
        
        // First pass: group by ID and name
        for (PersonData person : personList) {
            if (person.getId() != null) {
                String id = person.getId();
                byId.putIfAbsent(id, new PersonData(id));
                byId.get(id).merge(person);
            } else {
                String fullName = getFullName(person);
                if (fullName != null) {
                    byName.putIfAbsent(fullName, new ArrayList<>());
                    byName.get(fullName).add(person);
                } else {
                    noIdentifier.add(person);
                }
            }
        }
        
        // Second pass: merge persons with same name but no ID
        for (Map.Entry<String, List<PersonData>> entry : byName.entrySet()) {
            List<PersonData> personsWithName = entry.getValue();
            
            // Try to find if any has an ID that matches
            PersonData merged = null;
            for (PersonData p : personsWithName) {
                if (p.getId() != null) {
                    merged = byId.get(p.getId());
                    break;
                }
            }
            
            // If no ID found, create new entry
            if (merged == null) {
                merged = new PersonData();
                // Generate temporary ID based on name
                String tempId = "TEMP_" + entry.getKey().replaceAll("\\s+", "_");
                merged.setId(tempId);
                byId.put(tempId, merged);
            }
            
            // Merge all persons with this name
            for (PersonData p : personsWithName) {
                merged.merge(p);
            }
        }
        
        // Third pass: resolve sibling relationships using gender information
        resolveSiblingGenders(byId);
        
        // Fourth pass: try to resolve spouse names to IDs
        resolveSpouseReferences(byId);
        
        return byId;
    }
    
    /**
     * Resolve sibling gender by looking up sibling IDs in the person map
     */
    private void resolveSiblingGenders(Map<String, PersonData> personMap) {
        for (PersonData person : personMap.values()) {
            Set<String> unresolvedSiblings = new HashSet<>(person.getBrothers());
            person.getBrothers().clear();
            
            for (String siblingId : unresolvedSiblings) {
                PersonData sibling = personMap.get(siblingId);
                if (sibling != null && sibling.getGender() != null) {
                    if (sibling.getGender() == Gender.MALE) {
                        person.addBrother(siblingId);
                    } else {
                        person.addSister(siblingId);
                    }
                } else {
                    // Gender unknown, keep as brother for now
                    person.addBrother(siblingId);
                }
            }
        }
    }
    
    /**
     * Try to resolve spouse names to IDs
     */
    private void resolveSpouseReferences(Map<String, PersonData> personMap) {
        // Build name to ID mapping
        Map<String, String> nameToId = new HashMap<>();
        for (PersonData person : personMap.values()) {
            String name = getFullName(person);
            if (name != null && person.getId() != null && !person.getId().startsWith("TEMP_")) {
                nameToId.put(name, person.getId());
            }
        }
        
        // Resolve spouse references
        for (PersonData person : personMap.values()) {
            String spouse = person.getSpouse();
            if (spouse != null && !spouse.startsWith("P")) {
                // It's a name, try to resolve to ID
                String spouseId = nameToId.get(spouse);
                if (spouseId != null) {
                    person.setSpouse(spouseId);
                }
            }
        }
    }
    
    private String getFullName(PersonData person) {
        if (person.getFirstName() != null && person.getLastName() != null) {
            return person.getFirstName() + " " + person.getLastName();
        } else if (person.getFirstName() != null) {
            return person.getFirstName();
        }
        return null;
    }
}
