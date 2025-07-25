package com.dashboard.cloud_cost_dashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "usage_records")
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(length = 50)
    private String region;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "usage_hour")
    private Integer usageHour = 0;

    @Column(name = "usage_quantity", precision = 20, scale = 6)
    private BigDecimal usageQuantity = BigDecimal.ZERO;

    @Column(name = "usage_unit", length = 50)
    private String usageUnit;

    @Column(name = "unit_price", precision = 12, scale = 6)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 12, scale = 6)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "USD";

    @ElementCollection
    @CollectionTable(
            name = "usage_record_tags",
            joinColumns = @JoinColumn(name = "usage_record_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Convenience methods for backward compatibility
    @JsonIgnore
    public String getTeamName() {
        return team != null ? team.getName() : null;
    }

    @JsonIgnore
    public String getServiceName() {
        return service != null ? service.getServiceName() : null;
    }

    @JsonIgnore
    public double getUsageAmount() {
        return usageQuantity != null ? usageQuantity.doubleValue() : 0.0;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDate usageDate) {
        this.usageDate = usageDate;
    }

    public Integer getUsageHour() {
        return usageHour;
    }

    public void setUsageHour(Integer usageHour) {
        this.usageHour = usageHour;
    }

    public BigDecimal getUsageQuantity() {
        return usageQuantity;
    }

    public void setUsageQuantity(BigDecimal usageQuantity) {
        this.usageQuantity = usageQuantity;
    }

    public String getUsageUnit() {
        return usageUnit;
    }

    public void setUsageUnit(String usageUnit) {
        this.usageUnit = usageUnit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
