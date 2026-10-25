import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  forbidOnly: !!process.env['CI'],
  // Retrying hides a flaky test instead of fixing it.
  retries: 0,
  reporter: process.env['CI'] ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: 'http://localhost:4200',
    // The operators' browsers run in Brasília; the CI machine runs in UTC, a day ahead from 21:00 there.
    timezoneId: 'America/Sao_Paulo',
    trace: 'retain-on-failure',
    // npm run test:headed sets SLOWMO so a person can follow each step on screen.
    launchOptions: { slowMo: Number(process.env['SLOWMO'] ?? 0) },
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  // The backend starts PostgreSQL itself through compose.yaml; the portal proxies /api to it.
  webServer: [
    {
      command: './gradlew bootRun',
      cwd: '../backend',
      port: 8080,
      reuseExistingServer: !process.env['CI'],
      timeout: 240_000,
    },
    {
      command: 'npm start',
      cwd: '../frontend',
      url: 'http://localhost:4200',
      reuseExistingServer: !process.env['CI'],
      timeout: 120_000,
    },
  ],
});
