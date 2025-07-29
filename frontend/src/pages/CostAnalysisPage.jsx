import React, { useState, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";
import { fetchCostReport, fetchTeams } from "../services/api";
import { generateOptimizationRecommendations } from "../utils/optimizationEngine";
import { predictCosts, generatePredictionScenarios } from "../utils/predictiveModeling";
import { identifyTrends, analyzeSeasonalPatterns } from "../utils/trendAnalysis";
import { compareTeams } from "../utils/comparativeAnalysis";
import OptimizationSummary from "../components/optimization/OptimizationSummary";
import RecommendationCard from "../components/optimization/RecommendationCard";
import AnomalyChart from "../components/optimization/AnomalyChart";
import PredictiveCostChart from "../components/analytics/PredictiveCostChart";
import WhatIfScenarioBuilder from "../components/analytics/WhatIfScenarioBuilder";

const CostAnalysisPage = () => {
  const [costData, setCostData] = useState([]);
  const [teams, setTeams] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [predictions, setPredictions] = useState(null);
  const [trendAnalysis, setTrendAnalysis] = useState(null);
  const [teamComparison, setTeamComparison] = useState(null);
  const [activeTab, setActiveTab] = useState('optimization'); // optimization, predictions, trends, scenarios, comparison
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

        // Generate predictive analytics
        if (costDataResult.length > 0) {
          const costPredictions = predictCosts(costDataResult, 30, { method: 'seasonal' });
          setPredictions(costPredictions);

          const trends = identifyTrends(costDataResult);
          setTrendAnalysis(trends);

          const teamComp = compareTeams(costDataResult);
          setTeamComparison(teamComp.comparison);
        }

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

  const tabs = [
    { id: 'optimization', label: 'Cost Optimization', icon: '💡' },
    { id: 'predictions', label: 'Predictive Analytics', icon: '🔮' },
    { id: 'trends', label: 'Trend Analysis', icon: '📈' },
    { id: 'scenarios', label: 'What-If Scenarios', icon: '🎯' },
    { id: 'comparison', label: 'Team Comparison', icon: '📊' }
  ];

  return (
    <div style={{ maxWidth: "1400px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Cost Analysis & Optimization
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Advanced analytics, predictive modeling, and optimization recommendations
        </p>
      </div>

      {/* Tab Navigation */}
      <div style={{
        ...getCardStyle(),
        padding: '0',
        marginBottom: '2rem',
        overflow: 'hidden'
      }}>
        <div style={{
          display: 'flex',
          borderBottom: `1px solid ${colors.gray[200]}`,
          overflowX: 'auto'
        }}>
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                padding: '1rem 1.5rem',
                backgroundColor: activeTab === tab.id ? colors.primary[50] : 'transparent',
                color: activeTab === tab.id ? colors.primary[600] : colors.text.secondary,
                border: 'none',
                borderBottom: activeTab === tab.id ? `3px solid ${colors.primary[500]}` : '3px solid transparent',
                fontSize: '0.95rem',
                fontWeight: activeTab === tab.id ? '600' : '500',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                minWidth: isMobile ? 'auto' : '160px',
                justifyContent: 'center',
                whiteSpace: 'nowrap'
              }}
            >
              <span style={{ fontSize: '1.1em' }}>{tab.icon}</span>
              {!isMobile && tab.label}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div style={{
          ...getCardStyle(),
          padding: "2rem",
          textAlign: "center"
        }}>
          <p style={textStyles.body(colors.text.secondary)}>
            🔄 Analyzing your cloud costs and generating analytics...
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
          {/* Tab Content */}
          {activeTab === 'optimization' && (
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

            </>
          )}

          {activeTab === 'predictions' && (
            <PredictiveCostChart
              historicalData={costData}
              predictions={predictions}
              title="30-Day Cost Prediction"
              height={400}
              isMobile={isMobile}
            />
          )}

          {activeTab === 'trends' && (
            <div>
              {trendAnalysis && (
                <div style={{ ...getCardStyle(), padding: '1.5rem', marginBottom: '1.5rem' }}>
                  <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
                    📈 Trend Analysis Results
                  </h3>
                  
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '1rem',
                    marginBottom: '1.5rem'
                  }}>
                    <div style={{ padding: '1rem', backgroundColor: colors.background.secondary, borderRadius: '6px' }}>
                      <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: '0 0 0.5rem 0' }}>
                        Overall Trend
                      </h4>
                      <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
                        {trendAnalysis.summary.overall}
                      </p>
                    </div>
                    
                    <div style={{ padding: '1rem', backgroundColor: colors.background.secondary, borderRadius: '6px' }}>
                      <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: '0 0 0.5rem 0' }}>
                        Recommendation
                      </h4>
                      <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
                        {trendAnalysis.summary.recommendation}
                      </p>
                    </div>
                  </div>

                  <div>
                    <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
                      Trend Details
                    </h4>
                    {trendAnalysis.summary.details.map((detail, index) => (
                      <div key={index} style={{ 
                        padding: '0.75rem', 
                        backgroundColor: colors.white, 
                        border: `1px solid ${colors.gray[200]}`,
                        borderRadius: '4px',
                        marginBottom: '0.5rem'
                      }}>
                        <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
                          • {detail}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === 'scenarios' && (
            <WhatIfScenarioBuilder
              costData={costData}
              isMobile={isMobile}
            />
          )}

          {activeTab === 'comparison' && (
            <div>
              {teamComparison && (
                <div style={{ ...getCardStyle(), padding: '1.5rem' }}>
                  <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
                    📊 Team Cost Comparison
                  </h3>
                  
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '1rem'
                  }}>
                    {teamComparison.teams.map((team, index) => (
                      <div key={team.team} style={{
                        padding: '1rem',
                        backgroundColor: colors.background.secondary,
                        borderRadius: '6px',
                        border: index === 0 ? `2px solid ${colors.primary[500]}` : `1px solid ${colors.gray[200]}`
                      }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
                            {team.team}
                          </h4>
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            backgroundColor: index === 0 ? colors.primary[500] : colors.gray[400],
                            color: colors.white,
                            borderRadius: '4px',
                            fontSize: '0.75rem',
                            fontWeight: '600'
                          }}>
                            #{team.rank}
                          </span>
                        </div>
                        
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ ...textStyles.caption(colors.text.secondary) }}>Total Cost:</span>
                            <span style={{ ...textStyles.caption(colors.text.primary), fontWeight: '600' }}>
                              ${team.totalCost.toLocaleString()}
                            </span>
                          </div>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ ...textStyles.caption(colors.text.secondary) }}>Services:</span>
                            <span style={{ ...textStyles.caption(colors.text.primary), fontWeight: '600' }}>
                              {team.serviceCount}
                            </span>
                          </div>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ ...textStyles.caption(colors.text.secondary) }}>Efficiency:</span>
                            <span style={{ 
                              ...textStyles.caption(
                                team.efficiency < teamComparison.benchmarks.efficiency.avg ? colors.success : colors.warning
                              ), 
                              fontWeight: '600' 
                            }}>
                              ${team.efficiency.toFixed(0)}/service
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default CostAnalysisPage;