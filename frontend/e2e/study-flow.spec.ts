import { test, expect } from '@playwright/test';

test('upload, listen and delete a topic (self-cleaning)', async ({ page }) => {
  await page.goto('/temas');
  await page.locator('input[type="file"]').setInputFiles('e2e/fixtures/e2e-tema.pdf');
  const article = page.locator('article', { hasText: 'e2e-tema' });
  await expect(article).toBeVisible({ timeout: 120000 });
  await article.getByRole('link', { name: /E2E-TEMA|e2e-tema/ }).first().click();
  await expect(page.getByRole('heading', { name: /Estudio del tema/ })).toBeVisible();
  page.on('dialog', (d) => void d.accept());
  await page.goto('/temas');
  await page.locator('article', { hasText: 'e2e-tema' }).getByRole('button', { name: 'Borrar' }).click();
  await expect(page.locator('article', { hasText: 'e2e-tema' })).toHaveCount(0);
});
