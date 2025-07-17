// api.js
const API_BASE_URL = "http://localhost:8080/api/reports";

// Simulated POST request to fetch cost report
export const fetchCostReport = async (teamName, startDate, endDate) => {
  console.log("Mock API call to fetch cost report");

  // Placeholder: pretend to send a request, but return fake data
  return Promise.resolve([
    { service: "EC2", totalCost: 945.5 },
    { service: "S3", totalCost: 712.3 },
    { service: "RDS", totalCost: 388.75 }
  ]);
};
