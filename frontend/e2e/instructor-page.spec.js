const { test, expect } = require('@playwright/test');

test.describe('Instructor Page', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to instructor page
    await page.goto('/instructor');
  });

  test('should display instructor page with all elements', async ({ page }) => {
    // Check page title and main heading
    await expect(page.locator('h1')).toContainText('Instructor Scenario Generator');
    await expect(page.locator('h2')).toContainText('Generate Test Scenario');

    // Check form elements
    await expect(page.locator('label:has-text("Difficulty Level")')).toBeVisible();
    await expect(page.locator('label:has-text("Scenario Type")')).toBeVisible();
    await expect(page.locator('label:has-text("Student Identifier")')).toBeVisible();
    await expect(page.locator('button:has-text("Generate Scenario")')).toBeVisible();
  });

  test('should load scenario templates on page load', async ({ page }) => {
    // Wait for templates to load
    await expect(page.locator('select option')).toHaveCount.toBeGreaterThan(5);
    
    // Check that template select shows available count
    await expect(page.locator('text=/\\(\\d+ available\\)/')).toBeVisible();
  });

  test('should filter templates by difficulty', async ({ page }) => {
    // Wait for initial load
    await page.waitForSelector('select option:not([value=""])', { timeout: 10000 });
    
    // Select beginner difficulty
    await page.selectOption('select[aria-label*="Difficulty"]', 'beginner');
    
    // Wait for filtering to complete
    await page.waitForTimeout(1000);
    
    // Check that templates are filtered (should have fewer options)
    const templateOptions = await page.locator('select option:not([value=""])').count();
    expect(templateOptions).toBeGreaterThan(0);
  });

  test('should show validation error for incomplete form', async ({ page }) => {
    // Try to generate without filling required fields
    await page.click('button:has-text("Generate Scenario")');
    
    // Check for error message
    await expect(page.locator('text=Please select a template and enter student identifier')).toBeVisible();
  });

  test('should generate scenario successfully', async ({ page }) => {
    // Wait for templates to load
    await page.waitForSelector('select option:not([value=""])', { timeout: 10000 });
    
    // Select a template (first available option)
    const templateSelect = page.locator('select').nth(2); // Third select is template select
    await templateSelect.selectOption({ index: 1 }); // Select first non-empty option
    
    // Enter student identifier
    await page.fill('input[placeholder*="student"]', 'test-student-e2e');
    
    // Mock the API response
    await page.route('**/api/scenario-testing/generate', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionId: 'test-session-123',
          template: {
            name: 'Test Scenario',
            difficulty: 'beginner',
            estimatedSavings: '15-25%'
          },
          studentIdentifier: 'test-student-e2e',
          dashboardUrl: '/dashboard?scenario=test-session-123',
          message: 'Scenario generated successfully'
        })
      });
    });
    
    // Click generate button
    await page.click('button:has-text("Generate Scenario")');
    
    // Wait for success message
    await expect(page.locator('text=Scenario Generated Successfully!')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=test-session-123')).toBeVisible();
    await expect(page.locator('text=Test Scenario')).toBeVisible();
  });

  test('should display template details when template is selected', async ({ page }) => {
    // Wait for templates to load
    await page.waitForSelector('select option:not([value=""])', { timeout: 10000 });
    
    // Select a template
    const templateSelect = page.locator('select').nth(2);
    await templateSelect.selectOption({ index: 1 });
    
    // Wait for template details to appear
    await expect(page.locator('text=Selected Template Details')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('text=Description:')).toBeVisible();
    await expect(page.locator('text=Time to Complete:')).toBeVisible();
    await expect(page.locator('text=Skills Required:')).toBeVisible();
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Mock API failure for templates
    await page.route('**/api/scenario-testing/templates', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Server error' })
      });
    });
    
    // Reload page to trigger API call
    await page.reload();
    
    // Check error message appears
    await expect(page.locator('text=Failed to fetch templates')).toBeVisible({ timeout: 10000 });
  });

  test('should show loading state during scenario generation', async ({ page }) => {
    // Wait for templates to load
    await page.waitForSelector('select option:not([value=""])', { timeout: 10000 });
    
    // Fill form
    const templateSelect = page.locator('select').nth(2);
    await templateSelect.selectOption({ index: 1 });
    await page.fill('input[placeholder*="student"]', 'test-student-loading');
    
    // Mock slow API response
    await page.route('**/api/scenario-testing/generate', async route => {
      await new Promise(resolve => setTimeout(resolve, 2000)); // 2 second delay
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionId: 'test-loading-123',
          template: { name: 'Test Scenario' },
          studentIdentifier: 'test-student-loading'
        })
      });
    });
    
    // Click generate and immediately check loading state
    await page.click('button:has-text("Generate Scenario")');
    await expect(page.locator('button:has-text("Generating...")')).toBeVisible();
    await expect(page.locator('button:disabled')).toBeVisible();
    
    // Wait for completion
    await expect(page.locator('text=Scenario Generated Successfully!')).toBeVisible({ timeout: 10000 });
  });

  test('should reset form when "Generate Another" is clicked', async ({ page }) => {
    // Generate a scenario first
    await page.waitForSelector('select option:not([value=""])', { timeout: 10000 });
    
    const templateSelect = page.locator('select').nth(2);
    await templateSelect.selectOption({ index: 1 });
    await page.fill('input[placeholder*="student"]', 'test-reset');
    
    await page.route('**/api/scenario-testing/generate', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionId: 'test-reset-123',
          template: { name: 'Test Scenario' },
          studentIdentifier: 'test-reset'
        })
      });
    });
    
    await page.click('button:has-text("Generate Scenario")');
    await expect(page.locator('text=Scenario Generated Successfully!')).toBeVisible({ timeout: 10000 });
    
    // Click "Generate Another"
    await page.click('button:has-text("Generate Another")');
    
    // Check form is reset
    await expect(page.locator('select').nth(2)).toHaveValue('');
    await expect(page.locator('input[placeholder*="student"]')).toHaveValue('');
    await expect(page.locator('text=Scenario Generated Successfully!')).not.toBeVisible();
  });

  test('should work on mobile devices', async ({ page, isMobile }) => {
    if (isMobile) {
      // Check that mobile layout renders properly
      await expect(page.locator('h1')).toBeVisible();
      await expect(page.locator('button:has-text("Generate Scenario")')).toBeVisible();
      
      // Check that form elements are still accessible on mobile
      await expect(page.locator('select').first()).toBeVisible();
      await expect(page.locator('input[placeholder*="student"]')).toBeVisible();
    }
  });
});