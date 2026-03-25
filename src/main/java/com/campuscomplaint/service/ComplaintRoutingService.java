package com.campuscomplaint.service;

import com.campuscomplaint.dto.RepairAssignee;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class ComplaintRoutingService {

    private static final String PREFIX = "category.routing.";
    private static final String DEFAULT_CATEGORY = "Other";

    private final Properties routingProperties = new Properties();

    public ComplaintRoutingService() {
        loadRoutingProperties();
    }

    private void loadRoutingProperties() {
        ClassPathResource resource = new ClassPathResource("category-routing.properties");
        if (!resource.exists()) {
            System.err.println("category-routing.properties not found. Falling back to default assignee.");
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            routingProperties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Unable to load category-routing.properties. Falling back to default assignee.");
        }
    }

    public RepairAssignee getAssigneeForCategory(String category) {
        String resolvedCategory = routingProperties.containsKey(PREFIX + category + ".name")
                ? category
                : DEFAULT_CATEGORY;

        String name = routingProperties.getProperty(PREFIX + resolvedCategory + ".name", "Campus Maintenance Desk");
        String contact = routingProperties.getProperty(PREFIX + resolvedCategory + ".contact", "maintenance@campus.local");

        return new RepairAssignee(name, contact);
    }
}
