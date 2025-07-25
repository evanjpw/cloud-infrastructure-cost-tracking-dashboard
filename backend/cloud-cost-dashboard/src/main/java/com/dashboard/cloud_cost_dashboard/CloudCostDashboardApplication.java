package com.dashboard.cloud_cost_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
        basePackages = {
            "com.dashboard.cloud_cost_dashboard",
            "com.dashboard.service",
            "com.dashboard.repository",
            "com.dashboard.util"
        })
public class CloudCostDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudCostDashboardApplication.class, args);
    }
}
