import React, { useState, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";
import { fetchCostReport, fetchTeams } from "../services/api";
import { generateOptimizationRecommendations } from "../utils/optimizationEngine";
import OptimizationSummary from "../components/optimization/OptimizationSummary";
import RecommendationCard from "../components/optimization/RecommendationCard";
import AnomalyChart from "../components/optimization/AnomalyChart";

const CostAnalysisPage = () => {
  const [costData, setCostData] = useState([]);
  const [teams, setTeams] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedFilter, setSelectedFilter] = useState('all');
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkIsMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkIsMobile();
    window.addEventListener("resize", checkIsMobile);
    return () => window.removeEventListener("resize", checkIsMobile);
  }, []);

  // Load data on component mount
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Load teams and cost data
        const [teamsData, costDataResult] = await Promise.all([
          fetchTeams(),
          fetchCostReport("platform", "2025-01-01", "2025-01-31")
        ]);

        setTeams(teamsData);
        setCostData(costDataResult);

        // Generate optimization recommendations
        const optimizationRecs = generateOptimizationRecommendations(costDataResult);
        setRecommendations(optimizationRecs);

      } catch (err) {
        console.error('Failed to load optimization data:', err);
        setError('Failed to load cost optimization data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Filter recommendations based on selected filter
  const filteredRecommendations = recommendations.filter(rec => {
    if (selectedFilter === 'all') return true;
    return rec.type === selectedFilter || rec.impact === selectedFilter;
  });

  // Calculate total cost for optimization percentage
  const totalCost = costData.reduce((sum, item) => sum + (item.totalCost || 0), 0);

  const handleAcceptRecommendation = (recommendationId) => {
    console.log('Accepting recommendation:', recommendationId);
    setRecommendations(prev => 
      prev.filter(rec => rec.id !== recommendationId)
    );
    // In a real implementation, this would call an API to apply the recommendation
  };

  const handleDismissRecommendation = (recommendationId) => {
    console.log('Dismissing recommendation:', recommendationId);
    setRecommendations(prev => 
      prev.filter(rec => rec.id !== recommendationId)
    );
  };

  const filterOptions = [
    { value: 'all', label: 'All Recommendations', icon: '📋' },
    { value: 'high', label: 'High Impact', icon: '🔥' },
    { value: 'rightsizing', label: 'Right-sizing', icon: '📏' },
    { value: 'reserved_instance', label: 'Reserved Instances', icon: '💰' },
    { value: 'unused_resource', label: 'Unused Resources', icon: '🗑️' },
    { value: 'anomaly', label: 'Anomalies', icon: '🚨' },
    { value: 'optimization', label: 'Governance', icon: '⚡' }
  ];

  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Cost Optimization
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          AI-powered cost optimization with actionable recommendations and anomaly detection
        </p>
      </div>

      {loading ? (
        <div style={{
          ...getCardStyle(),
          padding: "2rem",
          textAlign: "center"
        }}>
          <p style={textStyles.body(colors.text.secondary)}>
            🔄 Analyzing your cloud costs and generating optimization recommendations...
          </p>
        </div>
      ) : error ? (
        <div style={{
          ...getCardStyle(),
          padding: "2rem",
          backgroundColor: colors.error + "10",
          border: `1px solid ${colors.error}40`
        }}>
          <p style={textStyles.body(colors.error)}>
            ❌ {error}
          </p>
        </div>
      ) : (
        <>
          {/* Optimization Summary */}
          <OptimizationSummary 
            recommendations={recommendations}
            totalCost={totalCost}
            isMobile={isMobile}
          />

          {/* Anomaly Detection Chart */}
          <AnomalyChart 
            data={costData}
            title="Spending Anomaly Detection"
            isMobile={isMobile}
          />

          {/* Recommendations Section */}
          <div style={{
            ...getCardStyle(),
            padding: isMobile ? "1rem" : "1.5rem",
            marginBottom: "2rem"
          }}>
            {/* Filter Bar */}
            <div style={{ 
              display: "flex", 
              justifyContent: "space-between", 
              alignItems: "center",
              marginBottom: "1.5rem",
              flexWrap: "wrap",
              gap: "1rem"
            }}>
              <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
                💡 Optimization Recommendations ({filteredRecommendations.length})
              </h3>
              
              <div style={{ 
                display: "flex", 
                gap: "0.5rem",
                flexWrap: "wrap",
                alignItems: "center"
              }}>
                <span style={{ ...textStyles.body(colors.text.secondary), marginRight: "0.5rem" }}>
                  Filter:
                </span>
                {filterOptions.map(option => (
                  <button
                    key={option.value}
                    onClick={() => setSelectedFilter(option.value)}
                    style={{
                      padding: "0.5rem 1rem",
                      backgroundColor: selectedFilter === option.value ? colors.primary[500] : 'transparent',
                      color: selectedFilter === option.value ? colors.white : colors.text.secondary,
                      border: `1px solid ${selectedFilter === option.value ? colors.primary[500] : colors.gray[300]}`,
                      borderRadius: "6px",
                      fontSize: "0.8rem",
                      fontWeight: "500",
                      cursor: "pointer",
                      transition: "all 0.2s ease",
                      display: "flex",
                      alignItems: "center",
                      gap: "0.25rem"
                    }}
                    onMouseEnter={(e) => {
                      if (selectedFilter !== option.value) {
                        e.target.style.backgroundColor = colors.gray[100];
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (selectedFilter !== option.value) {
                        e.target.style.backgroundColor = 'transparent';
                      }
                    }}
                  >
                    <span style={{ fontSize: "0.9em" }}>{option.icon}</span>
                    {!isMobile && option.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Recommendations List */}
            {filteredRecommendations.length === 0 ? (
              <div style={{
                textAlign: "center",
                padding: "2rem",
                backgroundColor: colors.gray[50],
                borderRadius: "6px"
              }}>
                <div style={{ fontSize: "3rem", marginBottom: "1rem" }}>✨</div>
                <h4 style={textStyles.cardTitle(colors.text.primary)}>
                  {recommendations.length === 0 ? "Great Job!" : "No recommendations found"}
                </h4>
                <p style={textStyles.body(colors.text.secondary)}>
                  {recommendations.length === 0 
                    ? "Your cloud infrastructure is already well-optimized!"
                    : `No recommendations match the selected filter. Try selecting "All Recommendations".`
                  }
                </p>
              </div>
            ) : (
              <div>
                {filteredRecommendations.map(recommendation => (
                  <RecommendationCard
                    key={recommendation.id}
                    recommendation={recommendation}
                    onAccept={handleAcceptRecommendation}
                    onDismiss={handleDismissRecommendation}
                    isMobile={isMobile}
                  />
                ))}
              </div>
            )}
          </div>

          {/* Implementation Note */}
          <div style={{
            padding: "1.5rem",
            backgroundColor: colors.primary[25],
            borderRadius: "8px",
            border: `1px solid ${colors.primary[200]}`
          }}>
            <h4 style={{ ...textStyles.cardTitle(colors.primary[700]), marginBottom: "0.75rem" }}>
              🎭 Cost Optimization - Phase 2.2 Implementation
            </h4>
            <p style={{ ...textStyles.body(colors.primary[700]), marginBottom: "0.5rem" }}>
              <strong>✅ Implemented Features:</strong>
            </p>
            <ul style={{ 
              color: colors.primary[700], 
              marginBottom: "1rem",
              paddingLeft: "1.5rem"
            }}>
              <li>AI-powered recommendation engine with multiple optimization types</li>
              <li>Right-sizing analysis for over-provisioned resources</li>
              <li>Reserved Instance planning with ROI calculations</li>
              <li>Unused resource detection and cleanup recommendations</li>
              <li>Anomaly detection with statistical threshold analysis</li>
              <li>Interactive recommendation cards with detailed implementation steps</li>
              <li>Optimization summary with potential savings and ROI metrics</li>
              <li>Implementation roadmap with timeline estimates</li>
            </ul>
            <p style={{ ...textStyles.caption(colors.primary[700]), margin: 0 }}>
              <strong>🔧 Technical Implementation:</strong> Recommendations are generated using real cost data patterns, 
              statistical analysis for anomaly detection, and industry best practices for cloud cost optimization. 
              In a production environment, this would integrate with cloud provider APIs for real-time recommendations.
            </p>
          </div>
        </>
      )}
    </div>
  );
};

export default CostAnalysisPage;