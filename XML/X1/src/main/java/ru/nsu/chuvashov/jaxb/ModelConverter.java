package ru.nsu.chuvashov.jaxb;

import ru.nsu.chuvashov.PersonData;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts PersonData objects to JAXB-annotated FinalPerson objects
 */
public class ModelConverter {

    /**
     * Convert map of PersonData to map of FinalPerson with proper ID/IDREF references
     */
    public static Map<String, FinalPerson> convertToFinalPersons(Map<String, PersonData> personDataMap) {
        Map<String, FinalPerson> finalPersonMap = new HashMap<>();

        // First pass: create all FinalPerson objects
        for (Map.Entry<String, PersonData> entry : personDataMap.entrySet()) {
            String id = entry.getKey();
            PersonData data = entry.getValue();

            FinalPerson person = new FinalPerson(id);
            person.firstName = data.firstName;
            person.familyName = data.lastName;
            person.gender = data.gender;

            finalPersonMap.put(id, person);
        }

        // Second pass: establish relationships using IDREF
        for (Map.Entry<String, PersonData> entry : personDataMap.entrySet()) {
            String id = entry.getKey();
            PersonData data = entry.getValue();
            FinalPerson person = finalPersonMap.get(id);

            // Set parents
            if (data.mother != null && finalPersonMap.containsKey(data.mother)) {
                person.mother = finalPersonMap.get(data.mother);
            }
            if (data.father != null && finalPersonMap.containsKey(data.father)) {
                person.father = finalPersonMap.get(data.father);
            }

            // Set spouse
            if (data.spouse != null && finalPersonMap.containsKey(data.spouse)) {
                FinalPerson spouse = finalPersonMap.get(data.spouse);
                // Определяем тип связи на основе пола текущего человека и супруга
                if (person.gender != null && spouse.gender != null) {
                    // Если я мужчина, то супруг(а) - это моя жена (должна быть женщиной)
                    if (person.gender.name().equalsIgnoreCase("MALE")) {
                        person.wife = spouse;
                    } 
                    // Если я женщина, то супруг(а) - это мой муж (должен быть мужчиной)
                    else {
                        person.husband = spouse;
                    }
                }
            }

            // Set siblings
            for (String brotherId : data.brothers) {
                if (finalPersonMap.containsKey(brotherId)) {
                    person.brothers.add(finalPersonMap.get(brotherId));
                }
            }
            for (String sisterId : data.sisters) {
                if (finalPersonMap.containsKey(sisterId)) {
                    person.sisters.add(finalPersonMap.get(sisterId));
                }
            }

            // Set children
            for (String sonId : data.sons) {
                if (finalPersonMap.containsKey(sonId)) {
                    person.sons.add(finalPersonMap.get(sonId));
                }
            }
            for (String daughterId : data.daughters) {
                if (finalPersonMap.containsKey(daughterId)) {
                    person.daughters.add(finalPersonMap.get(daughterId));
                }
            }
        }

        return finalPersonMap;
    }
}
