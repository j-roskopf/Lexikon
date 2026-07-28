import { test, expect } from '@playwright/test';

test('loads daily 5 route', async ({ page }) => {
  await page.goto('/daily/5?e2e=1');
  await expect(page.getByText('Lexikon', { exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: '5', exact: true })).toBeVisible();
});

// No click first: the board has to accept the physical keyboard as soon as it loads.
// Six valid guesses end the game either way, and the result panel only shows up if every
// keystroke landed on the board.
test('keyboard submits guess @e2e', async ({ page }) => {
  await page.goto('/free/5?e2e=1');
  await expect(page.getByRole('button', { name: '5', exact: true })).toBeVisible();
  for (let i = 0; i < 6; i++) {
    await page.keyboard.type('crane');
    await page.keyboard.press('Enter');
  }
  await expect(page.getByRole('button', { name: 'Copy result' })).toBeVisible();
});

test('free play route length 8', async ({ page }) => {
  await page.goto('/free/8?e2e=1');
  await expect(page.getByRole('button', { name: '8', exact: true })).toBeVisible();
});

test('desktop viewport screenshot', async ({ page }) => {
  await page.goto('/daily/5?screenshot=board');
  await expect(page).toHaveScreenshot('daily-5-desktop.png');
});

test('mobile viewport screenshot', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/daily/5?screenshot=board');
  await expect(page).toHaveScreenshot('daily-5-mobile.png');
});
