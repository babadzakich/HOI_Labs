package ru.nsu.chuvashov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PersonDataMerger {
    
    public Map<String, PersonData> mergePersons(List<PersonData> personList) {
        Map<String, PersonData> peopleOut = new HashMap<>();
        Map<String, List<PersonData>> idsToTemps = new HashMap<>();
        Map<String, List<Gender>> genderHints = new HashMap<>();
        Map<String, List<PersonData>> namesToTemps = new HashMap<>();
        
        for (PersonData person : personList) {
            if (person.id != null) {
                if (!peopleOut.containsKey(person.id)) {
                    peopleOut.put(person.id, new PersonData(person.id));
                    genderHints.put(person.id, new ArrayList<>());
                    idsToTemps.put(person.id, new ArrayList<>());
                }
                idsToTemps.get(person.id).add(person);
            } else {
                String key = getFullName(person);
                if (key != null) {
                    if (!namesToTemps.containsKey(key)) {
                        namesToTemps.put(key, new ArrayList<>());
                    }
                    namesToTemps.get(key).add(person);
                }
            }
        }
        
        BiConsumer<String, String> setHusbandWife = (hId, wId) -> {
            if (!peopleOut.containsKey(hId) || !peopleOut.containsKey(wId)) return;
            if (!assertEquals(peopleOut.get(hId).spouse, wId)) return;
            if (!assertEquals(peopleOut.get(wId).spouse, hId)) return;
            peopleOut.get(hId).spouse = wId;
            peopleOut.get(wId).spouse = hId;
        };
        
        BiConsumer<String, String> addChild = (pId, cId) -> {
            if (!peopleOut.containsKey(pId) || !peopleOut.containsKey(cId)) return;
            PersonData child = peopleOut.get(cId);
            PersonData parent = peopleOut.get(pId);
            if (child.mother != null && child.father != null) {
                if (!child.mother.equals(pId) && !child.father.equals(pId)) return;
            }
            if (!parent.children.contains(cId))
                parent.children.add(cId);
            if (child.mother == null) {
                child.mother = pId;
            } else if (!child.mother.equals(pId) && child.father == null) {
                child.father = pId;
            } else {
                assert child.mother.equals(pId) || child.father.equals(pId);
            }
        };
        
        BiConsumer<String, String> addSibling = (lhsId, rhsId) -> {
            if (!peopleOut.containsKey(lhsId) || !peopleOut.containsKey(rhsId)) return;
            PersonData lhs = peopleOut.get(lhsId);
            PersonData rhs = peopleOut.get(rhsId);            
            // Don't add as siblings if one is already a child/parent of the other
            if (lhs.children.contains(rhsId) || rhs.children.contains(lhsId))
                return;
            if (lhsId.equals(rhs.mother) || lhsId.equals(rhs.father))
                return;
            if (rhsId.equals(lhs.mother) || rhsId.equals(lhs.father))
                return;
                        if (!lhs.siblings.contains(rhsId))
                lhs.siblings.add(rhsId);
            if (!rhs.siblings.contains(lhsId))
                rhs.siblings.add(lhsId);
            for (String parent : lhs.getParents()) {
                if (parent != null) {
                    addChild.accept(parent, rhsId);
                }
            }
            for (String parent : rhs.getParents()) {
                if (parent != null) {
                    addChild.accept(parent, lhsId);
                }
            }
        };
        
        for (String id : peopleOut.keySet()) {
            List<PersonData> temps = idsToTemps.get(id);
            PersonData person = peopleOut.get(id);
            
            for (PersonData temp : temps) {
                assert temp.id != null;
                if (temp.firstName != null) {
                    assertEquals(person.firstName, temp.firstName);
                    person.firstName = temp.firstName;
                }
                if (temp.lastName != null) {
                    assertEquals(person.lastName, temp.lastName);
                    person.lastName = temp.lastName;
                }
                if (temp.spouse != null && temp.spouse.startsWith("P") && peopleOut.containsKey(temp.spouse)) {
                    setHusbandWife.accept(id, temp.spouse);
                    // Если spouse - это wife (жена), то супруг - женщина, а сам человек - мужчина
                    if (temp.isSpouseWife != null && temp.isSpouseWife) {
                        genderHints.get(temp.spouse).add(Gender.FEMALE);
                        genderHints.get(id).add(Gender.MALE);
                    }
                    // Если spouse - это husband (муж), то супруг - мужчина, а сам человек - женщина
                    else if (temp.isSpouseWife != null && !temp.isSpouseWife) {
                        genderHints.get(temp.spouse).add(Gender.MALE);
                        genderHints.get(id).add(Gender.FEMALE);
                    }
                }
                
                for (String sonId : temp.sons) {
                    if (peopleOut.containsKey(sonId)) {
                        addChild.accept(id, sonId);
                        genderHints.get(sonId).add(Gender.MALE);
                    }
                }
                for (String daughterId : temp.daughters) {
                    if (peopleOut.containsKey(daughterId)) {
                        addChild.accept(id, daughterId);
                        genderHints.get(daughterId).add(Gender.FEMALE);
                    }
                }
                if (temp.mother != null && temp.mother.startsWith("P") && peopleOut.containsKey(temp.mother)) {
                    addChild.accept(temp.mother, id);
                }
                if (temp.father != null && temp.father.startsWith("P") && peopleOut.containsKey(temp.father)) {
                    addChild.accept(temp.father, id);
                }
                for (String sibling : temp.brothers) {
                    if (sibling.startsWith("P") && peopleOut.containsKey(sibling)) {
                        addSibling.accept(id, sibling);
                        genderHints.get(sibling).add(Gender.MALE);
                    }
                }
                for (String sibling : temp.sisters) {
                    if (sibling.startsWith("P") && peopleOut.containsKey(sibling)) {
                        addSibling.accept(id, sibling);
                        genderHints.get(sibling).add(Gender.FEMALE);
                    }
                }
                // Обработка siblings с неизвестным полом (без gender hints)
                for (String sibling : temp.siblings) {
                    if (sibling.startsWith("P") && peopleOut.containsKey(sibling)) {
                        addSibling.accept(id, sibling);
                        // НЕ добавляем gender hint, т.к. пол неизвестен
                    }
                }
            }
        }
        
        Map<String, List<String>> namesToIds = new HashMap<>();
        for (String id : peopleOut.keySet()) {
            PersonData person = peopleOut.get(id);
            assert (person.firstName != null) && (person.lastName != null);
            String key = person.firstName + " " + person.lastName;
            if (!namesToIds.containsKey(key)) {
                namesToIds.put(key, new ArrayList<>());
            }
            namesToIds.get(key).add(id);
        }
        
        for (String id : peopleOut.keySet()) {
            List<PersonData> temps = idsToTemps.get(id);
            PersonData person = peopleOut.get(id);
            List<PersonData> additionals = namesToTemps.get(person.firstName + " " + person.lastName);
            if (additionals != null) {
                temps.addAll(additionals.stream().filter((t) ->  {
                    if (!(t.id == null || t.id.equals(id)))
                        return false;
                    if (person.spouse != null && !person.spouse.equals(t.spouse))
                        return false;
                    return true;
                }).toList());
            }
            
            for (PersonData temp : temps) {
                if (temp.spouse != null && !temp.spouse.startsWith("P")) {
                    List<String> ids = namesToIds.get(temp.spouse);
                    if (ids != null && !ids.isEmpty()) {
                        String spouseId = ids.stream().filter(candidateId -> {
                            PersonData candidate = peopleOut.get(candidateId);
                            if (candidate.spouse != null && candidate.spouse.equals(id))
                                return true;
                            for (String childId : peopleOut.get(id).children) {
                                PersonData child = peopleOut.get(childId);
                                if (candidateId.equals(child.mother) || candidateId.equals(child.father))
                                    return true;
                            }
                            return false;
                        }).findFirst().orElse(ids.get(0));
                        setHusbandWife.accept(id, spouseId);
                    }
                }
                
                if (temp.father != null && !temp.father.startsWith("P")) {
                    List<String> ids = namesToIds.get(temp.father);
                    if (ids != null && !ids.isEmpty()) {
                        String fatherId = ids.stream().filter(candidateId -> {
                            PersonData candidate = peopleOut.get(candidateId);
                            return candidate.children.contains(id);
                        }).findFirst().orElse(ids.get(0));
                        addChild.accept(fatherId, id);
                        genderHints.get(fatherId).add(Gender.MALE);
                    }
                }
                if (temp.mother != null && !temp.mother.startsWith("P")) {
                    List<String> ids = namesToIds.get(temp.mother);
                    if (ids != null && !ids.isEmpty()) {
                        String motherId = ids.stream().filter(candidateId -> {
                            PersonData candidate = peopleOut.get(candidateId);
                            return candidate.children.contains(id);
                        }).findFirst().orElse(ids.get(0));
                        addChild.accept(motherId, id);
                        genderHints.get(motherId).add(Gender.FEMALE);
                    }
                }
                for (String brotherName : temp.brothers) {
                    if (brotherName.startsWith("P")) continue;
                    List<String> ids = namesToIds.get(brotherName);
                    if (ids != null && !ids.isEmpty()) {
                        PersonData self = peopleOut.get(id);
                        String brotherId = ids.stream().filter(candidateId -> {
                            PersonData candidate = peopleOut.get(candidateId);
                            if (candidate.siblings.contains(id)) return true;
                            for (int i = 0; i < 2; i++) {
                                String parent = i == 0 ? self.mother : self.father;
                                if (parent != null && (parent.equals(candidate.mother) || parent.equals(candidate.father)))
                                    return true;
                            }
                            return false;
                        }).findFirst().orElse(ids.get(0));
                        addSibling.accept(id, brotherId);
                        genderHints.get(brotherId).add(Gender.MALE);
                    }
                }
                for (String sisterName : temp.sisters) {
                    if (sisterName.startsWith("P")) continue;
                    List<String> ids = namesToIds.get(sisterName);
                    if (ids != null && !ids.isEmpty()) {
                        PersonData self = peopleOut.get(id);
                        String sisterId = ids.stream().filter(candidateId -> {
                            PersonData candidate = peopleOut.get(candidateId);
                            for (int i = 0; i < 2; i++) {
                                String parent = i == 0 ? self.mother : self.father;
                                if (parent != null && (parent.equals(candidate.mother) || parent.equals(candidate.father)))
                                    return true;
                            }
                            return false;
                        }).findFirst().orElse(ids.get(0));
                        addSibling.accept(id, sisterId);
                        genderHints.get(sisterId).add(Gender.FEMALE);
                    }
                }
                for (String childName : temp.childrenNames) {
                    List<String> ids = namesToIds.get(childName);
                    if (ids != null && !ids.isEmpty()) {
                        List<String> childId = ids.stream().filter((filterId) -> {
                            PersonData candidate = peopleOut.get(filterId);
                            return candidate.mother == null || candidate.father == null ||
                                   candidate.mother.equals(id) || candidate.father.equals(id);
                        }).toList();
                        if (childId.size() == 1) {
                            addChild.accept(id, childId.get(0));
                        }
                    }
                }
                for (String parentName : temp.parentNames) {
                    List<String> ids = namesToIds.get(parentName);
                    if (ids != null && !ids.isEmpty()) {
                        String parentId = ids.stream().filter(candidateId -> {
                            PersonData candidate = peopleOut.get(candidateId);
                            if (candidate.children.contains(id)) return true;
                            PersonData me = peopleOut.get(id);
                            if (me.mother != null && me.mother.equals(candidateId)) return true;
                            if (me.father != null && me.father.equals(candidateId)) return true;
                            return false;
                        }).findFirst().orElse(ids.get(0));
                        addChild.accept(parentId, id);
                    }
                }
            }
        }

        for (String id : genderHints.keySet()) {
            List<Gender> hints = genderHints.get(id);
            long maleCnt = hints.stream().filter((g) -> g.equals(Gender.MALE)).count();
            peopleOut.get(id).gender = maleCnt >= hints.size() / 2.0 ? Gender.MALE : Gender.FEMALE;
        }
        
        // После определения пола распределяем детей и сиблингов по полу
        for (String id : peopleOut.keySet()) {
            PersonData person = peopleOut.get(id);
            
            // Распределяем детей по полу
            for (String childId : person.children) {
                PersonData child = peopleOut.get(childId);
                if (child != null && child.gender != null) {
                    if (child.gender == Gender.MALE) {
                        person.sons.add(childId);
                    } else {
                        person.daughters.add(childId);
                    }
                }
            }
            
            // Распределяем сиблингов по полу
            for (String siblingId : person.siblings) {
                PersonData sibling = peopleOut.get(siblingId);
                if (sibling != null && sibling.gender != null) {
                    if (sibling.gender == Gender.MALE) {
                        person.brothers.add(siblingId);
                    } else {
                        person.sisters.add(siblingId);
                    }
                }
            }
        }
        
        return peopleOut;
    }
    
    private String getFullName(PersonData person) {
        if (person.firstName == null || person.lastName == null) {
            return null;
        }
        return person.firstName + " " + person.lastName;
    }
    
    private static boolean assertEquals(Object lhs, Object rhs) {
        return lhs == null || lhs.equals(rhs);
    }
}
