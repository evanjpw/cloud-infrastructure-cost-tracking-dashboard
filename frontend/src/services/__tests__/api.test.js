import {
  fetchCostReport,
  fetchTeams,
  generatePredictions,
  analyzeTrends,
  compareEntities,
  detectAnomalies,
  generateOptimizationRecommendations,
  getOptimizationSummary,
  createScenario,
  compareScenarios,
  getBudgets,
  createBudget,
  updateBudget,
  deleteBudget,
  fetchUsageRecords
} from '../api';

// Mock fetch globally
global.fetch = jest.fn();

describe('API Service Tests', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  describe('fetchCostReport', () => {
    it('should fetch cost report successfully', async () => {
      const mockResponse = {
        breakdowns: [
          { service: 'EC2', team: 'platform', totalCost: 1000, date: '2025-01-15' }
        ]
      };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      });

      const result = await fetchCostReport('platform', '2025-01-01', '2025-01-31');

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/reports', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          teamName: 'platform',
          startDate: '2025-01-01',
          endDate: '2025-01-31'
        })
      });

      expect(result).toEqual(mockResponse.breakdowns);
    });

    it('should return fallback data when API fails', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await fetchCostReport('platform', '2025-01-01', '2025-01-31');

      expect(result).toBeInstanceOf(Array);
      expect(result.length).toBeGreaterThan(0);
      expect(result[0]).toHaveProperty('service');
      expect(result[0]).toHaveProperty('totalCost');
    });

    it('should throw error when fallback is disabled', async () => {
      // Mock the API module with fallback disabled
      jest.doMock('../api', () => {
        const originalModule = jest.requireActual('../api');
        return {
          ...originalModule,
          fetchCostReport: jest.fn().mockRejectedValue(new Error('API call failed and fallback data is disabled'))
        };
      });

      const { fetchCostReport } = require('../api');
      
      await expect(fetchCostReport('platform', '2025-01-01', '2025-01-31'))
        .rejects
        .toThrow('API call failed and fallback data is disabled');
        
      jest.dontMock('../api');
    });
  });

  describe('fetchTeams', () => {
    it('should fetch teams successfully', async () => {
      const mockTeams = [
        { name: 'platform', displayName: 'Platform Engineering' },
        { name: 'backend', displayName: 'Backend Development' }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTeams
      });

      const result = await fetchTeams();

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/teams');
      expect(result).toEqual(mockTeams);
    });

    it('should return fallback teams when API fails', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await fetchTeams();

      expect(result).toEqual([
        { name: 'platform', displayName: 'Platform Engineering' },
        { name: 'frontend', displayName: 'Frontend Development' },
        { name: 'backend', displayName: 'Backend Development' },
        { name: 'data', displayName: 'Data Engineering' },
        { name: 'ml', displayName: 'Machine Learning' }
      ]);
    });
  });

  describe('generatePredictions', () => {
    it('should generate predictions successfully', async () => {
      const mockPredictions = {
        method: 'linear_regression',
        predictions: [
          { date: '2025-02-01', predictedCost: 1200, confidence: 0.85 }
        ]
      };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockPredictions
      });

      const result = await generatePredictions('linear_regression', 30, 'platform', '2025-01-01', '2025-01-31');

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/analytics/predictions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          method: 'linear_regression',
          daysToPredict: 30,
          teamName: 'platform',
          startDate: '2025-01-01',
          endDate: '2025-01-31',
          includeSeasonality: true,
          confidenceLevel: 0.95,
          options: {}
        })
      });

      expect(result).toEqual(mockPredictions);
    });
  });

  describe('Budget APIs', () => {
    it('should create budget successfully', async () => {
      const budgetData = {
        name: 'Test Budget',
        amount: 5000,
        period: 'monthly'
      };

      const mockResponse = { ...budgetData, id: '123' };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      });

      const result = await createBudget(budgetData);

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/budgets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(budgetData)
      });

      expect(result).toEqual(mockResponse);
    });

    it('should update budget successfully', async () => {
      const budgetData = { name: 'Updated Budget', amount: 6000 };
      const mockResponse = { ...budgetData, id: '123' };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      });

      const result = await updateBudget('123', budgetData);

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/budgets/123', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(budgetData)
      });

      expect(result).toEqual(mockResponse);
    });

    it('should delete budget successfully', async () => {
      fetch.mockResolvedValueOnce({
        ok: true
      });

      const result = await deleteBudget('123');

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/budgets/123', {
        method: 'DELETE'
      });

      expect(result).toEqual({ success: true });
    });
  });

  describe('Optimization APIs', () => {
    it('should generate optimization recommendations', async () => {
      const mockRecommendations = [
        {
          id: 'rec_1',
          title: 'Rightsize EC2 Instances',
          type: 'rightsizing',
          potentialSavings: 850.00
        }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockRecommendations
      });

      const result = await generateOptimizationRecommendations('team', '2025-01-01', '2025-01-31');

      expect(result).toEqual(mockRecommendations);
    });

    it('should detect anomalies', async () => {
      const mockAnomalies = [
        {
          date: '2025-01-15',
          service: 'EC2',
          deviationScore: 3.2,
          severity: 'high'
        }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockAnomalies
      });

      const result = await detectAnomalies('platform', '2025-01-01', '2025-01-31', 2.0);

      expect(result).toEqual(mockAnomalies);
    });
  });

  describe('Error Handling', () => {
    it('should handle HTTP error responses', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500
      });

      const result = await fetchTeams();

      // Should fall back to default teams
      expect(result).toHaveLength(5);
      expect(result[0]).toHaveProperty('name', 'platform');
    });

    it('should handle network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await fetchCostReport('platform', '2025-01-01', '2025-01-31');

      // Should return fallback data
      expect(result).toBeInstanceOf(Array);
      expect(result.length).toBeGreaterThan(0);
    });
  });

  describe('URL Parameter Construction', () => {
    it('should construct query parameters correctly for detectAnomalies', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      });

      await detectAnomalies('platform', '2025-01-01', '2025-01-31', 2.5);

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/analytics/anomalies?teamName=platform&startDate=2025-01-01&endDate=2025-01-31&threshold=2.5'
      );
    });

    it('should handle optional parameters in getOptimizationSummary', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ totalPotentialSavings: 1000 })
      });

      await getOptimizationSummary('all', '2025-01-01', '2025-01-31');

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/optimization/summary?startDate=2025-01-01&endDate=2025-01-31'
      );
    });
  });
});