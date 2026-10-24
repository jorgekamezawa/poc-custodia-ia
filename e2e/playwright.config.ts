import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  forbidOnly: !!process.env['CI'],
  // Retrying hides a flaky test instead of fixing it.
  retries: 0,
  reporter: process.env['CI'] ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'retain-on-failure',
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
