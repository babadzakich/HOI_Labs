package ru.nsu.chuvashov;

import java.util.*;

/**
 * Validates consistency of person data based on auxiliary markers
 */
public class DataValidator {
    
    private List<ValidationError> errors = new ArrayList<>();
    private List<ValidationWarning> warnings = new ArrayList<>();
    
    public static class ValidationError {
        private String personId;
        private String message;
        
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
    
    /**
     * Validate all persons and return validation report
     */
    public ValidationReport validate(Map<String, PersonData> personMap) {
        errors.clear();
        warnings.clear();
        
        for (PersonData person : personMap.values()) {
            validatePerson(person, personMap);
        }
        
        return new ValidationReport(new ArrayList<>(errors), new ArrayList<>(warnings));
    }
    
    private void validatePerson(PersonData person, Map<String, PersonData> personMap) {
        String id = person.getId() != null ? person.getId() : "UNKNOWN";
        
        // Validate children count
        if (person.getExpectedChildrenCount() != null) {
            int expected = person.getExpectedChildrenCount();
            int actual = person.getActualChildrenCount();
            
            if (expected != actual) {
                errors.add(new ValidationError(id, 
                    String.format("Children count mismatch: expected %d, found %d", expected, actual)));
            }
        }
        
        // Validate siblings count
        if (person.getExpectedSiblingsCount() != null) {
            int expected = person.getExpectedSiblingsCount();
            int actual = person.getActualSiblingsCount();
            
            if (expected != actual) {
                errors.add(new ValidationError(id,
                    String.format("Siblings count mismatch: expected %d, found %d", expected, actual)));
            }
        }
        
        // Validate that referenced persons exist
        validateReferences(person, personMap);
        
        // Check for logical inconsistencies
        validateLogicalConsistency(person);
    }
    
    private void validateReferences(PersonData person, Map<String, PersonData> personMap) {
        String id = person.getId() != null ? person.getId() : "UNKNOWN";
        
        // Check spouse exists
        if (person.getSpouse() != null && person.getSpouse().startsWith("P")) {
            if (!personMap.containsKey(person.getSpouse())) {
                warnings.add(new ValidationWarning(id, 
                    "Spouse reference " + person.getSpouse() + " not found"));
            }
        }
        
        // Check children exist
        for (String childId : person.getAllChildren()) {
            if (childId.startsWith("P") && !personMap.containsKey(childId)) {
                warnings.add(new ValidationWarning(id,
                    "Child reference " + childId + " not found"));
            }
        }
        
        // Check siblings exist
        for (String siblingId : person.getAllSiblings()) {
            if (siblingId.startsWith("P") && !personMap.containsKey(siblingId)) {
                warnings.add(new ValidationWarning(id,
                    "Sibling reference " + siblingId + " not found"));
            }
        }
        
        // Check parents exist
        if (person.getMother() != null && person.getMother().startsWith("P")) {
            if (!personMap.containsKey(person.getMother())) {
                warnings.add(new ValidationWarning(id,
                    "Mother reference " + person.getMother() + " not found"));
            }
        }
        
        if (person.getFather() != null && person.getFather().startsWith("P")) {
            if (!personMap.containsKey(person.getFather())) {
                warnings.add(new ValidationWarning(id,
                    "Father reference " + person.getFather() + " not found"));
            }
        }
    }
    
    private void validateLogicalConsistency(PersonData person) {
        String id = person.getId() != null ? person.getId() : "UNKNOWN";
        
        // Check that person has at least some information
        if (person.getFirstName() == null && 
            person.getLastName() == null && 
            person.getGender() == null &&
            person.getSpouse() == null &&
            person.getAllChildren().isEmpty() &&
            person.getAllSiblings().isEmpty() &&
            person.getParents().isEmpty()) {
            
            warnings.add(new ValidationWarning(id, 
                "Person has minimal information (only ID)"));
        }
        
        // Check for siblings that are also children (logical error)
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
        
        public List<ValidationError> getErrors() {
            return errors;
        }
        
        public List<ValidationWarning> getWarnings() {
            return warnings;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
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
                // Print only first 20 warnings to avoid clutter
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
