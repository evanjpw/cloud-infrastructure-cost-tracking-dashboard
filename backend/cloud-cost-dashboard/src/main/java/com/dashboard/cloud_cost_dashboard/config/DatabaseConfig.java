package com.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/cloud_costs")
            .username("root")
            .password("password123")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build();
    }
}
