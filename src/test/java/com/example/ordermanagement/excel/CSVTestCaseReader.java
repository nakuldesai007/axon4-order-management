package com.example.ordermanagement.excel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Alternative CSV-based test case reader (simpler than Excel, no dependencies)
 * Can be used if Apache POI is not available
 */
public class CSVTestCaseReader {

    public static List<ExcelTestCaseReader.TestCase> readTestCasesFromCSV(String csvFilePath) {
        List<ExcelTestCaseReader.TestCase> testCases = new ArrayList<>();
        
        try (InputStream inputStream = CSVTestCaseReader.class
                .getClassLoader()
                .getResourceAsStream(csvFilePath)) {
            
            if (inputStream == null) {
                throw new RuntimeException("CSV file not found: " + csvFilePath);
            }
            
            // Wrap BufferedReader in try-with-resources to ensure proper cleanup
            // Closing BufferedReader will also close the underlying InputStreamReader
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = reader.readLine(); // Skip header
                
                if (line == null) {
                    throw new RuntimeException("CSV file is empty: " + csvFilePath);
                }
                
                // Parse header to get column indices
                String[] headers = parseCSVLine(line);
                Map<String, Integer> columnMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    columnMap.put(headers[i].trim().toUpperCase(), i);
                }
                
                // Read data rows
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    
                    String[] values = parseCSVLine(line);
                    if (values.length == 0) continue;
                    
                    ExcelTestCaseReader.TestCase testCase = new ExcelTestCaseReader.TestCase();
                    
                    testCase.setTestCaseId(getValue(values, columnMap, "TEST CASE ID"));
                    testCase.setTestCaseName(getValue(values, columnMap, "TEST CASE NAME"));
                    testCase.setDescription(getValue(values, columnMap, "DESCRIPTION"));
                    testCase.setDay(getValue(values, columnMap, "DAY"));
                    
                    String stepNumStr = getValue(values, columnMap, "STEP NUMBER");
                    if (stepNumStr != null && !stepNumStr.isEmpty()) {
                        try {
                            testCase.setStepNumber(Integer.parseInt(stepNumStr.trim()));
                        } catch (NumberFormatException e) {
                            testCase.setStepNumber(0);
                        }
                    }
                    
                    testCase.setAction(getValue(values, columnMap, "ACTION"));
                    
                    String paramsStr = getValue(values, columnMap, "PARAMETERS");
                    testCase.setParameters(parseParameters(paramsStr));
                    
                    testCase.setExpectedStatus(getValue(values, columnMap, "EXPECTED STATUS"));
                    testCase.setExpectedResult(getValue(values, columnMap, "EXPECTED RESULT"));
                    testCase.setExpectedException(getValue(values, columnMap, "EXPECTED EXCEPTION"));
                    
                    String enabledStr = getValue(values, columnMap, "ENABLED");
                    testCase.setEnabled(enabledStr != null && 
                        (enabledStr.equalsIgnoreCase("Y") || enabledStr.equalsIgnoreCase("YES") || 
                         enabledStr.equalsIgnoreCase("TRUE") || enabledStr.equals("1")));
                    
                    if (testCase.getTestCaseId() != null && !testCase.getTestCaseId().isEmpty()) {
                        testCases.add(testCase);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV file: " + csvFilePath, e);
        }
        
        return testCases;
    }

    private static String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        
        return values.toArray(new String[0]);
    }

    private static String getValue(String[] values, Map<String, Integer> columnMap, String columnName) {
        Integer colIndex = columnMap.get(columnName);
        if (colIndex == null || colIndex >= values.length) {
            return null;
        }
        String value = values[colIndex];
        return value == null || value.isEmpty() ? null : value;
    }

    private static Map<String, String> parseParameters(String paramsStr) {
        Map<String, String> params = new HashMap<>();
        if (paramsStr == null || paramsStr.trim().isEmpty()) {
            return params;
        }
        
        // Remove quotes if present
        paramsStr = paramsStr.trim();
        if (paramsStr.startsWith("\"") && paramsStr.endsWith("\"")) {
            paramsStr = paramsStr.substring(1, paramsStr.length() - 1);
        }
        
        // Parse key=value pairs
        String[] pairs = paramsStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0].trim(), kv[1].trim());
            }
        }
        
        return params;
    }
}

