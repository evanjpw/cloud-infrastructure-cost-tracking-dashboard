import React, { useState, useRef, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const MultiSelectFilter = ({
  label,
  options = [],
  selected = [],
  onChange,
  placeholder = "Select options...",
  icon = "🔽",
  isMobile = false,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Filter options based on search
  const filteredOptions = options.filter((option) =>
    option.label.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleToggleOption = (optionValue) => {
    const newSelected = selected.includes(optionValue)
      ? selected.filter((val) => val !== optionValue)
      : [...selected, optionValue];
    onChange(newSelected);
  };

  const handleSelectAll = () => {
    if (selected.length === filteredOptions.length) {
      onChange([]); // Deselect all
    } else {
      onChange(filteredOptions.map((opt) => opt.value)); // Select all filtered
    }
  };

  const getDisplayText = () => {
    if (selected.length === 0) return placeholder;
    if (selected.length === 1) {
      const selectedOption = options.find((opt) => opt.value === selected[0]);
      return selectedOption ? selectedOption.label : selected[0];
    }
    return `${selected.length} selected`;
  };

  return (
    <div style={{ position: "relative", width: "100%" }} ref={dropdownRef}>
      {/* Label */}
      <label style={textStyles.label(colors.text.primary)}>{label}:</label>

      {/* Dropdown Trigger */}
      <div
        style={{
          ...getCardStyle(),
          padding: "0.75rem",
          marginTop: "0.5rem",
          cursor: "pointer",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          backgroundColor: isOpen ? colors.primary[50] : colors.white,
          borderColor: isOpen ? colors.primary[300] : colors.border.medium,
          minHeight: "44px",
        }}
        onClick={() => setIsOpen(!isOpen)}
      >
        <span style={{ ...textStyles.body(colors.text.primary), flex: 1 }}>
          {getDisplayText()}
        </span>
        <span
          style={{
            fontSize: "0.875rem",
            transform: isOpen ? "rotate(180deg)" : "rotate(0deg)",
            transition: "transform 0.2s ease",
          }}
        >
          {icon}
        </span>
      </div>

      {/* Dropdown Menu */}
      {isOpen && (
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
            maxHeight: "300px",
            overflow: "hidden",
            marginTop: "4px",
          }}
        >
          {/* Search Input */}
          <div style={{ padding: "0.75rem", borderBottom: `1px solid ${colors.border.light}` }}>
            <input
              type="text"
              placeholder="Search..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                width: "100%",
                padding: "0.5rem",
                border: `1px solid ${colors.border.medium}`,
                borderRadius: "4px",
                fontSize: "0.875rem",
                outline: "none",
              }}
              onFocus={(e) => (e.target.style.borderColor = colors.primary[300])}
              onBlur={(e) => (e.target.style.borderColor = colors.border.medium)}
            />
          </div>

          {/* Select All Option */}
          {filteredOptions.length > 1 && (
            <div
              style={{
                padding: "0.75rem",
                borderBottom: `1px solid ${colors.border.light}`,
                cursor: "pointer",
                backgroundColor: colors.background.secondary,
                ...textStyles.body(colors.text.primary),
              }}
              onClick={handleSelectAll}
              onMouseEnter={(e) => (e.target.style.backgroundColor = colors.primary[50])}
              onMouseLeave={(e) => (e.target.style.backgroundColor = colors.background.secondary)}
            >
              <strong>
                {selected.length === filteredOptions.length ? "Deselect All" : "Select All"} 
                {searchTerm && ` (${filteredOptions.length} filtered)`}
              </strong>
            </div>
          )}

          {/* Options List */}
          <div style={{ maxHeight: "200px", overflowY: "auto" }}>
            {filteredOptions.length === 0 ? (
              <div
                style={{
                  padding: "1rem",
                  textAlign: "center",
                  ...textStyles.body(colors.text.secondary),
                }}
              >
                No options found
              </div>
            ) : (
              filteredOptions.map((option) => {
                const isSelected = selected.includes(option.value);
                return (
                  <div
                    key={option.value}
                    style={{
                      padding: "0.75rem",
                      cursor: "pointer",
                      display: "flex",
                      alignItems: "center",
                      gap: "0.5rem",
                      backgroundColor: isSelected ? colors.primary[50] : colors.white,
                      ...textStyles.body(colors.text.primary),
                    }}
                    onClick={() => handleToggleOption(option.value)}
                    onMouseEnter={(e) => {
                      if (!isSelected) {
                        e.target.style.backgroundColor = colors.background.secondary;
                      }
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.backgroundColor = isSelected
                        ? colors.primary[50]
                        : colors.white;
                    }}
                  >
                    {/* Checkbox */}
                    <div
                      style={{
                        width: "16px",
                        height: "16px",
                        border: `2px solid ${isSelected ? colors.primary[500] : colors.border.medium}`,
                        borderRadius: "3px",
                        backgroundColor: isSelected ? colors.primary[500] : colors.white,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        fontSize: "10px",
                        color: colors.white,
                      }}
                    >
                      {isSelected && "✓"}
                    </div>
                    <span>{option.label}</span>
                    {option.count && (
                      <span style={{ ...textStyles.caption(colors.text.secondary), marginLeft: "auto" }}>
                        ({option.count})
                      </span>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default MultiSelectFilter;