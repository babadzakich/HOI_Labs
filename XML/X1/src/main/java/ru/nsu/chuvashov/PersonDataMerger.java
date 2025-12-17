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
        Map<String, PersonData> byId = new HashMap<>();
        Map<String, List<PersonData>> byName = new HashMap<>();
        List<PersonData> noIdentifier = new ArrayList<>();

        for (PersonData person : personList) {
            if (person.id != null) {
                String id = person.id;
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

        for (Map.Entry<String, List<PersonData>> entry : byName.entrySet()) {
            List<PersonData> personsWithName = entry.getValue();

            PersonData merged = null;
            for (PersonData p : personsWithName) {
                if (p.id != null) {
                    merged = byId.get(p.id);
                    break;
                }
            }
            

            if (merged == null) {
                merged = new PersonData();
                String tempId = entry.getKey().replaceAll("\\s+", "_");
                merged.id = tempId;
                byId.put(tempId, merged);
            }

            for (PersonData p : personsWithName) {
                merged.merge(p);
            }
        }
        

        resolveSiblingGenders(byId);
        

        resolveSpouseReferences(byId);
        
        return byId;
    }

    private void resolveSiblingGenders(Map<String, PersonData> personMap) {
        for (PersonData person : personMap.values()) {
            Set<String> unresolvedSiblings = new HashSet<>(person.brothers);
            person.brothers.clear();
            
            for (String siblingId : unresolvedSiblings) {
                PersonData sibling = personMap.get(siblingId);
                if (sibling != null && sibling.gender != null) {
                    if (sibling.gender == Gender.MALE) {
                        person.brothers.add(siblingId);
                    } else {
                        person.sisters.add(siblingId);
                    }
                } else {

                    person.brothers.add(siblingId);
                }
            }
        }
    }
    
    private void resolveSpouseReferences(Map<String, PersonData> personMap) {
        Map<String, String> nameToId = new HashMap<>();
        for (PersonData person : personMap.values()) {
            String name = getFullName(person);
            if (name != null && person.id != null && !person.id.startsWith("TEMP_")) {
                nameToId.put(name, person.id);
            }
        }

        for (PersonData person : personMap.values()) {
            String spouse = person.spouse;
            if (spouse != null && !spouse.startsWith("P")) {
                String spouseId = nameToId.get(spouse);
                if (spouseId != null) {
                    person.spouse = spouseId;
                }
            }
        }
    }
    
    private String getFullName(PersonData person) {
        if (person.firstName != null && person.lastName != null) {
            return person.firstName + " " + person.lastName;
        } else if (person.firstName != null) {
            return person.firstName;
        }
        return null;
    }
}
