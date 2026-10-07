import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  retries: 1,
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'retain-on-failure',
  },
  webServer: {
    command: 'sh -c "cd .. && ./mvnw -q spring-boot:run"',
    url: 'http://localhost:8080/api/v1/temas',
    reuseExistingServer: true,
    timeout: 240000,
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
