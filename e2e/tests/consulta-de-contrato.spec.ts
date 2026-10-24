import { expect, Page, test } from '@playwright/test';

async function enterAsOperator(page: Page): Promise<void> {
  await page.goto('/entrar');
  await page.getByLabel('Usuário').fill('operador');
  await page.getByLabel('Senha').fill('operador-demo');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: 'Bem-vindo, Operador.' })).toBeVisible();
}

async function register(page: Page, number: string, releaseDate: string, firstDueDate: string) {
  await page.getByRole('link', { name: 'Cadastrar contrato' }).click();
  await page.getByLabel('Número do contrato').fill(number);
  await page.getByLabel('Nome').fill('Cliente Exemplo');
  await page.getByLabel('CPF').fill('12345678909');
  await page.getByLabel('Chassi').fill('9BWZZZ377VT004251');
  await page.getByLabel('Descrição do veículo').fill('Hatch 1.0 2026');
  await page.getByLabel('Valor financiado (R$)').fill('10000');
  await page.getByLabel('Taxa ao mês (%)').fill('2');
  await page.getByLabel('Prazo (meses)').fill('12');
  await page.getByLabel('Data de liberação').fill(releaseDate);
  await page.getByLabel('Primeiro vencimento').fill(firstDueDate);
  await page.getByRole('button', { name: 'Cadastrar' }).click();
  await expect(page.getByText(`Contrato ${number} cadastrado.`)).toBeVisible();
}

test('0006/CT-17: busca pelo número e consulta o contrato', async ({ page }) => {
  const number = `CTR-Q-${Date.now()}`;
  await enterAsOperator(page);
  await register(page, number, '2027-01-10', '2027-02-10');

  await page.getByRole('link', { name: 'Contratos', exact: true }).click();
  await page.getByLabel('Buscar por número ou CPF').fill(number);
  await page.getByRole('button', { name: 'Buscar' }).click();
  await expect(page).toHaveURL(new RegExp(`busca=${number}`));
  await page.getByRole('link', { name: number }).click();

  await expect(page.getByRole('heading', { name: `Contrato ${number}` })).toBeVisible();
  await expect(page.locator('.summary div').filter({ hasText: 'Saldo devedor' })).toContainText(
    'R$ 10.000,00',
  );
  const installments = page.locator('tr.installment');
  await expect(installments).toHaveCount(12);
  await expect(installments.filter({ hasText: 'Em aberto' })).toHaveCount(12);
});

test('0006/CT-18: parcela de vencimento passado aparece vencida', async ({ page }) => {
  const number = `CTR-V-${Date.now()}`;
  await enterAsOperator(page);
  await register(page, number, '2026-01-10', '2026-02-10');

  await page.getByRole('link', { name: 'Ver contrato' }).click();

  const installments = page.locator('tr.installment');
  await expect(installments.first()).toContainText('Vencida');
  await expect(installments.last()).toContainText('Em aberto');
});
