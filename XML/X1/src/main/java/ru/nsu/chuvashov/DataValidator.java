package ru.nsu.chuvashov;

import java.util.*;

public class DataValidator {
    
    private final List<ValidationError> errors = new ArrayList<>();
    private final List<ValidationWarning> warnings = new ArrayList<>();
    
    public static class ValidationError {
        private final String personId;
        private final String message;
        
        public ValidationError(String personId, String message) {
            this.personId = personId;
            this.message = message;
        }
        
        @Override
        public String toString() {
            return "ERROR [" + personId + "]: " + message;
        }
    }
    
    public static class ValidationWarning {
        private String personId;
        private String message;
        
        public ValidationWarning(String personId, String message) {
            this.personId = personId;
            this.message = message;
        }
        
        @Override
        public String toString() {
            return "WARNING [" + personId + "]: " + message;
        }
    }

    public ValidationReport validate(Map<String, PersonData> personMap) {
        errors.clear();
        warnings.clear();
        
        for (PersonData person : personMap.values()) {
            validatePerson(person, personMap);
        }
        
        return new ValidationReport(new ArrayList<>(errors), new ArrayList<>(warnings));
    }
    
    private void validatePerson(PersonData person, Map<String, PersonData> personMap) {
        String id = person.id != null ? person.id : "UNKNOWN";
        
        if (person.expectedChildrenCount != null) {
            int expected = person.expectedChildrenCount;
            int actual = person.getActualChildrenCount();
            
            if (expected != actual) {
                errors.add(new ValidationError(id, 
                    String.format("Children count mismatch: expected %d, found %d", expected, actual)));
            }
        }
        
        if (person.expectedSiblingsCount != null) {
            int expected = person.expectedSiblingsCount;
            int actual = person.getActualSiblingsCount();
            
            if (expected != actual) {
                errors.add(new ValidationError(id,
                    String.format("Siblings count mismatch: expected %d, found %d", expected, actual)));
            }
        }
        
        validateReferences(person, personMap);
        
        validateLogicalConsistency(person);
    }
    
    private void validateReferences(PersonData person, Map<String, PersonData> personMap) {
        String id = person.id != null ? person.id : "UNKNOWN";
        
        if (person.spouse != null && person.spouse.startsWith("P")) {
            if (!personMap.containsKey(person.spouse)) {
                warnings.add(new ValidationWarning(id, 
                    "Spouse reference " + person.spouse + " not found"));
            }
        }
        
        for (String childId : person.getAllChildren()) {
            if (childId.startsWith("P") && !personMap.containsKey(childId)) {
                warnings.add(new ValidationWarning(id,
                    "Child reference " + childId + " not found"));
            }
        }
        
        for (String siblingId : person.getAllSiblings()) {
            if (siblingId.startsWith("P") && !personMap.containsKey(siblingId)) {
                warnings.add(new ValidationWarning(id,
                    "Sibling reference " + siblingId + " not found"));
            }
        }
        
        if (person.mother != null && person.mother.startsWith("P")) {
            if (!personMap.containsKey(person.mother)) {
                warnings.add(new ValidationWarning(id,
                    "Mother reference " + person.mother + " not found"));
            }
        }
        
        if (person.father != null && person.father.startsWith("P")) {
            if (!personMap.containsKey(person.father)) {
                warnings.add(new ValidationWarning(id,
                    "Father reference " + person.father + " not found"));
            }
        }
    }
    
    private void validateLogicalConsistency(PersonData person) {
        String id = person.id != null ? person.id : "UNKNOWN";
        
        if (person.firstName == null &&
            person.lastName == null && 
            person.gender == null &&
            person.spouse == null &&
            person.getAllChildren().isEmpty() &&
            person.getAllSiblings().isEmpty() &&
            person.getParents().isEmpty()) {
            
            warnings.add(new ValidationWarning(id, 
                "Person has minimal information (only ID)"));
        }
        
        Set<String> childrenIds = person.getAllChildren();
        Set<String> siblingIds = person.getAllSiblings();
        
        Set<String> intersection = new HashSet<>(childrenIds);
        intersection.retainAll(siblingIds);
        
        if (!intersection.isEmpty()) {
            errors.add(new ValidationError(id,
                "Person has individuals listed as both children and siblings: " + intersection));
        }
    }
    
    public static class ValidationReport {
        private final List<ValidationError> errors;
        private final List<ValidationWarning> warnings;
        
        public ValidationReport(List<ValidationError> errors, List<ValidationWarning> warnings) {
            this.errors = errors;
            this.warnings = warnings;
        }

        public void printReport() {
            System.out.println("\n=== VALIDATION REPORT ===");
            System.out.println("Errors: " + errors.size());
            System.out.println("Warnings: " + warnings.size());
            
            if (!errors.isEmpty()) {
                System.out.println("\n--- ERRORS ---");
                for (ValidationError error : errors) {
                    System.out.println(error);
                }
            }
            
            if (!warnings.isEmpty()) {
                System.out.println("\n--- WARNINGS ---");
                int count = Math.min(20, warnings.size());
                for (int i = 0; i < count; i++) {
                    System.out.println(warnings.get(i));
                }
                if (warnings.size() > 20) {
                    System.out.println("... and " + (warnings.size() - 20) + " more warnings");
                }
            }
            
            System.out.println("========================\n");
        }
    }
}
