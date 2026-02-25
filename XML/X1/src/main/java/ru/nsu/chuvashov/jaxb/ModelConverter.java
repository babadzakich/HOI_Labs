package ru.nsu.chuvashov.jaxb;

import ru.nsu.chuvashov.PersonData;

import java.util.HashMap;
import java.util.Map;


public class ModelConverter {

    public static Map<String, FinalPerson> convertToFinalPersons(Map<String, PersonData> personDataMap) {
        Map<String, FinalPerson> finalPersonMap = new HashMap<>();

        for (Map.Entry<String, PersonData> entry : personDataMap.entrySet()) {
            String id = entry.getKey();
            PersonData data = entry.getValue();

            FinalPerson person = new FinalPerson(id);
            person.firstName = data.firstName;
            person.familyName = data.lastName;
            person.gender = data.gender;

            finalPersonMap.put(id, person);
        }

        for (Map.Entry<String, PersonData> entry : personDataMap.entrySet()) {
            String id = entry.getKey();
            PersonData data = entry.getValue();
            FinalPerson person = finalPersonMap.get(id);

            if (data.mother != null && finalPersonMap.containsKey(data.mother)) {
                person.mother = finalPersonMap.get(data.mother);
            }
            if (data.father != null && finalPersonMap.containsKey(data.father)) {
                person.father = finalPersonMap.get(data.father);
            }

            if (data.spouse != null && finalPersonMap.containsKey(data.spouse)) {
                FinalPerson spouse = finalPersonMap.get(data.spouse);
                if (person.gender != null && spouse.gender != null) {
                    if (person.gender.name().equalsIgnoreCase("MALE")) {
                        person.wife = spouse;
                    } 
                    else {
                        person.husband = spouse;
                    }
                }
            }

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
