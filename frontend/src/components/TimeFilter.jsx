import React, { useState, useEffect } from "react";
import {
  colors,
  getCardStyle,
  getInputStyle,
  getButtonStyle,
} from "../styles/colors";
import { textStyles } from "../styles/typography";

const TimeFilter = ({ startDate, endDate, onDateRangeChange }) => {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkIsMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkIsMobile();
    window.addEventListener("resize", checkIsMobile);
    return () => window.removeEventListener("resize", checkIsMobile);
  }, []);
  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const newStartDate = formData.get("startDate");
    const newEndDate = formData.get("endDate");

    if (newStartDate && newEndDate) {
      onDateRangeChange(newStartDate, newEndDate);
    }
  };

  return (
    <div
      style={{
        marginBottom: "2rem",
        padding: isMobile ? "1rem" : "1.5rem",
        ...getCardStyle(),
      }}
    >
      <h5
        style={{
          ...textStyles.cardTitle(colors.text.primary),
          marginBottom: "1rem",
        }}
      >
        📅 Date Range Filter
      </h5>
      <form
        onSubmit={handleSubmit}
        style={{
          display: "flex",
          alignItems: isMobile ? "stretch" : "center",
          flexDirection: isMobile ? "column" : "row",
          gap: "1rem",
          flexWrap: "wrap",
        }}
      >
        <label
          style={{
            display: "flex",
            flexDirection: "column",
            minWidth: isMobile ? "100%" : "150px",
            flex: isMobile ? "1" : "0 0 auto",
          }}
        >
          <span
            style={{
              ...textStyles.label(colors.text.primary),
              marginBottom: "0.25rem",
              display: "block",
            }}
          >
            Start Date:
          </span>
          <input
            type="date"
            name="startDate"
            defaultValue={startDate}
            style={{
              ...getInputStyle(),
            }}
          />
        </label>
        <label
          style={{
            display: "flex",
            flexDirection: "column",
            minWidth: isMobile ? "100%" : "150px",
            flex: isMobile ? "1" : "0 0 auto",
          }}
        >
          <span
            style={{
              ...textStyles.label(colors.text.primary),
              marginBottom: "0.25rem",
              display: "block",
            }}
          >
            End Date:
          </span>
          <input
            type="date"
            name="endDate"
            defaultValue={endDate}
            style={{
              ...getInputStyle(),
            }}
          />
        </label>
        <button
          type="submit"
          style={{
            ...getButtonStyle("primary"),
            alignSelf: isMobile ? "stretch" : "flex-end",
            width: isMobile ? "100%" : "auto",
          }}
          onMouseOver={(e) =>
            (e.target.style.backgroundColor = colors.primary[600])
          }
          onMouseOut={(e) =>
            (e.target.style.backgroundColor = colors.primary[500])
          }
        >
          🔍 Apply Filter
        </button>
      </form>
      <p
        style={{
          ...textStyles.caption(colors.text.secondary),
          marginTop: "0.5rem",
          fontStyle: "italic",
        }}
      >
        💡 Current range: {startDate} to {endDate}
      </p>
    </div>
  );
};

export default TimeFilter;
