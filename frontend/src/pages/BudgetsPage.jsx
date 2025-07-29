import React, { useState, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";
import BudgetCard from "../components/budget/BudgetCard";
import BudgetForm from "../components/budget/BudgetForm";
import { fetchTeams, fetchCostReport } from "../services/api";

const BudgetsPage = () => {
  const [budgets, setBudgets] = useState([]);
  const [teams, setTeams] = useState([]);
  const [services, setServices] = useState([]);
  const [costData, setCostData] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingBudget, setEditingBudget] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkIsMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkIsMobile();
    window.addEventListener("resize", checkIsMobile);
    return () => window.removeEventListener("resize", checkIsMobile);
  }, []);

  // Load initial data
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        
        // Load teams
        const teamsData = await fetchTeams();
        setTeams(teamsData);

        // Load cost data to extract services
        const costDataResult = await fetchCostReport("platform", "2025-01-01", "2025-01-31");
        setCostData(costDataResult);
        
        // Extract unique services
        const uniqueServices = [...new Set(costDataResult.map(item => item.service || item.serviceName))].filter(Boolean);
        setServices(uniqueServices);

        // Load saved budgets from localStorage
        const savedBudgets = localStorage.getItem('cloud-cost-budgets');
        if (savedBudgets) {
          setBudgets(JSON.parse(savedBudgets));
        } else {
          // Create some sample budgets
          const sampleBudgets = [
            {
              id: '1',
              name: 'Q4 Platform Team Budget',
              amount: 50000,
              period: 'quarterly',
              scope: 'team',
              target: 'platform',
              alertThreshold: 80,
              description: 'Quarterly budget allocation for platform infrastructure',
              startDate: '2025-01-01',
              endDate: '2025-03-31'
            },
            {
              id: '2',
              name: 'Monthly EC2 Budget',
              amount: 15000,
              period: 'monthly',
              scope: 'service',
              target: 'EC2',
              alertThreshold: 75,
              description: 'EC2 compute costs budget',
              startDate: '2025-01-01',
              endDate: '2025-01-31'
            },
            {
              id: '3',
              name: 'Annual Organization Budget',
              amount: 500000,
              period: 'yearly',
              scope: 'total',
              target: 'organization',
              alertThreshold: 85,
              description: 'Total organizational cloud spend budget',
              startDate: '2025-01-01',
              endDate: '2025-12-31'
            }
          ];
          setBudgets(sampleBudgets);
          localStorage.setItem('cloud-cost-budgets', JSON.stringify(sampleBudgets));
        }
      } catch (error) {
        console.error('Failed to load data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Calculate current spend and forecast for each budget
  const calculateBudgetMetrics = (budget) => {
    let relevantData = costData;

    // Filter data based on budget scope
    if (budget.scope === 'team') {
      relevantData = costData.filter(item => item.team === budget.target);
    } else if (budget.scope === 'service') {
      relevantData = costData.filter(item => (item.service || item.serviceName) === budget.target);
    }

    const currentSpend = relevantData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    
    // Simple forecast calculation (assuming linear growth)
    const forecastedSpend = currentSpend * 1.2; // 20% projected increase
    
    // Calculate days remaining in budget period
    const endDate = new Date(budget.endDate);
    const today = new Date();
    const daysRemaining = Math.max(0, Math.ceil((endDate - today) / (1000 * 60 * 60 * 24)));

    return {
      currentSpend,
      forecastedSpend,
      daysRemaining
    };
  };

  const handleCreateBudget = () => {
    setEditingBudget(null);
    setShowForm(true);
  };

  const handleEditBudget = (budget) => {
    setEditingBudget(budget);
    setShowForm(true);
  };

  const handleSaveBudget = (budgetData) => {
    let updatedBudgets;
    
    if (editingBudget) {
      // Update existing budget
      updatedBudgets = budgets.map(budget => 
        budget.id === editingBudget.id ? budgetData : budget
      );
    } else {
      // Add new budget
      updatedBudgets = [...budgets, budgetData];
    }

    setBudgets(updatedBudgets);
    localStorage.setItem('cloud-cost-budgets', JSON.stringify(updatedBudgets));
    setShowForm(false);
    setEditingBudget(null);
  };

  const handleDeleteBudget = (budgetId) => {
    const updatedBudgets = budgets.filter(budget => budget.id !== budgetId);
    setBudgets(updatedBudgets);
    localStorage.setItem('cloud-cost-budgets', JSON.stringify(updatedBudgets));
  };

  const handleCancelForm = () => {
    setShowForm(false);
    setEditingBudget(null);
  };

  // Calculate overall budget statistics
  const totalBudgetAmount = budgets.reduce((sum, budget) => sum + budget.amount, 0);
  const totalCurrentSpend = budgets.reduce((sum, budget) => {
    const metrics = calculateBudgetMetrics(budget);
    return sum + metrics.currentSpend;
  }, 0);
  const budgetsOverThreshold = budgets.filter(budget => {
    const metrics = calculateBudgetMetrics(budget);
    const spendPercentage = budget.amount > 0 ? (metrics.currentSpend / budget.amount) * 100 : 0;
    return spendPercentage >= budget.alertThreshold;
  }).length;

  if (showForm) {
    return (
      <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
        <BudgetForm
          budget={editingBudget}
          teams={teams}
          services={services}
          onSave={handleSaveBudget}
          onCancel={handleCancelForm}
          isMobile={isMobile}
        />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Budget Management
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Set, track, and manage budgets across teams, services, and time periods
        </p>
      </div>

      {/* Budget Overview Cards */}
      <div style={{ 
        display: "grid", 
        gridTemplateColumns: isMobile ? "1fr" : "repeat(auto-fit, minmax(250px, 1fr))",
        gap: "1rem",
        marginBottom: "2rem"
      }}>
        {/* Total Budgets */}
        <div style={{
          ...getCardStyle(),
          padding: "1.5rem",
          textAlign: "center",
          backgroundColor: colors.primary[50],
          border: `1px solid ${colors.primary[200]}`
        }}>
          <div style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>🎯</div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: "0.5rem" }}>
            Total Budgets
          </h3>
          <p style={{ ...textStyles.body(colors.primary[700]), fontWeight: "600", fontSize: "1.5rem", margin: 0 }}>
            {budgets.length}
          </p>
        </div>

        {/* Total Budget Amount */}
        <div style={{
          ...getCardStyle(),
          padding: "1.5rem",
          textAlign: "center",
          backgroundColor: colors.success + "10",
          border: `1px solid ${colors.success}40`
        }}>
          <div style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>💰</div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: "0.5rem" }}>
            Total Allocated
          </h3>
          <p style={{ ...textStyles.body(colors.success), fontWeight: "600", fontSize: "1.5rem", margin: 0 }}>
            ${totalBudgetAmount.toLocaleString()}
          </p>
        </div>

        {/* Current Spend */}
        <div style={{
          ...getCardStyle(),
          padding: "1.5rem",
          textAlign: "center",
          backgroundColor: colors.warning + "10",
          border: `1px solid ${colors.warning}40`
        }}>
          <div style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>📊</div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: "0.5rem" }}>
            Current Spend
          </h3>
          <p style={{ ...textStyles.body(colors.warning), fontWeight: "600", fontSize: "1.5rem", margin: 0 }}>
            ${totalCurrentSpend.toLocaleString()}
          </p>
        </div>

        {/* Alerts */}
        <div style={{
          ...getCardStyle(),
          padding: "1.5rem",
          textAlign: "center",
          backgroundColor: budgetsOverThreshold > 0 ? colors.error + "10" : colors.success + "10",
          border: `1px solid ${budgetsOverThreshold > 0 ? colors.error : colors.success}40`
        }}>
          <div style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>
            {budgetsOverThreshold > 0 ? "🚨" : "✅"}
          </div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: "0.5rem" }}>
            Budget Alerts
          </h3>
          <p style={{ 
            ...textStyles.body(budgetsOverThreshold > 0 ? colors.error : colors.success), 
            fontWeight: "600", 
            fontSize: "1.5rem", 
            margin: 0 
          }}>
            {budgetsOverThreshold}
          </p>
        </div>
      </div>

      {/* Actions Bar */}
      <div style={{ 
        display: "flex", 
        justifyContent: "space-between", 
        alignItems: "center",
        marginBottom: "2rem",
        flexWrap: "wrap",
        gap: "1rem"
      }}>
        <div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
            Active Budgets ({budgets.length})
          </h3>
        </div>
        
        <button
          onClick={handleCreateBudget}
          style={{
            padding: "0.75rem 1.5rem",
            backgroundColor: colors.primary[500],
            color: colors.white,
            border: "none",
            borderRadius: "6px",
            fontSize: "0.9rem",
            fontWeight: "600",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
            transition: "all 0.2s ease",
            boxShadow: "0 2px 4px rgba(33, 150, 243, 0.2)",
          }}
          onMouseEnter={(e) => {
            e.target.style.backgroundColor = colors.primary[600];
            e.target.style.transform = "translateY(-1px)";
            e.target.style.boxShadow = "0 4px 8px rgba(33, 150, 243, 0.3)";
          }}
          onMouseLeave={(e) => {
            e.target.style.backgroundColor = colors.primary[500];
            e.target.style.transform = "translateY(0)";
            e.target.style.boxShadow = "0 2px 4px rgba(33, 150, 243, 0.2)";
          }}
        >
          <span style={{ fontSize: "1.1em" }}>➕</span>
          Create Budget
        </button>
      </div>

      {/* Budget Cards */}
      {loading ? (
        <div style={{
          ...getCardStyle(),
          padding: "2rem",
          textAlign: "center"
        }}>
          <p style={textStyles.body(colors.text.secondary)}>
            🔄 Loading budgets...
          </p>
        </div>
      ) : budgets.length === 0 ? (
        <div style={{
          ...getCardStyle(),
          padding: "3rem",
          textAlign: "center",
          backgroundColor: colors.gray[50]
        }}>
          <div style={{ fontSize: "3rem", marginBottom: "1rem" }}>📊</div>
          <h3 style={textStyles.cardTitle(colors.text.primary)}>
            No Budgets Created
          </h3>
          <p style={textStyles.body(colors.text.secondary)}>
            Create your first budget to start tracking and managing your cloud costs.
          </p>
          <button
            onClick={handleCreateBudget}
            style={{
              padding: "0.75rem 1.5rem",
              backgroundColor: colors.primary[500],
              color: colors.white,
              border: "none",
              borderRadius: "6px",
              fontSize: "0.9rem",
              fontWeight: "600",
              cursor: "pointer",
              marginTop: "1rem",
              transition: "background-color 0.2s ease",
            }}
            onMouseEnter={(e) => {
              e.target.style.backgroundColor = colors.primary[600];
            }}
            onMouseLeave={(e) => {
              e.target.style.backgroundColor = colors.primary[500];
            }}
          >
            Create Your First Budget
          </button>
        </div>
      ) : (
        <div style={{ 
          display: "grid", 
          gridTemplateColumns: isMobile ? "1fr" : "repeat(auto-fill, minmax(400px, 1fr))",
          gap: "1.5rem"
        }}>
          {budgets.map(budget => {
            const metrics = calculateBudgetMetrics(budget);
            return (
              <BudgetCard
                key={budget.id}
                budget={budget}
                currentSpend={metrics.currentSpend}
                forecastedSpend={metrics.forecastedSpend}
                daysRemaining={metrics.daysRemaining}
                isMobile={isMobile}
                onEdit={handleEditBudget}
                onDelete={handleDeleteBudget}
              />
            );
          })}
        </div>
      )}

    </div>
  );
};

export default BudgetsPage;