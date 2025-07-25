package com.dashboard.cloud_cost_dashboard.repository;

import com.dashboard.cloud_cost_dashboard.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCloudProviderId(Long providerId);

    List<Account> findByEnvironment(Account.Environment environment);
}
