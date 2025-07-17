package com.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usage_records")
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;
    private String service;
    private double usageAmount;
    private LocalDate usageDate;

    // Getters and setters omitted for brevity
}
