import { expect, test } from '@playwright/test';

test('portal opens', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('app-root')).toBeVisible();
});
