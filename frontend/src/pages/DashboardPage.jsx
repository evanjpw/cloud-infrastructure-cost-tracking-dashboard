import React, { useEffect, useState } from "react";
import { fetchCostReport, fetchTeams } from "../services/api";
import CostBreakdownChart from "../components/CostBreakdownChart";
import TeamUsageTable from "../components/TeamUsageTable";
import CostTrendChart from "../components/charts/CostTrendChart";
import ServiceBreakdownChart from "../components/charts/ServiceBreakdownChart";
import GranularitySelector from "../components/GranularitySelector";
import DateRangePicker from "../components/DateRangePicker";
import KPIDashboard from "../components/KPIDashboard";
import MultiSelectFilter from "../components/MultiSelectFilter";
import QuickSearch from "../components/QuickSearch";
import SavedViews from "../components/SavedViews";
import { colors, getCardStyle, getInputStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";
import { 
  aggregateDataByGranularity, 
  calculatePeriodChanges,
  suggestOptimalGranularity 
} from "../utils/dataAggregation";

const DashboardPage = () => {
  const [costData, setCostData] = useState([]);
  const [filteredCostData, setFilteredCostData] = useState([]);
  const [teams, setTeams] = useState([]);
  const [selectedTeams, setSelectedTeams] = useState(["platform"]);
  const [selectedServices, setSelectedServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [startDate, setStartDate] = useState("2025-01-01");
  const [endDate, setEndDate] = useState("2025-01-31");
  const [granularity, setGranularity] = useState("daily");
  const [aggregatedTrendData, setAggregatedTrendData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkIsMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkIsMobile();
    window.addEventListener("resize", checkIsMobile);
    return () => window.removeEventListener("resize", checkIsMobile);
  }, []);

  // Load teams on component mount
  useEffect(() => {
    const loadTeams = async () => {
      try {
        const teamsData = await fetchTeams();
        setTeams(teamsData);
      } catch (err) {
        console.error("Failed to load teams:", err);
        setError("Failed to load teams");
      }
    };
    loadTeams();
  }, []);

  // Load cost data when teams or date range changes
  useEffect(() => {
    const loadCostData = async () => {
      setLoading(true);
      setError(null);

      try {
        // Load data for all selected teams
        console.log(
          `Loading cost data for teams: ${selectedTeams.join(", ")}, dates: ${startDate} to ${endDate}`,
        );
        
        // For now, load data for the first selected team (API doesn't support multi-team yet)
        const primaryTeam = selectedTeams[0] || "platform";
        const data = await fetchCostReport(primaryTeam, startDate, endDate);
        setCostData(data);
      } catch (err) {
        console.error("Failed to load cost data:", err);
        setError("Failed to load cost data");
        setCostData([]);
      } finally {
        setLoading(false);
      }
    };

    if (selectedTeams.length > 0 && startDate && endDate) {
      loadCostData();
    }
  }, [selectedTeams, startDate, endDate]);

  // Filter cost data based on selected services and search term
  useEffect(() => {
    let filtered = [...costData];

    // Filter by selected services
    if (selectedServices.length > 0) {
      filtered = filtered.filter(item => 
        selectedServices.includes(item.service || item.serviceName)
      );
    }

    // Filter by search term
    if (searchTerm.trim()) {
      const search = searchTerm.toLowerCase();
      filtered = filtered.filter(item => 
        (item.service || item.serviceName || "").toLowerCase().includes(search) ||
        (item.team || "").toLowerCase().includes(search) ||
        (item.totalCost || 0).toString().includes(search)
      );
    }

    setFilteredCostData(filtered);
  }, [costData, selectedServices, searchTerm]);

  // Generate aggregated trend data when granularity or filtered data changes
  useEffect(() => {
    if (filteredCostData && filteredCostData.length > 0) {
      const aggregated = aggregateDataByGranularity(filteredCostData, granularity, startDate, endDate);
      const withChanges = calculatePeriodChanges(aggregated);
      setAggregatedTrendData(withChanges);
    } else {
      setAggregatedTrendData([]);
    }
  }, [filteredCostData, granularity, startDate, endDate]);

  // Suggest optimal granularity when date range changes
  useEffect(() => {
    const suggested = suggestOptimalGranularity(startDate, endDate);
    if (suggested !== granularity) {
      console.log(`Suggested granularity: ${suggested} (current: ${granularity})`);
    }
  }, [startDate, endDate, granularity]);

  // Generate filter options
  const teamOptions = teams.map(team => ({
    value: team.name,
    label: team.displayName || team.name,
    count: costData.filter(item => item.team === team.name).length
  }));

  const serviceOptions = [...new Set(costData.map(item => item.service || item.serviceName))]
    .filter(Boolean)
    .map(service => ({
      value: service,
      label: service,
      count: costData.filter(item => (item.service || item.serviceName) === service).length
    }))
    .sort((a, b) => b.count - a.count);

  // Generate search suggestions
  const searchSuggestions = [
    ...serviceOptions.map(service => ({
      label: service.label,
      category: "Service",
      description: `${service.count} records`,
      value: `$${costData.filter(item => (item.service || item.serviceName) === service.value)
        .reduce((sum, item) => sum + (item.totalCost || 0), 0).toFixed(0)}`
    })),
    ...teamOptions.map(team => ({
      label: team.label,
      category: "Team", 
      description: `${team.count} services`,
      value: team.value
    })),
    // Add cost-based suggestions
    { label: "High cost services (>$1000)", category: "Cost", description: "Services over $1000" },
    { label: "Low cost services (<$100)", category: "Cost", description: "Services under $100" },
  ];

  const handleTeamsChange = (newSelectedTeams) => {
    console.log("Teams changed to:", newSelectedTeams);
    setSelectedTeams(newSelectedTeams);
  };

  const handleServicesChange = (newSelectedServices) => {
    console.log("Services changed to:", newSelectedServices);
    setSelectedServices(newSelectedServices);
  };

  const handleSearchChange = (newSearchTerm) => {
    console.log("Search changed to:", newSearchTerm);
    setSearchTerm(newSearchTerm);
  };

  const handleDateRangeChange = (newStartDate, newEndDate) => {
    console.log("Date range changed to:", newStartDate, "to", newEndDate);
    setStartDate(newStartDate);
    setEndDate(newEndDate);
  };

  const handleLoadSavedView = (filters) => {
    console.log("Loading saved view:", filters);
    setSelectedTeams(filters.selectedTeams || []);
    setSelectedServices(filters.selectedServices || []);
    setSearchTerm(filters.searchTerm || "");
    setStartDate(filters.startDate || "2025-01-01");
    setEndDate(filters.endDate || "2025-01-31");
    setGranularity(filters.granularity || "daily");
  };

  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Advanced Filters Section */}
      <div
        style={{
          ...getCardStyle(),
          padding: isMobile ? "1rem" : "1.5rem",
          marginBottom: "2rem",
        }}
      >
        <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: "1.5rem" }}>
          🔍 Filters & Search
        </h3>
        
        <div style={{ 
          display: "grid", 
          gridTemplateColumns: isMobile ? "1fr" : "1fr 1fr", 
          gap: "1.5rem",
          marginBottom: "1.5rem"
        }}>
          {/* Multi-Select Team Filter */}
          <MultiSelectFilter
            label="Teams"
            options={teamOptions}
            selected={selectedTeams}
            onChange={handleTeamsChange}
            placeholder="Select teams..."
            icon="👥"
            isMobile={isMobile}
          />

          {/* Multi-Select Service Filter */}
          <MultiSelectFilter
            label="Services"
            options={serviceOptions}
            selected={selectedServices}
            onChange={handleServicesChange}
            placeholder="Select services..."
            icon="🔧"
            isMobile={isMobile}
          />
        </div>

        {/* Quick Search */}
        <QuickSearch
          placeholder="Search services, teams, or costs..."
          onSearch={handleSearchChange}
          suggestions={searchSuggestions}
          isMobile={isMobile}
        />

        {/* Filter Summary */}
        {(selectedTeams.length > 0 || selectedServices.length > 0 || searchTerm) && (
          <div style={{ 
            marginTop: "1rem", 
            padding: "0.75rem",
            backgroundColor: colors.primary[50],
            borderRadius: "6px",
            border: `1px solid ${colors.primary[200]}`
          }}>
            <p style={{ ...textStyles.caption(colors.primary[700]), margin: 0 }}>
              <strong>Active Filters:</strong> 
              {selectedTeams.length > 0 && ` Teams (${selectedTeams.length})`}
              {selectedServices.length > 0 && ` Services (${selectedServices.length})`}
              {searchTerm && ` Search: "${searchTerm}"`}
              {` • Showing ${filteredCostData.length} of ${costData.length} records`}
            </p>
          </div>
        )}

        {/* Saved Views */}
        <SavedViews
          currentFilters={{
            selectedTeams,
            selectedServices,
            searchTerm,
            startDate,
            endDate,
            granularity,
          }}
          onLoadView={handleLoadSavedView}
          isMobile={isMobile}
        />
      </div>

      {/* Enhanced Date Range Picker */}
      <DateRangePicker
        startDate={startDate}
        endDate={endDate}
        onDateRangeChange={handleDateRangeChange}
        granularity={granularity}
        isMobile={isMobile}
      />

      {/* Granularity Controls */}
      <div
        style={{
          ...getCardStyle(),
          padding: isMobile ? "1rem" : "1.5rem",
          marginBottom: "1.5rem",
        }}
      >
        <GranularitySelector
          selectedGranularity={granularity}
          onGranularityChange={setGranularity}
          dateRange={{ start: startDate, end: endDate }}
          isMobile={isMobile}
        />
      </div>

      {/* Loading State */}
      {loading && (
        <div
          style={{
            padding: "2rem",
            textAlign: "center",
            backgroundColor: colors.primary[50],
            border: `1px solid ${colors.primary[500]}`,
            borderRadius: "8px",
            marginBottom: "2rem",
          }}
        >
          <p
            style={{
              ...textStyles.body(colors.primary[700]),
              fontWeight: "600",
              margin: "0",
            }}
          >
            🔄 Loading cost data for {selectedTeams.join(", ")} ({startDate} to {endDate}
            )...
          </p>
          <div
            style={{
              width: "100%",
              height: "4px",
              backgroundColor: colors.primary[100],
              borderRadius: "2px",
              marginTop: "0.5rem",
              overflow: "hidden",
            }}
          >
            <div
              style={{
                width: "30%",
                height: "100%",
                backgroundColor: colors.primary[500],
                borderRadius: "2px",
                animation: "loading 1.5s ease-in-out infinite",
              }}
            />
          </div>
          <style jsx>{`
            @keyframes loading {
              0% {
                transform: translateX(-100%);
              }
              50% {
                transform: translateX(300%);
              }
              100% {
                transform: translateX(-100%);
              }
            }
          `}</style>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div
          style={{
            padding: "1rem",
            backgroundColor: "#ffebee",
            border: `1px solid ${colors.error}`,
            borderRadius: "8px",
            color: "#d32f2f",
            marginBottom: "2rem",
          }}
        >
          <strong>Error:</strong> {error}
        </div>
      )}

      {/* Cost Data Display */}
      {!loading && !error && (
        <>
          {/* KPI Dashboard - Top Section */}
          <KPIDashboard
            costData={filteredCostData}
            aggregatedTrendData={aggregatedTrendData}
            granularity={granularity}
            startDate={startDate}
            endDate={endDate}
            selectedTeam={selectedTeams.join(", ")}
            isMobile={isMobile}
          />

          {/* Charts Row */}
          <div style={{ 
            display: "grid", 
            gridTemplateColumns: isMobile ? "1fr" : "1fr 1fr",
            gap: "1.5rem",
            marginBottom: "1.5rem"
          }}>
            {/* Service Breakdown Pie Chart */}
            <ServiceBreakdownChart 
              data={filteredCostData} 
              title="Cost Distribution by Service"
              height="350px"
            />
            
            {/* Cost Trend Line Chart with Real Aggregated Data */}
            <CostTrendChart 
              data={aggregatedTrendData.map(item => ({
                date: item.displayDate,
                totalCost: item.totalCost,
                change: item.change,
                changePercent: item.changePercent
              }))}
              title={`Cost Trend (${granularity.charAt(0).toUpperCase() + granularity.slice(1)})`}
              height="350px"
            />
          </div>

          {/* Original table chart */}
          <CostBreakdownChart data={filteredCostData} />
          <TeamUsageTable data={filteredCostData} />

          {/* Data Status */}
          <div
            style={{
              marginTop: "2rem",
              padding: "1.5rem",
              ...getCardStyle(),
            }}
          >
            <p
              style={{
                ...textStyles.body(colors.text.primary),
                marginBottom: "0.5rem",
              }}
            >
              <strong>📊 Data Summary:</strong> Showing {filteredCostData.length} of {costData.length}{" "}
              services
              {costData.some((item) => item.note) ? (
                <span style={{ color: colors.warning, marginLeft: "1rem" }}>
                  ⚠️ Using fallback data (no data found for this date range)
                </span>
              ) : (
                <span style={{ color: colors.success, marginLeft: "1rem" }}>
                  ✅ Real data from database
                </span>
              )}
            </p>
            <p
              style={{
                ...textStyles.caption(colors.text.secondary),
                marginBottom: "0.5rem",
              }}
            >
              <strong>🎯 Filters:</strong> Teams ({selectedTeams.length}) | Services ({selectedServices.length}) | 📅 {startDate}{" "}
              to {endDate}
            </p>
            <p style={textStyles.caption(colors.text.secondary)}>
              <strong>💰 Filtered Total:</strong> $
              {filteredCostData
                .reduce((sum, item) => sum + (item.totalCost || 0), 0)
                .toFixed(2)}
              {filteredCostData.length > 0 && (
                <span style={{ marginLeft: "1rem" }}>
                  <strong>📊 Avg per Service:</strong> $
                  {(
                    filteredCostData.reduce(
                      (sum, item) => sum + (item.totalCost || 0),
                      0,
                    ) / filteredCostData.length
                  ).toFixed(2)}
                </span>
              )}
            </p>
          </div>
        </>
      )}
    </div>
  );
};

export default DashboardPage;