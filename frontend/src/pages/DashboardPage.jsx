import React, { useEffect, useState } from "react";
import { fetchCostReport, fetchTeams } from "../services/api";
import CostBreakdownChart from "../components/CostBreakdownChart";
import TeamUsageTable from "../components/TeamUsageTable";
import TimeFilter from "../components/TimeFilter";
import CostTrendChart from "../components/charts/CostTrendChart";
import ServiceBreakdownChart from "../components/charts/ServiceBreakdownChart";
import { colors, getCardStyle, getInputStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const DashboardPage = () => {
  const [costData, setCostData] = useState([]);
  const [teams, setTeams] = useState([]);
  const [selectedTeam, setSelectedTeam] = useState("platform");
  const [startDate, setStartDate] = useState("2025-01-01");
  const [endDate, setEndDate] = useState("2025-01-31");
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

  // Load cost data when team or date range changes
  useEffect(() => {
    const loadCostData = async () => {
      setLoading(true);
      setError(null);

      try {
        console.log(
          `Loading cost data for team: ${selectedTeam}, dates: ${startDate} to ${endDate}`,
        );
        const data = await fetchCostReport(selectedTeam, startDate, endDate);
        setCostData(data);
      } catch (err) {
        console.error("Failed to load cost data:", err);
        setError("Failed to load cost data");
        setCostData([]);
      } finally {
        setLoading(false);
      }
    };

    if (selectedTeam && startDate && endDate) {
      loadCostData();
    }
  }, [selectedTeam, startDate, endDate]);

  const handleTeamChange = (teamName) => {
    console.log("Team changed to:", teamName);
    setSelectedTeam(teamName);
  };

  const handleDateRangeChange = (newStartDate, newEndDate) => {
    console.log("Date range changed to:", newStartDate, "to", newEndDate);
    setStartDate(newStartDate);
    setEndDate(newEndDate);
  };

  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Team Selection */}
      <div
        style={{
          ...getCardStyle(),
          padding: isMobile ? "0.75rem" : "1rem",
          marginBottom: "2rem",
          display: "flex",
          alignItems: isMobile ? "flex-start" : "center",
          flexDirection: isMobile ? "column" : "row",
          gap: "1rem",
        }}
      >
        <label style={textStyles.label(colors.text.primary)}>Team:</label>
        <select
          value={selectedTeam}
          onChange={(e) => handleTeamChange(e.target.value)}
          style={{
            ...getInputStyle(),
            minWidth: isMobile ? "100%" : "200px",
            width: isMobile ? "100%" : "auto",
          }}
        >
          {teams.map((team) => (
            <option key={team.name} value={team.name}>
              {team.displayName || team.name}
            </option>
          ))}
        </select>
      </div>

      {/* Date Range Filter */}
      <TimeFilter
        startDate={startDate}
        endDate={endDate}
        onDateRangeChange={handleDateRangeChange}
      />

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
            🔄 Loading cost data for {selectedTeam} ({startDate} to {endDate}
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
          {/* Charts Row */}
          <div style={{ 
            display: "grid", 
            gridTemplateColumns: isMobile ? "1fr" : "1fr 1fr",
            gap: "1.5rem",
            marginBottom: "1.5rem"
          }}>
            {/* Service Breakdown Pie Chart */}
            <ServiceBreakdownChart 
              data={costData} 
              title="Cost Distribution by Service"
              height="350px"
            />
            
            {/* Cost Trend Line Chart (Mock data for now) */}
            <CostTrendChart 
              data={[
                { date: "Jan 2025", totalCost: 2856.42 },
                { date: "Feb 2025", totalCost: 3124.18 },
                { date: "Mar 2025", totalCost: 2945.73 },
                { date: "Apr 2025", totalCost: 3389.91 },
                { date: "May 2025", totalCost: 3567.29 },
                { date: "Jun 2025", totalCost: 3812.45 }
              ]}
              title="6-Month Cost Trend"
              height="350px"
            />
          </div>

          {/* Original table chart */}
          <CostBreakdownChart data={costData} />
          <TeamUsageTable data={costData} />

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
              <strong>📊 Data Summary:</strong> Showing {costData.length}{" "}
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
              <strong>🎯 Filter:</strong> Team "{selectedTeam}" | 📅 {startDate}{" "}
              to {endDate}
            </p>
            <p style={textStyles.caption(colors.text.secondary)}>
              <strong>💰 Total Cost:</strong> $
              {costData
                .reduce((sum, item) => sum + (item.totalCost || 0), 0)
                .toFixed(2)}
              {costData.length > 0 && (
                <span style={{ marginLeft: "1rem" }}>
                  <strong>📊 Avg per Service:</strong> $
                  {(
                    costData.reduce(
                      (sum, item) => sum + (item.totalCost || 0),
                      0,
                    ) / costData.length
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
