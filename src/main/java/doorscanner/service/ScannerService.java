package doorscanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import doorscanner.model.ScanResult;

public class ScannerService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScanResult parseScan(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return new ScanResult(false, rawValue, null, null, null, "Scan was empty.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawValue);

            String memberId = getText(root, "memberId");
            String name = getText(root, "name");
            String status = getText(root, "status");

            if (memberId == null || name == null || status == null) {
                return new ScanResult(
                        false,
                        rawValue,
                        memberId,
                        name,
                        status,
                        "Missing one or more required JSON fields."
                );
            }

            return new ScanResult(
                    true,
                    rawValue,
                    memberId,
                    name,
                    status,
                    "Scan parsed successfully."
            );

        } catch (Exception e) {
            return new ScanResult(
                    false,
                    rawValue,
                    null,
                    null,
                    null,
                    "Failed to parse scan JSON: " + e.getMessage()
            );
        }
    }

    private String getText(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }
}