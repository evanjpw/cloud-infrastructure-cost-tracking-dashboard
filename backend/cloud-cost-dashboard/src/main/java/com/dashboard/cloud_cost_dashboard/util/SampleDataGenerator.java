package com.dashboard.cloud_cost_dashboard.util;

import com.dashboard.cloud_cost_dashboard.model.*;
import com.dashboard.cloud_cost_dashboard.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Component
@Profile("!test") // Don't run during tests
public class SampleDataGenerator implements CommandLineRunner {

    @Autowired UsageRecordRepository usageRecordRepository;

    @Autowired TeamRepository teamRepository;

    @Autowired ServiceRepository serviceRepository;

    @Autowired AccountRepository accountRepository;

    @Autowired CloudProviderRepository cloudProviderRepository;

    private final Random random = new Random();
    private static final int MONTHS_OF_DATA = 6;

    // Pricing constants (simplified)
    private static final Map<String, BigDecimal> SERVICE_HOURLY_RATES =
            new HashMap<>() {
                {
                    // AWS
                    put("AmazonEC2", new BigDecimal("0.0464")); // t3.medium
                    put("AmazonS3", new BigDecimal("0.023")); // per GB
                    put("AmazonRDS", new BigDecimal("0.096")); // db.t3.medium
                    put("AWSLambda", new BigDecimal("0.0000166667")); // per GB-second
                    put("AmazonCloudWatch", new BigDecimal("0.30")); // per metric
                    put("AmazonVPC", new BigDecimal("0.045")); // NAT Gateway
                    put("AmazonCloudFront", new BigDecimal("0.085")); // per GB
                    put("AmazonDynamoDB", new BigDecimal("0.25")); // per million requests
                    put("ElasticLoadBalancing", new BigDecimal("0.025")); // ALB
                    put("AmazonEKS", new BigDecimal("0.10")); // per cluster hour

                    // Azure
                    put("VirtualMachines", new BigDecimal("0.0496")); // D2s v3
                    put("Storage", new BigDecimal("0.0184")); // Hot tier per GB
                    put("SQLDatabase", new BigDecimal("0.0896")); // S2
                    put("Functions", new BigDecimal("0.000016")); // per GB-second
                    put("Monitor", new BigDecimal("0.25")); // per metric
                    put("VirtualNetwork", new BigDecimal("0.045")); // VPN Gateway
                    put("CDN", new BigDecimal("0.081")); // per GB
                    put("CosmosDB", new BigDecimal("0.008")); // per RU
                    put("LoadBalancer", new BigDecimal("0.025")); // Standard
                    put("ContainerInstances", new BigDecimal("0.10")); // AKS per node hour

                    // GCP
                    put("ComputeEngine", new BigDecimal("0.0475")); // n1-standard-1
                    put("CloudStorage", new BigDecimal("0.020")); // Standard per GB
                    put("CloudSQL", new BigDecimal("0.0935")); // db-n1-standard-1
                    put("CloudFunctions", new BigDecimal("0.0000166")); // per GB-second
                    put("Stackdriver", new BigDecimal("0.2580")); // per metric
                    put("VPCNetwork", new BigDecimal("0.050")); // Cloud VPN
                    put("CloudCDN", new BigDecimal("0.080")); // per GB
                    put("Firestore", new BigDecimal("0.06")); // per 100K reads
                    put("LoadBalancing", new BigDecimal("0.025")); // per rule
                    put("GKE", new BigDecimal("0.10")); // cluster management fee
                }
            };

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (usageRecordRepository.count() > 0) {
            System.out.println("Sample data already exists. Skipping generation.");
            return;
        }

        System.out.println("Generating sample cloud cost data...");

        // Create base data if it doesn't exist
        createBaseDataIfNeeded();

        List<Team> teams = teamRepository.findAll();
        List<Service> services = serviceRepository.findAll();
        List<Account> accounts = accountRepository.findAll();

        LocalDate startDate = LocalDate.now().minusMonths(MONTHS_OF_DATA);
        LocalDate endDate = LocalDate.now();

        int totalRecords = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            for (Team team : teams) {
                // Each team uses different services based on their profile
                List<Service> teamServices = getServicesForTeam(team, services);

                for (Service service : teamServices) {
                    // Generate usage for this service on this day
                    Account account = getAccountForService(service, accounts);

                    // Generate hourly usage with variations
                    for (int hour = 0; hour < 24; hour++) {
                        if (shouldGenerateUsageForHour(team, service, currentDate, hour)) {
                            UsageRecord record =
                                    generateUsageRecord(team, service, account, currentDate, hour);
                            usageRecordRepository.save(record);
                            totalRecords++;
                        }
                    }
                }
            }

            currentDate = currentDate.plusDays(1);

            // Log progress
            if (currentDate.getDayOfMonth() == 1) {
                System.out.println("Generated data up to: " + currentDate);
            }
        }

        System.out.println(
                "Sample data generation complete. Generated " + totalRecords + " usage records.");
    }

    private List<Service> getServicesForTeam(Team team, List<Service> allServices) {
        List<Service> teamServices = new ArrayList<>();
        String teamName = team.getName();

        for (Service service : allServices) {
            boolean includeService = false;

            // Platform team uses infrastructure services
            if (teamName.equals("platform")) {
                includeService =
                        service.getCategory().equals("Compute")
                                || service.getCategory().equals("Networking")
                                || service.getServiceCode().contains("EKS")
                                || service.getServiceCode().contains("GKE")
                                || service.getServiceCode().contains("ContainerInstances");
            }
            // Data analytics team uses data services
            else if (teamName.equals("data-analytics")) {
                includeService =
                        service.getCategory().equals("Database")
                                || service.getCategory().equals("Storage")
                                || service.getServiceCode().contains("Lambda")
                                || service.getServiceCode().contains("Functions");
            }
            // Mobile team uses CDN and API services
            else if (teamName.equals("mobile")) {
                includeService =
                        service.getServiceCode().contains("CloudFront")
                                || service.getServiceCode().contains("CDN")
                                || service.getServiceCode().contains("S3")
                                || service.getServiceCode().contains("Storage");
            }
            // Web frontend team
            else if (teamName.equals("web-frontend")) {
                includeService =
                        service.getServiceCode().contains("CloudFront")
                                || service.getServiceCode().contains("CDN")
                                || service.getServiceCode().contains("S3")
                                || service.getServiceCode().contains("Storage")
                                || service.getServiceCode().contains("LoadBalanc");
            }
            // DevOps team uses monitoring and infrastructure
            else if (teamName.equals("devops")) {
                includeService =
                        service.getCategory().equals("Management")
                                || service.getServiceCode().contains("EKS")
                                || service.getServiceCode().contains("GKE")
                                || service.getServiceCode().contains("ContainerInstances");
            }
            // Security team uses monitoring services
            else if (teamName.equals("security")) {
                includeService =
                        service.getCategory().equals("Management")
                                || service.getServiceCode().contains("VPC")
                                || service.getServiceCode().contains("VirtualNetwork");
            }
            // ML team uses compute and storage
            else if (teamName.equals("ml-team")) {
                includeService =
                        service.getCategory().equals("Compute")
                                || service.getCategory().equals("Storage")
                                || service.getServiceCode().contains("Lambda")
                                || service.getServiceCode().contains("Functions");
            }
            // Backend API team
            else if (teamName.equals("backend-api")) {
                includeService =
                        service.getCategory().equals("Compute")
                                || service.getCategory().equals("Database")
                                || service.getServiceCode().contains("LoadBalanc");
            }
            // QA automation team
            else if (teamName.equals("qa-automation")) {
                includeService =
                        service.getCategory().equals("Compute")
                                || service.getServiceCode().contains("Lambda")
                                || service.getServiceCode().contains("Functions");
            }
            // Infrastructure team uses everything
            else if (teamName.equals("infrastructure")) {
                includeService = random.nextDouble() < 0.3; // 30% chance for any service
            }

            if (includeService) {
                teamServices.add(service);
            }
        }

        return teamServices;
    }

    private Account getAccountForService(Service service, List<Account> accounts) {
        // 80% production, 20% development
        boolean isProduction = random.nextDouble() < 0.8;
        Account.Environment targetEnv =
                isProduction ? Account.Environment.PRODUCTION : Account.Environment.DEVELOPMENT;

        return accounts.stream()
                .filter(
                        a ->
                                a.getCloudProvider()
                                        .getId()
                                        .equals(service.getCloudProvider().getId()))
                .filter(a -> a.getEnvironment().equals(targetEnv))
                .findFirst()
                .orElse(accounts.get(0));
    }

    private boolean shouldGenerateUsageForHour(
            Team team, Service service, LocalDate date, int hour) {
        // Weekday vs weekend patterns
        boolean isWeekend = date.getDayOfWeek().getValue() >= 6;

        // Business hours (9-18 in UTC)
        boolean isBusinessHours = hour >= 9 && hour <= 18;

        // Base probability
        double probability = 0.3;

        // Adjust for patterns
        if (isBusinessHours && !isWeekend) {
            probability = 0.8; // Higher during business hours on weekdays
        } else if (isWeekend) {
            probability = 0.2; // Lower on weekends
        }

        // Some services run 24/7 (databases, load balancers)
        if (service.getCategory().equals("Database")
                || service.getServiceCode().contains("LoadBalanc")
                || service.getServiceCode().contains("EKS")
                || service.getServiceCode().contains("GKE")) {
            probability = 0.95; // Almost always running
        }

        // Batch jobs (data analytics) run at night
        if (team.getName().equals("data-analytics") && (hour < 6 || hour > 22)) {
            probability = 0.7;
        }

        return random.nextDouble() < probability;
    }

    private UsageRecord generateUsageRecord(
            Team team, Service service, Account account, LocalDate date, int hour) {
        UsageRecord record = new UsageRecord();
        record.setAccount(account);
        record.setTeam(team);
        record.setService(service);
        record.setUsageDate(date);
        record.setUsageHour(hour);

        // Generate resource ID and name
        String resourceType = getResourceTypeForService(service);
        String resourceId = generateResourceId(service, team);
        record.setResourceId(resourceId);
        record.setResourceName(resourceType + "-" + team.getName() + "-" + random.nextInt(100));
        record.setResourceType(resourceType);

        // Set region
        record.setRegion(getRandomRegion(service.getCloudProvider()));

        // Generate usage quantity and calculate cost
        BigDecimal[] usageAndCost = generateUsageAndCost(service, date, hour);
        record.setUsageQuantity(usageAndCost[0]);
        record.setUsageUnit(getUsageUnit(service));
        record.setUnitPrice(usageAndCost[1]);
        record.setTotalCost(usageAndCost[2]);
        record.setCurrency("USD");

        // Add tags
        Map<String, String> tags = new HashMap<>();
        tags.put("team", team.getName());
        tags.put("environment", account.getEnvironment().getValue());
        tags.put("cost-center", team.getCostCenter());
        tags.put("managed-by", "terraform");
        record.setTags(tags);

        return record;
    }

    private String getResourceTypeForService(Service service) {
        String serviceCode = service.getServiceCode();
        if (serviceCode.contains("EC2")
                || serviceCode.contains("VirtualMachines")
                || serviceCode.contains("ComputeEngine")) {
            return "instance";
        } else if (serviceCode.contains("S3") || serviceCode.contains("Storage")) {
            return "bucket";
        } else if (serviceCode.contains("RDS") || serviceCode.contains("SQL")) {
            return "database";
        } else if (serviceCode.contains("Lambda") || serviceCode.contains("Functions")) {
            return "function";
        } else if (serviceCode.contains("EKS")
                || serviceCode.contains("GKE")
                || serviceCode.contains("ContainerInstances")) {
            return "cluster";
        } else if (serviceCode.contains("LoadBalanc")) {
            return "load-balancer";
        } else if (serviceCode.contains("VPC") || serviceCode.contains("VirtualNetwork")) {
            return "network";
        }
        return "resource";
    }

    private String generateResourceId(Service service, Team team) {
        String prefix = "";
        CloudProvider provider = service.getCloudProvider();

        if (provider.getName().equals("aws")) {
            if (service.getServiceCode().contains("EC2")) prefix = "i-";
            else if (service.getServiceCode().contains("S3")) prefix = "s3://";
            else if (service.getServiceCode().contains("RDS")) prefix = "db-";
        } else if (provider.getName().equals("azure")) {
            prefix =
                    "/subscriptions/"
                            + UUID.randomUUID()
                            + "/resourceGroups/rg-"
                            + team.getName()
                            + "/";
        } else if (provider.getName().equals("gcp")) {
            prefix = "projects/my-project/";
        }

        return prefix + UUID.randomUUID().toString().substring(0, 12);
    }

    private String getRandomRegion(CloudProvider provider) {
        List<String> regions = new ArrayList<>();

        if (provider.getName().equals("aws")) {
            regions = Arrays.asList("us-east-1", "us-west-2", "eu-west-1", "ap-southeast-1");
        } else if (provider.getName().equals("azure")) {
            regions = Arrays.asList("eastus", "westus2", "northeurope", "southeastasia");
        } else if (provider.getName().equals("gcp")) {
            regions = Arrays.asList("us-central1", "us-west1", "europe-west1", "asia-southeast1");
        }

        return regions.get(random.nextInt(regions.size()));
    }

    private BigDecimal[] generateUsageAndCost(Service service, LocalDate date, int hour) {
        BigDecimal baseRate = SERVICE_HOURLY_RATES.get(service.getServiceCode());
        if (baseRate == null) baseRate = new BigDecimal("0.05");

        // Generate usage quantity with some randomness
        BigDecimal quantity = generateUsageQuantity(service, date, hour);

        // Apply time-based variations (peak hours cost more)
        BigDecimal hourlyMultiplier = BigDecimal.ONE;
        if (hour >= 9 && hour <= 17) { // Business hours
            hourlyMultiplier = new BigDecimal("1.2");
        }

        // Apply seasonal variations
        BigDecimal seasonalMultiplier = BigDecimal.ONE;
        int month = date.getMonthValue();
        if (month == 11 || month == 12) { // Black Friday / Holiday season
            seasonalMultiplier = new BigDecimal("1.5");
        }

        BigDecimal adjustedRate = baseRate.multiply(hourlyMultiplier).multiply(seasonalMultiplier);
        BigDecimal totalCost = quantity.multiply(adjustedRate).setScale(6, RoundingMode.HALF_UP);

        return new BigDecimal[] {quantity, adjustedRate, totalCost};
    }

    private BigDecimal generateUsageQuantity(Service service, LocalDate date, int hour) {
        String serviceCode = service.getServiceCode();

        // Base quantities for different service types
        if (serviceCode.contains("EC2")
                || serviceCode.contains("VirtualMachines")
                || serviceCode.contains("ComputeEngine")) {
            // Number of instance hours (1-20 instances)
            return new BigDecimal(1 + random.nextInt(20));
        } else if (serviceCode.contains("S3") || serviceCode.contains("Storage")) {
            // GB of storage (10-1000 GB)
            return new BigDecimal(10 + random.nextInt(990));
        } else if (serviceCode.contains("RDS") || serviceCode.contains("SQL")) {
            // Database instance hours (1-5 instances)
            return new BigDecimal(1 + random.nextInt(5));
        } else if (serviceCode.contains("Lambda") || serviceCode.contains("Functions")) {
            // GB-seconds (100-10000)
            return new BigDecimal(100 + random.nextInt(9900));
        } else if (serviceCode.contains("CloudFront") || serviceCode.contains("CDN")) {
            // GB transferred (1-500 GB)
            return new BigDecimal(1 + random.nextInt(500));
        }

        // Default quantity
        return new BigDecimal(1 + random.nextInt(100));
    }

    private String getUsageUnit(Service service) {
        String serviceCode = service.getServiceCode();

        if (serviceCode.contains("EC2")
                || serviceCode.contains("VirtualMachines")
                || serviceCode.contains("ComputeEngine")) {
            return "Instance-Hours";
        } else if (serviceCode.contains("S3") || serviceCode.contains("Storage")) {
            return "GB-Month";
        } else if (serviceCode.contains("RDS") || serviceCode.contains("SQL")) {
            return "Instance-Hours";
        } else if (serviceCode.contains("Lambda") || serviceCode.contains("Functions")) {
            return "GB-Seconds";
        } else if (serviceCode.contains("CloudFront") || serviceCode.contains("CDN")) {
            return "GB";
        } else if (serviceCode.contains("LoadBalanc")) {
            return "LCU-Hours";
        }

        return "Units";
    }

    private void createBaseDataIfNeeded() {
        // Create cloud providers if they don't exist
        if (cloudProviderRepository.count() == 0) {
            CloudProvider aws = new CloudProvider();
            aws.setName("aws");
            aws.setDisplayName("Amazon Web Services");
            aws.setIconUrl(
                    "https://d1.awsstatic.com/logos/aws-logo-lockups/poweredbyaws/PB_AWS_logo_RGB_REV_SQ.8c88ac215fe4e441dc42865dd6962ed4f444a90d.png");
            cloudProviderRepository.save(aws);

            CloudProvider azure = new CloudProvider();
            azure.setName("azure");
            azure.setDisplayName("Microsoft Azure");
            azure.setIconUrl("https://azure.microsoft.com/svghandler/azure-logo");
            cloudProviderRepository.save(azure);

            CloudProvider gcp = new CloudProvider();
            gcp.setName("gcp");
            gcp.setDisplayName("Google Cloud Platform");
            gcp.setIconUrl(
                    "https://cloud.google.com/_static/cloud/images/social-icon-google-cloud-1200-630.png");
            cloudProviderRepository.save(gcp);
        }

        // Create teams if they don't exist
        if (teamRepository.count() == 0) {
            Team platform = new Team();
            platform.setName("platform");
            platform.setDisplayName("Platform Engineering");
            platform.setDepartment("Engineering");
            platform.setCostCenter("ENG-001");
            platform.setManagerEmail("platform-manager@company.com");
            teamRepository.save(platform);

            Team frontend = new Team();
            frontend.setName("frontend");
            frontend.setDisplayName("Frontend Development");
            frontend.setDepartment("Engineering");
            frontend.setCostCenter("ENG-002");
            frontend.setManagerEmail("frontend-manager@company.com");
            teamRepository.save(frontend);

            Team backend = new Team();
            backend.setName("backend");
            backend.setDisplayName("Backend Development");
            backend.setDepartment("Engineering");
            backend.setCostCenter("ENG-003");
            backend.setManagerEmail("backend-manager@company.com");
            teamRepository.save(backend);

            Team data = new Team();
            data.setName("data");
            data.setDisplayName("Data Engineering");
            data.setDepartment("Engineering");
            data.setCostCenter("ENG-004");
            data.setManagerEmail("data-manager@company.com");
            teamRepository.save(data);

            Team ml = new Team();
            ml.setName("ml");
            ml.setDisplayName("Machine Learning");
            ml.setDepartment("Engineering");
            ml.setCostCenter("ENG-005");
            ml.setManagerEmail("ml-manager@company.com");
            teamRepository.save(ml);
        }

        // Create services if they don't exist
        if (serviceRepository.count() == 0) {
            List<CloudProvider> providers = cloudProviderRepository.findAll();
            CloudProvider aws =
                    providers.stream()
                            .filter(p -> p.getName().equals("aws"))
                            .findFirst()
                            .orElse(null);
            CloudProvider azure =
                    providers.stream()
                            .filter(p -> p.getName().equals("azure"))
                            .findFirst()
                            .orElse(null);
            CloudProvider gcp =
                    providers.stream()
                            .filter(p -> p.getName().equals("gcp"))
                            .findFirst()
                            .orElse(null);

            // AWS Services
            if (aws != null) {
                createService(aws, "AmazonEC2", "Amazon Elastic Compute Cloud", "Compute");
                createService(aws, "AmazonS3", "Amazon Simple Storage Service", "Storage");
                createService(aws, "AmazonRDS", "Amazon Relational Database Service", "Database");
                createService(aws, "AWSLambda", "AWS Lambda", "Compute");
                createService(aws, "AmazonVPC", "Amazon Virtual Private Cloud", "Networking");
                createService(aws, "ElasticLoadBalancing", "Elastic Load Balancing", "Networking");
                createService(aws, "AmazonCloudFront", "Amazon CloudFront", "Content Delivery");
                createService(aws, "AmazonEKS", "Amazon Elastic Kubernetes Service", "Containers");
            }

            // Azure Services
            if (azure != null) {
                createService(azure, "VirtualMachines", "Azure Virtual Machines", "Compute");
                createService(azure, "BlobStorage", "Azure Blob Storage", "Storage");
                createService(azure, "SQLDatabase", "Azure SQL Database", "Database");
                createService(azure, "Functions", "Azure Functions", "Compute");
                createService(azure, "VirtualNetwork", "Azure Virtual Network", "Networking");
                createService(azure, "LoadBalancer", "Azure Load Balancer", "Networking");
                createService(azure, "CDN", "Azure Content Delivery Network", "Content Delivery");
                createService(azure, "AKS", "Azure Kubernetes Service", "Containers");
            }

            // GCP Services
            if (gcp != null) {
                createService(gcp, "ComputeEngine", "Google Compute Engine", "Compute");
                createService(gcp, "CloudStorage", "Google Cloud Storage", "Storage");
                createService(gcp, "CloudSQL", "Google Cloud SQL", "Database");
                createService(gcp, "CloudFunctions", "Google Cloud Functions", "Compute");
                createService(gcp, "VPCNetwork", "Google VPC Network", "Networking");
                createService(gcp, "LoadBalancing", "Google Cloud Load Balancing", "Networking");
                createService(gcp, "CloudCDN", "Google Cloud CDN", "Content Delivery");
                createService(gcp, "GKE", "Google Kubernetes Engine", "Containers");
            }
        }

        // Create accounts if they don't exist
        if (accountRepository.count() == 0) {
            List<CloudProvider> providers = cloudProviderRepository.findAll();

            for (CloudProvider provider : providers) {
                // Production account
                Account prodAccount = new Account();
                prodAccount.setCloudProvider(provider);
                prodAccount.setAccountId("prod-" + provider.getName() + "-123456789");
                prodAccount.setAccountName("Production - " + provider.getDisplayName());
                prodAccount.setEnvironment(Account.Environment.PRODUCTION);
                prodAccount.setStatus(Account.AccountStatus.ACTIVE);
                accountRepository.save(prodAccount);

                // Staging account
                Account stagingAccount = new Account();
                stagingAccount.setCloudProvider(provider);
                stagingAccount.setAccountId("staging-" + provider.getName() + "-987654321");
                stagingAccount.setAccountName("Staging - " + provider.getDisplayName());
                stagingAccount.setEnvironment(Account.Environment.STAGING);
                stagingAccount.setStatus(Account.AccountStatus.ACTIVE);
                accountRepository.save(stagingAccount);

                // Development account
                Account devAccount = new Account();
                devAccount.setCloudProvider(provider);
                devAccount.setAccountId("dev-" + provider.getName() + "-456789123");
                devAccount.setAccountName("Development - " + provider.getDisplayName());
                devAccount.setEnvironment(Account.Environment.DEVELOPMENT);
                devAccount.setStatus(Account.AccountStatus.ACTIVE);
                accountRepository.save(devAccount);
            }
        }
    }

    private void createService(
            CloudProvider provider, String serviceCode, String serviceName, String category) {
        Service service = new Service();
        service.setCloudProvider(provider);
        service.setServiceCode(serviceCode);
        service.setServiceName(serviceName);
        service.setCategory(category);
        serviceRepository.save(service);
    }
}
