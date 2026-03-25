package com.campuscomplaint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Campus Complaint System backend.
 * Enables scheduling for automatic complaint escalation.
 */
@SpringBootApplication
@EnableScheduling
public class CampusComplaintApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusComplaintApplication.class, args);
    }
}
