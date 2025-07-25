package com.dashboard.cloud_cost_dashboard.repository;

import com.dashboard.cloud_cost_dashboard.model.CloudProvider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloudProviderRepository extends JpaRepository<CloudProvider, Long> {
    Optional<CloudProvider> findByName(String name);
}
