package com.dashboard.cloud_cost_dashboard.repository;

import com.dashboard.cloud_cost_dashboard.model.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByCategory(String category);

    List<Service> findByCloudProviderId(Long providerId);
}
