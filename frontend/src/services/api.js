// api.js
const API_BASE_URL = "http://localhost:8080/api";

// Real API call to fetch cost report from backend
export const fetchCostReport = async (teamName, startDate, endDate) => {
  console.log(
    `Making API call to fetch cost report for team: ${teamName}, dates: ${startDate} to ${endDate}`,
  );

  try {
    const response = await fetch(`${API_BASE_URL}/reports`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        teamName,
        startDate,
        endDate,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received data from backend:", data);

    // Transform backend response to match frontend expectations
    return data.breakdowns || [];
  } catch (error) {
    console.error("Error fetching cost report:", error);
    // Return mock data as fallback for development
    return [
      {
        service: "EC2",
        totalCost: 945.5,
        note: "Fallback data - backend unavailable",
      },
      {
        service: "S3",
        totalCost: 712.3,
        note: "Fallback data - backend unavailable",
      },
      {
        service: "RDS",
        totalCost: 388.75,
        note: "Fallback data - backend unavailable",
      },
    ];
  }
};

// Get list of available teams
export const fetchTeams = async () => {
  console.log("Fetching teams from backend");

  try {
    const response = await fetch(`${API_BASE_URL}/teams`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const teams = await response.json();
    console.log("Received teams from backend:", teams);
    return teams;
  } catch (error) {
    console.error("Error fetching teams:", error);
    // Return default teams as fallback
    return [
      { name: "platform", displayName: "Platform Engineering" },
      { name: "frontend", displayName: "Frontend Development" },
      { name: "backend", displayName: "Backend Development" },
      { name: "data", displayName: "Data Engineering" },
      { name: "ml", displayName: "Machine Learning" },
    ];
  }
};

// Get usage records for a specific team and date range
export const fetchUsageRecords = async (teamName, startDate, endDate) => {
  console.log(`Fetching usage records for team: ${teamName}`);

  try {
    const response = await fetch(
      `${API_BASE_URL}/usage?team=${encodeURIComponent(
        teamName,
      )}&startDate=${startDate}&endDate=${endDate}`,
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const records = await response.json();
    console.log("Received usage records from backend:", records);
    return records;
  } catch (error) {
    console.error("Error fetching usage records:", error);
    return [];
  }
};
