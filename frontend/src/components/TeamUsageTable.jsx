import React from 'react';

const TeamUsageTable = () => {
  return (
    <div style={{ border: '1px solid #ddd', padding: '1rem' }}>
      <h5>Team Usage Table</h5>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={{ borderBottom: '1px solid #ccc', textAlign: 'left' }}>Service</th>
            <th style={{ borderBottom: '1px solid #ccc', textAlign: 'left' }}>Total Cost ($)</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>EC2</td>
            <td>945.50</td>
          </tr>
          <tr>
            <td>S3</td>
            <td>712.30</td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};

export default TeamUsageTable;
