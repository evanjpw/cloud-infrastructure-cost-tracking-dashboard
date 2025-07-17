import React from 'react';

const TimeFilter = () => {
  return (
    <div style={{ marginBottom: '2rem' }}>
      <h5>Filter by Date</h5>
      <form>
        <label>
          Start Date:&nbsp;
          <input type="date" name="startDate" />
        </label>
        <label style={{ marginLeft: '1rem' }}>
          End Date:&nbsp;
          <input type="date" name="endDate" />
        </label>
        <button type="submit" style={{ marginLeft: '1rem' }}>Apply</button>
      </form>
    </div>
  );
};

export default TimeFilter;
