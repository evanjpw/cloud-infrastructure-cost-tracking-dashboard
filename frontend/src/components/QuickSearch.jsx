import React, { useState, useRef, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const QuickSearch = ({
  placeholder = "Search services, teams, or costs...",
  onSearch,
  suggestions = [],
  isMobile = false,
}) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [filteredSuggestions, setFilteredSuggestions] = useState([]);
  const searchRef = useRef(null);
  const debounceRef = useRef(null);

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Filter suggestions based on search term
  useEffect(() => {
    if (searchTerm.length > 0) {
      const filtered = suggestions.filter((suggestion) =>
        suggestion.label.toLowerCase().includes(searchTerm.toLowerCase()) ||
        suggestion.category.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredSuggestions(filtered.slice(0, 8)); // Limit to 8 suggestions
      setShowSuggestions(filtered.length > 0);
    } else {
      setFilteredSuggestions([]);
      setShowSuggestions(false);
    }
  }, [searchTerm, suggestions]);

  // Debounced search
  useEffect(() => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      if (onSearch) {
        onSearch(searchTerm);
      }
    }, 300);

    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [searchTerm, onSearch]);

  const handleInputChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSuggestionClick = (suggestion) => {
    setSearchTerm(suggestion.label);
    setShowSuggestions(false);
    if (onSearch) {
      onSearch(suggestion.label);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      setShowSuggestions(false);
      if (onSearch) {
        onSearch(searchTerm);
      }
    } else if (e.key === "Escape") {
      setShowSuggestions(false);
    }
  };

  const clearSearch = () => {
    setSearchTerm("");
    setShowSuggestions(false);
    if (onSearch) {
      onSearch("");
    }
  };

  const getCategoryIcon = (category) => {
    switch (category.toLowerCase()) {
      case "service": return "🔧";
      case "team": return "👥";
      case "cost": return "💰";
      case "region": return "🌍";
      default: return "🔍";
    }
  };

  return (
    <div style={{ position: "relative", width: "100%" }} ref={searchRef}>
      {/* Search Input */}
      <div
        style={{
          ...getCardStyle(),
          padding: "0.75rem",
          display: "flex",
          alignItems: "center",
          gap: "0.75rem",
          backgroundColor: colors.white,
          border: `2px solid ${showSuggestions ? colors.primary[300] : colors.border.medium}`,
          transition: "border-color 0.2s ease",
        }}
      >
        {/* Search Icon */}
        <span style={{ fontSize: "1.1rem", color: colors.text.secondary }}>🔍</span>
        
        {/* Input Field */}
        <input
          type="text"
          placeholder={placeholder}
          value={searchTerm}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          onFocus={() => searchTerm.length > 0 && setShowSuggestions(filteredSuggestions.length > 0)}
          style={{
            border: "none",
            outline: "none",
            flex: 1,
            fontSize: "0.9rem",
            color: colors.text.primary,
            backgroundColor: "transparent",
          }}
        />

        {/* Clear Button */}
        {searchTerm && (
          <button
            onClick={clearSearch}
            style={{
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: "1.1rem",
              color: colors.text.secondary,
              padding: "2px 4px",
              borderRadius: "50%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
            onMouseEnter={(e) => (e.target.style.backgroundColor = colors.background.secondary)}
            onMouseLeave={(e) => (e.target.style.backgroundColor = "transparent")}
            title="Clear search"
          >
            ✕
          </button>
        )}
      </div>

      {/* Search Suggestions */}
      {showSuggestions && (
        <div
          style={{
            position: "absolute",
            top: "100%",
            left: 0,
            right: 0,
            zIndex: 1000,
            backgroundColor: colors.white,
            border: `1px solid ${colors.border.medium}`,
            borderRadius: "6px",
            boxShadow: "0 4px 12px rgba(0, 0, 0, 0.15)",
            marginTop: "4px",
            maxHeight: "300px",
            overflow: "hidden",
          }}
        >
          {/* Suggestions Header */}
          <div
            style={{
              padding: "0.75rem",
              backgroundColor: colors.background.secondary,
              borderBottom: `1px solid ${colors.border.light}`,
              ...textStyles.caption(colors.text.secondary),
            }}
          >
            <strong>Suggestions</strong>
            {searchTerm && (
              <span style={{ marginLeft: "0.5rem" }}>
                for "{searchTerm}" ({filteredSuggestions.length} found)
              </span>
            )}
          </div>

          {/* Suggestions List */}
          <div style={{ maxHeight: "250px", overflowY: "auto" }}>
            {filteredSuggestions.map((suggestion, index) => (
              <div
                key={index}
                style={{
                  padding: "0.75rem",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  gap: "0.75rem",
                  borderBottom: index < filteredSuggestions.length - 1 
                    ? `1px solid ${colors.border.light}` 
                    : "none",
                }}
                onClick={() => handleSuggestionClick(suggestion)}
                onMouseEnter={(e) => (e.target.style.backgroundColor = colors.primary[50])}
                onMouseLeave={(e) => (e.target.style.backgroundColor = colors.white)}
              >
                {/* Category Icon */}
                <span style={{ fontSize: "1rem" }}>
                  {getCategoryIcon(suggestion.category)}
                </span>

                {/* Suggestion Content */}
                <div style={{ flex: 1 }}>
                  <div style={{ ...textStyles.body(colors.text.primary) }}>
                    {suggestion.label}
                  </div>
                  <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                    {suggestion.category}
                    {suggestion.description && ` • ${suggestion.description}`}
                  </div>
                </div>

                {/* Additional Info */}
                {suggestion.value && (
                  <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                    {suggestion.value}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* No Results */}
          {filteredSuggestions.length === 0 && searchTerm && (
            <div
              style={{
                padding: "1.5rem",
                textAlign: "center",
                ...textStyles.body(colors.text.secondary),
              }}
            >
              No suggestions found for "{searchTerm}"
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default QuickSearch;