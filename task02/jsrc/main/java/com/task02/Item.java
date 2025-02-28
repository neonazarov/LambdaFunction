package com.task02;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Item {
    private String id = UUID.randomUUID().toString();
    private Integer principleId;
    private String createdAt = DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.now());
    private Map<String, String> body;

    public Map<String, AttributeValue> mapperToAttributeValueMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("id", AttributeValue.builder().s(id).build());
        map.put("principleId", AttributeValue.builder().s(String.valueOf(principleId)).build());
        map.put("createdAt", AttributeValue.builder().s(createdAt).build());

        for(Map.Entry<String, String> entry : body.entrySet()) {
            map.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
        }

        return map;
    }
}
