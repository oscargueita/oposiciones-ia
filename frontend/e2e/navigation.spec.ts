import { test, expect } from '@playwright/test';

test('navigates the 4 views', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: /Oposiciones IA/ })).toBeVisible();
  for (const [link, heading] of [
    ['Temas', /Temas \(/],
    ['Repaso', /Repaso por palabra/],
    ['Tests', /^Tests$/],
    ['Chuleta/Mapa', /Chuleta y mapa/],
  ] as const) {
    await page.getByRole('navigation').getByRole('link', { name: link, exact: true }).click();
    await expect(page.getByRole('heading', { name: heading }).first()).toBeVisible();
  }
});

test('topics list shows loaded content', async ({ page }) => {
  await page.goto('/temas');
  await expect(page.getByText(/fragmentos/).first()).toBeVisible({ timeout: 15000 });
});
