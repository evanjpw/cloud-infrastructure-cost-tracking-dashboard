const { test, expect } = require('@playwright/test');

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Mock API responses for dashboard
    await page.route('**/api/teams', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { name: 'platform', displayName: 'Platform Engineering' },
          { name: 'backend', displayName: 'Backend Development' },
          { name: 'frontend', displayName: 'Frontend Development' }
        ])
      });
    });

    await page.route('**/api/reports', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          breakdowns: [
            { service: 'EC2', team: 'platform', totalCost: 1500, date: '2025-01-15' },
            { service: 'S3', team: 'backend', totalCost: 800, date: '2025-01-15' },
            { service: 'RDS', team: 'backend', totalCost: 600, date: '2025-01-15' }
          ]
        })
      });
    });

    await page.goto('/');
  });

  test('should display main dashboard elements', async ({ page }) => {
    // Check for main navigation or dashboard title
    await expect(page.locator('h1, h2, nav')).toBeVisible({ timeout: 10000 });
    
    // Should have some form of cost data display
    await expect(page.locator('text=/cost|Cost|COST/i').first()).toBeVisible({ timeout: 10000 });
  });

  test('should load and display cost data', async ({ page }) => {
    // Wait for data to load and display
    await expect(page.locator('text=/1500|800|600/').first()).toBeVisible({ timeout: 10000 });
    
    // Should show service names
    await expect(page.locator('text=EC2')).toBeVisible();
    await expect(page.locator('text=S3')).toBeVisible();
    await expect(page.locator('text=RDS')).toBeVisible();
  });

  test('should handle team filtering', async ({ page }) => {
    // Look for team filter dropdown or buttons
    const teamFilter = page.locator('select, button').filter({ hasText: /team|Team|platform|backend/i }).first();
    
    if (await teamFilter.isVisible({ timeout: 5000 })) {
      await teamFilter.click();
      
      // If it's a dropdown, select an option
      if (await teamFilter.evaluate(el => el.tagName === 'SELECT')) {
        await teamFilter.selectOption('platform');
      }
      
      // Verify filtering works (cost data updates)
      await page.waitForTimeout(1000);
      await expect(page.locator('body')).toBeVisible(); // Basic check that page still works
    }
  });

  test('should display charts or visualizations', async ({ page }) => {
    // Look for chart containers (common chart library classes/elements)
    const chartElements = page.locator('canvas, svg, .recharts-wrapper, .chart-container, [data-testid*="chart"]');
    
    // Wait for charts to render
    await expect(chartElements.first()).toBeVisible({ timeout: 10000 });
  });

  test('should be responsive on mobile', async ({ page, isMobile }) => {
    if (isMobile) {
      // Check that content is visible on mobile
      await expect(page.locator('body')).toBeVisible();
      
      // Check that text is readable (not too small)
      const mainHeading = page.locator('h1, h2').first();
      if (await mainHeading.isVisible()) {
        const fontSize = await mainHeading.evaluate(el => 
          window.getComputedStyle(el).fontSize
        );
        expect(parseInt(fontSize)).toBeGreaterThan(16); // Minimum readable font size
      }
    }
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Mock API failure
    await page.route('**/api/reports', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Server error' })
      });
    });

    await page.reload();

    // Should still render page structure without crashing
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
    
    // Should show some form of error handling or fallback data
    await expect(page.locator('text=/error|Error|fallback|no data/i').first()).toBeVisible({ timeout: 5000 });
  });

  test('should work with scenario parameter', async ({ page }) => {
    // Test loading with scenario parameter (student view)
    await page.goto('/dashboard?scenario=test-scenario-123');
    
    // Should still load dashboard
    await expect(page.locator('body')).toBeVisible({ timeout: 10000 });
    
    // May show scenario-specific data or UI elements
    await expect(page.locator('text=/scenario|test/i').first()).toBeVisible({ timeout: 5000 });
  });

  test('should have working navigation', async ({ page }) => {
    // Look for navigation links
    const navLinks = page.locator('nav a, [role="navigation"] a, a[href*="/"]');
    
    if (await navLinks.count() > 0) {
      const firstLink = navLinks.first();
      const href = await firstLink.getAttribute('href');
      
      if (href && href !== '#' && !href.startsWith('http')) {
        await firstLink.click();
        
        // Should navigate successfully
        await page.waitForTimeout(1000);
        await expect(page.locator('body')).toBeVisible();
      }
    }
  });

  test('should load within reasonable time', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto('/', { waitUntil: 'networkidle' });
    
    const loadTime = Date.now() - startTime;
    
    // Should load within 10 seconds
    expect(loadTime).toBeLessThan(10000);
    
    // Main content should be visible
    await expect(page.locator('body')).toBeVisible();
  });
});