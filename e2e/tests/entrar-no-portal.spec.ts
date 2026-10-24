import { expect, Page, test } from '@playwright/test';

async function enter(page: Page, username: string, password: string): Promise<void> {
  await page.goto('/entrar');
  await page.getByLabel('Usuário').fill(username);
  await page.getByLabel('Senha').fill(password);
  await page.getByRole('button', { name: 'Entrar' }).click();
}

test('0004/CT-15: operador entra, vê o Início e sai', async ({ page }) => {
  await enter(page, 'operador', 'operador-demo');

  await expect(page.getByRole('heading', { name: 'Bem-vindo, Operador.' })).toBeVisible();
  await expect(page).toHaveURL('/');

  await page.getByRole('button', { name: 'Sair' }).click();
  await expect(page).toHaveURL('/entrar');
  await page.goto('/');
  await expect(page).toHaveURL('/entrar');
});

test('0004/CT-16: senha errada mostra a recusa', async ({ page }) => {
  await enter(page, 'operador', 'senha-errada');

  await expect(page.getByRole('alert')).toHaveText('Usuário ou senha inválidos');
  await expect(page).toHaveURL('/entrar');
});

test('0004/CT-17: sem login o portal leva à entrada', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveURL('/entrar');
  await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible();
});

test('0004/CT-18: supervisor vê o próprio perfil', async ({ page }) => {
  await enter(page, 'supervisor', 'supervisor-demo');

  await expect(page.getByTestId('role')).toHaveText('Supervisor');
});
