import React from 'react';

const DashboardPage = () => {
  return (
    <div style={{ padding: '2rem' }}>
      <h2>Cloud Cost Dashboard</h2>

      <div style={{ marginTop: '2rem' }}>
        <h4>Filters</h4>
        {/* Simulated time filter component */}
        <div>
          <label>Start Date:</label>
          <input type="date" />
          <label style={{ marginLeft: '1rem' }}>End Date:</label>
          <input type="date" />
        </div>
      </div>

      <div style={{ marginTop: '2rem' }}>
        <h4>Cost Breakdown</h4>
        {/* Simulated chart/table placeholder */}
        <div style={{ border: '1px solid #ccc', padding: '1rem' }}>
          <p>[Chart or table component goes here]</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
