import React, { useEffect, useState } from 'react';
import { fetchCostReport } from '../services/api';
import CostBreakdownChart from '../components/CostBreakdownChart';
import TeamUsageTable from '../components/TeamUsageTable';
import TimeFilter from '../components/TimeFilter';

const DashboardPage = () => {
  const [costData, setCostData] = useState([]);

  useEffect(() => {
    // Simulate API call
    fetchCostReport("Platform", "2024-11-01", "2024-11-07").then((data) => {
      setCostData(data);
    });
  }, []);

  return (
    <div style={{ padding: '2rem' }}>
      <h2>Cloud Cost Dashboard</h2>
      <TimeFilter />
      <CostBreakdownChart />
      <TeamUsageTable />
      <p style={{ marginTop: '1rem', fontSize: '0.9rem' }}>
        Loaded {costData.length} service entries from API.
      </p>
    </div>
  );
};

export default DashboardPage;
