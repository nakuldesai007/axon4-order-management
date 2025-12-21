package com.example.ordermanagement.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.*;

/**
 * Utility class to read test cases from Excel files.
 * Supports reading test scenarios with multiple steps across different days.
 */
public class ExcelTestCaseReader {

    public static class TestCase {
        private String testCaseId;
        private String testCaseName;
        private String description;
        private String day;
        private int stepNumber;
        private String action;
        private Map<String, String> parameters;
        private String expectedStatus;
        private String expectedResult;
        private String expectedException;
        private boolean enabled;

        // Getters and Setters
        public String getTestCaseId() { return testCaseId; }
        public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }
        
        public String getTestCaseName() { return testCaseName; }
        public void setTestCaseName(String testCaseName) { this.testCaseName = testCaseName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        
        public int getStepNumber() { return stepNumber; }
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public Map<String, String> getParameters() { return parameters; }
        public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
        
        public String getExpectedStatus() { return expectedStatus; }
        public void setExpectedStatus(String expectedStatus) { this.expectedStatus = expectedStatus; }
        
        public String getExpectedResult() { return expectedResult; }
        public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
        
        public String getExpectedException() { return expectedException; }
        public void setExpectedException(String expectedException) { this.expectedException = expectedException; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * Read test cases from Excel file
     * Expected columns:
     * - Test Case ID
     * - Test Case Name
     * - Description
     * - Day (Day-1, Day-2, Day-3, etc.)
     * - Step Number
     * - Action (CREATE_ORDER, ADD_ITEM, CONFIRM_ORDER, etc.)
     * - Parameters (JSON string or key-value pairs)
     * - Expected Status
     * - Expected Result
     * - Enabled (Y/N)
     */
    public static List<TestCase> readTestCases(String excelFilePath) {
        List<TestCase> testCases = new ArrayList<>();
        
        try (InputStream inputStream = ExcelTestCaseReader.class
                .getClassLoader()
                .getResourceAsStream(excelFilePath)) {
            
            if (inputStream == null) {
                throw new RuntimeException("Excel file not found: " + excelFilePath);
            }
            
            // Use try-with-resources to ensure Workbook is always closed, even if exceptions occur
            try (Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                
                // Read header row to find column indices
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new RuntimeException("Excel sheet is empty or missing header row at row 0");
                }
                Map<String, Integer> columnMap = new HashMap<>();
                for (Cell cell : headerRow) {
                    String header = getCellValueAsString(cell);
                    columnMap.put(header.trim().toUpperCase(), cell.getColumnIndex());
                }
                
                // Read data rows
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    TestCase testCase = new TestCase();
                    
                    // Read columns
                    testCase.setTestCaseId(getCellValue(row, columnMap, "TEST CASE ID"));
                    testCase.setTestCaseName(getCellValue(row, columnMap, "TEST CASE NAME"));
                    testCase.setDescription(getCellValue(row, columnMap, "DESCRIPTION"));
                    testCase.setDay(getCellValue(row, columnMap, "DAY"));
                    testCase.setStepNumber(getIntValue(row, columnMap, "STEP NUMBER"));
                    testCase.setAction(getCellValue(row, columnMap, "ACTION"));
                    
                    // Parse parameters (can be JSON or key=value pairs)
                    String paramsStr = getCellValue(row, columnMap, "PARAMETERS");
                    testCase.setParameters(parseParameters(paramsStr));
                    
                    testCase.setExpectedStatus(getCellValue(row, columnMap, "EXPECTED STATUS"));
                    testCase.setExpectedResult(getCellValue(row, columnMap, "EXPECTED RESULT"));
                    testCase.setExpectedException(getCellValue(row, columnMap, "EXPECTED EXCEPTION"));
                    
                    String enabledStr = getCellValue(row, columnMap, "ENABLED");
                    testCase.setEnabled(enabledStr != null && 
                        (enabledStr.equalsIgnoreCase("Y") || enabledStr.equalsIgnoreCase("YES") || 
                         enabledStr.equalsIgnoreCase("TRUE") || enabledStr.equals("1")));
                    
                    if (testCase.getTestCaseId() != null && !testCase.getTestCaseId().isEmpty()) {
                        testCases.add(testCase);
                    }
                }
                // Workbook is automatically closed here by try-with-resources
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file: " + excelFilePath, e);
        }
        
        return testCases;
    }

    /**
     * Group test cases by test case ID and day
     */
    public static Map<String, Map<String, List<TestCase>>> groupByTestCaseAndDay(List<TestCase> testCases) {
        Map<String, Map<String, List<TestCase>>> grouped = new LinkedHashMap<>();
        
        for (TestCase testCase : testCases) {
            if (!testCase.isEnabled()) continue;
            
            grouped.computeIfAbsent(testCase.getTestCaseId(), k -> new LinkedHashMap<>())
                   .computeIfAbsent(testCase.getDay(), k -> new ArrayList<>())
                   .add(testCase);
        }
        
        // Sort steps within each day
        grouped.values().forEach(dayMap -> 
            dayMap.values().forEach(steps -> 
                steps.sort(Comparator.comparingInt(TestCase::getStepNumber))
            )
        );
        
        return grouped;
    }

    private static String getCellValue(Row row, Map<String, Integer> columnMap, String columnName) {
        Integer colIndex = columnMap.get(columnName);
        if (colIndex == null) return null;
        
        Cell cell = row.getCell(colIndex);
        return getCellValueAsString(cell);
    }

    private static int getIntValue(Row row, Map<String, Integer> columnMap, String columnName) {
        String value = getCellValue(row, columnMap, columnName);
        if (value == null || value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Remove decimal if it's a whole number
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private static Map<String, String> parseParameters(String paramsStr) {
        Map<String, String> params = new HashMap<>();
        if (paramsStr == null || paramsStr.trim().isEmpty()) {
            return params;
        }
        
        // Try JSON format first (simple key:value pairs)
        if (paramsStr.trim().startsWith("{")) {
            // Simple JSON parsing for key:value pairs
            String cleaned = paramsStr.trim().replaceAll("[{}]", "");
            String[] pairs = cleaned.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    params.put(kv[0].trim().replaceAll("\"", ""), 
                              kv[1].trim().replaceAll("\"", ""));
                }
            }
        } else {
            // Try key=value format
            String[] pairs = paramsStr.split(",");
            for (String pair : pairs) {
                // Use limit 2 to handle values containing equals signs (e.g., key=2+2=4)
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        
        return params;
    }
}

