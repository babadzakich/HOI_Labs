package ru.nsu.chuvashov;

import java.util.*;

/**
 * Main application to parse, validate, and restructure person data from XML
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Configuration
            String inputFile = "src/main/resources/people.xml";
            String outputFile = "src/main/resources/people_structured.xml";
            
            System.out.println("=== Person Data Processing ===");
            System.out.println("Input file: " + inputFile);
            System.out.println("Output file: " + outputFile);
            System.out.println();
            
            // Step 1: Parse XML file
            System.out.println("Step 1: Parsing XML file...");
            XMLParser parser = new XMLParser();
            List<PersonData> rawPersons = parser.parseXML(inputFile);
            System.out.println("Parsed " + rawPersons.size() + " person entries");
            System.out.println();
            
            // Step 2: Merge duplicate/partial entries
            System.out.println("Step 2: Merging person data...");
            PersonDataMerger merger = new PersonDataMerger();
            Map<String, PersonData> consolidatedPersons = merger.mergePersons(rawPersons);
            System.out.println("Consolidated into " + consolidatedPersons.size() + " unique persons");
            System.out.println();
            
            // Step 3: Validate data consistency
            System.out.println("Step 3: Validating data consistency...");
            DataValidator validator = new DataValidator();
            DataValidator.ValidationReport report = validator.validate(consolidatedPersons);
            report.printReport();
            
            // Step 4: Display sample data
            System.out.println("Step 4: Sample consolidated data:");
            displaySampleData(consolidatedPersons, 5);
            System.out.println();
            
            // Step 5: Write structured XML output
            System.out.println("Step 5: Writing structured XML...");
            XMLWriter writer = new XMLWriter();
            writer.writeXML(consolidatedPersons, outputFile);
            System.out.println("Successfully wrote structured data to " + outputFile);
            System.out.println();
            
            // Summary statistics
            printStatistics(consolidatedPersons);
            
            System.out.println("\n=== Processing Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error processing XML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void displaySampleData(Map<String, PersonData> personMap, int count) {
        int displayed = 0;
        for (PersonData person : personMap.values()) {
            if (displayed >= count) break;
            
            // Only show persons with meaningful data
            if (person.firstName != null || person.lastName != null) {
                System.out.println("  " + person);
                displayed++;
            }
        }
    }
    
    private static void printStatistics(Map<String, PersonData> personMap) {
        int withFirstName = 0;
        int withLastName = 0;
        int withGender = 0;
        int withSpouse = 0;
        int withChildren = 0;
        int withSiblings = 0;
        int withParents = 0;
        
        for (PersonData person : personMap.values()) {
            if (person.firstName != null) withFirstName++;
            if (person.lastName != null) withLastName++;
            if (person.gender != null) withGender++;
            if (person.spouse != null) withSpouse++;
            if (!person.brothers.isEmpty() || !person.sisters.isEmpty()) withSiblings++;
            if (!person.daughters.isEmpty() || !person.brothers.isEmpty()) withChildren++;
            if (person.father != null || person.mother != null) withParents++;
        }
        
        System.out.println("=== Statistics ===");
        System.out.println("Total persons: " + personMap.size());
        System.out.println("With first name: " + withFirstName);
        System.out.println("With last name: " + withLastName);
        System.out.println("With gender: " + withGender);
        System.out.println("With spouse: " + withSpouse);
        System.out.println("With children: " + withChildren);
        System.out.println("With siblings: " + withSiblings);
        System.out.println("With parents: " + withParents);
    }
}