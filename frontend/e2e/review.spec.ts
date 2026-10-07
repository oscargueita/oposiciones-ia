import { test, expect } from '@playwright/test';

test('review finds a known concept', async ({ page }) => {
  await page.goto('/repaso');
  await page.getByPlaceholder(/recurso de alzada/).fill('recurso de alzada');
  await page.getByRole('button', { name: 'Repasar' }).click();
  await expect(page.getByText(/Tema \d+, pág\./).first()).toBeVisible({ timeout: 60000 });
});

test('review without results shows notice', async ({ page }) => {
  await page.goto('/repaso');
  await page.getByPlaceholder(/recurso de alzada/).fill('xyzqwerty');
  await page.getByRole('button', { name: 'Repasar' }).click();
  await expect(page.getByText(/Sin resultados/)).toBeVisible({ timeout: 60000 });
});
