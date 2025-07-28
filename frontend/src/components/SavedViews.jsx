import React, { useState, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const SavedViews = ({
  currentFilters,
  onLoadView,
  isMobile = false,
}) => {
  const [savedViews, setSavedViews] = useState([]);
  const [showSaveDialog, setShowSaveDialog] = useState(false);
  const [viewName, setViewName] = useState("");

  // Load saved views from localStorage on component mount
  useEffect(() => {
    const saved = localStorage.getItem("costDashboardSavedViews");
    if (saved) {
      try {
        setSavedViews(JSON.parse(saved));
      } catch (error) {
        console.error("Error loading saved views:", error);
      }
    }
  }, []);

  // Save views to localStorage whenever savedViews changes
  useEffect(() => {
    localStorage.setItem("costDashboardSavedViews", JSON.stringify(savedViews));
  }, [savedViews]);

  const handleSaveView = () => {
    if (!viewName.trim()) return;

    const newView = {
      id: Date.now().toString(),
      name: viewName.trim(),
      filters: {
        selectedTeams: currentFilters.selectedTeams,
        selectedServices: currentFilters.selectedServices,
        searchTerm: currentFilters.searchTerm,
        startDate: currentFilters.startDate,
        endDate: currentFilters.endDate,
        granularity: currentFilters.granularity,
      },
      createdAt: new Date().toISOString(),
    };

    setSavedViews(prev => [...prev, newView]);
    setViewName("");
    setShowSaveDialog(false);
  };

  const handleLoadView = (view) => {
    if (onLoadView) {
      onLoadView(view.filters);
    }
  };

  const handleDeleteView = (viewId) => {
    setSavedViews(prev => prev.filter(view => view.id !== viewId));
  };

  const getFilterSummary = (filters) => {
    const parts = [];
    if (filters.selectedTeams?.length > 0) {
      parts.push(`${filters.selectedTeams.length} teams`);
    }
    if (filters.selectedServices?.length > 0) {
      parts.push(`${filters.selectedServices.length} services`);
    }
    if (filters.searchTerm) {
      parts.push(`search: "${filters.searchTerm}"`);
    }
    return parts.length > 0 ? parts.join(", ") : "No filters";
  };

  return (
    <div style={{ marginTop: "1rem" }}>
      {/* Header with Save Button */}
      <div style={{ 
        display: "flex", 
        alignItems: "center", 
        justifyContent: "space-between",
        marginBottom: "1rem"
      }}>
        <h4 style={{ ...textStyles.label(colors.text.primary), margin: 0 }}>
          💾 Saved Views
        </h4>
        <button
          onClick={() => setShowSaveDialog(true)}
          style={{
            padding: "0.5rem 1rem",
            backgroundColor: colors.primary[500],
            color: colors.white,
            border: "none",
            borderRadius: "4px",
            fontSize: "0.875rem",
            cursor: "pointer",
            fontWeight: "500",
          }}
          onMouseEnter={(e) => (e.target.style.backgroundColor = colors.primary[600])}
          onMouseLeave={(e) => (e.target.style.backgroundColor = colors.primary[500])}
        >
          Save Current View
        </button>
      </div>

      {/* Save Dialog */}
      {showSaveDialog && (
        <div
          style={{
            ...getCardStyle(),
            padding: "1rem",
            marginBottom: "1rem",
            backgroundColor: colors.primary[50],
            border: `1px solid ${colors.primary[300]}`,
          }}
        >
          <h5 style={{ ...textStyles.body(colors.text.primary), margin: "0 0 0.75rem 0" }}>
            Save Current View
          </h5>
          <div style={{ display: "flex", gap: isMobile ? "0.5rem" : "1rem", flexDirection: isMobile ? "column" : "row" }}>
            <input
              type="text"
              placeholder="Enter view name..."
              value={viewName}
              onChange={(e) => setViewName(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSaveView()}
              style={{
                flex: 1,
                padding: "0.5rem",
                border: `1px solid ${colors.border.medium}`,
                borderRadius: "4px",
                fontSize: "0.875rem",
                outline: "none",
              }}
              onFocus={(e) => (e.target.style.borderColor = colors.primary[300])}
              onBlur={(e) => (e.target.style.borderColor = colors.border.medium)}
            />
            <div style={{ display: "flex", gap: "0.5rem" }}>
              <button
                onClick={handleSaveView}
                disabled={!viewName.trim()}
                style={{
                  padding: "0.5rem 1rem",
                  backgroundColor: viewName.trim() ? colors.success : colors.gray[400],
                  color: colors.white,
                  border: "none",
                  borderRadius: "4px",
                  fontSize: "0.875rem",
                  cursor: viewName.trim() ? "pointer" : "not-allowed",
                }}
              >
                Save
              </button>
              <button
                onClick={() => {
                  setShowSaveDialog(false);
                  setViewName("");
                }}
                style={{
                  padding: "0.5rem 1rem",
                  backgroundColor: colors.gray[500],
                  color: colors.white,
                  border: "none",
                  borderRadius: "4px",
                  fontSize: "0.875rem",
                  cursor: "pointer",
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Saved Views List */}
      {savedViews.length === 0 ? (
        <div
          style={{
            ...getCardStyle(),
            padding: "2rem",
            textAlign: "center",
            backgroundColor: colors.background.secondary,
          }}
        >
          <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
            No saved views yet. Save your current filters to quickly access them later.
          </p>
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
          {savedViews.map((view) => (
            <div
              key={view.id}
              style={{
                ...getCardStyle(),
                padding: "1rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                cursor: "pointer",
                transition: "all 0.2s ease",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = colors.primary[50];
                e.currentTarget.style.borderColor = colors.primary[300];
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = colors.white;
                e.currentTarget.style.borderColor = colors.border.light;
              }}
            >
              <div style={{ flex: 1 }} onClick={() => handleLoadView(view)}>
                <div style={{ ...textStyles.body(colors.text.primary), fontWeight: "600" }}>
                  {view.name}
                </div>
                <div style={{ ...textStyles.caption(colors.text.secondary), marginTop: "0.25rem" }}>
                  {getFilterSummary(view.filters)} • 
                  Saved {new Date(view.createdAt).toLocaleDateString()}
                </div>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleDeleteView(view.id);
                }}
                style={{
                  background: "none",
                  border: "none",
                  color: colors.text.secondary,
                  cursor: "pointer",
                  fontSize: "1.1rem",
                  padding: "0.25rem",
                  borderRadius: "50%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
                onMouseEnter={(e) => {
                  e.target.style.backgroundColor = colors.error + "20";
                  e.target.style.color = colors.error;
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = "transparent";
                  e.target.style.color = colors.text.secondary;
                }}
                title="Delete view"
              >
                🗑️
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default SavedViews;