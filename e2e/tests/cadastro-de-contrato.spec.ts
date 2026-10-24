import { expect, Page, test } from '@playwright/test';

async function enterAsOperator(page: Page): Promise<void> {
  await page.goto('/entrar');
  await page.getByLabel('Usuário').fill('operador');
  await page.getByLabel('Senha').fill('operador-demo');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: 'Bem-vindo, Operador.' })).toBeVisible();
}

async function registerExample(page: Page, number: string): Promise<void> {
  await page.getByLabel('Número do contrato').fill(number);
  await page.getByLabel('Nome').fill('Cliente Exemplo');
  await page.getByLabel('CPF').fill('12345678909');
  await page.getByLabel('Chassi').fill('9BWZZZ377VT004251');
  await page.getByLabel('Descrição do veículo').fill('Hatch 1.0 2026');
  await page.getByLabel('Valor financiado (R$)').fill('10000');
  await page.getByLabel('Taxa ao mês (%)').fill('2');
  await page.getByLabel('Prazo (meses)').fill('12');
  await page.getByLabel('Data de liberação').fill('2027-01-10');
  await page.getByLabel('Primeiro vencimento').fill('2027-02-10');
  await page.getByRole('button', { name: 'Cadastrar' }).click();
}

test('0005/CT-25: operador cadastra o contrato pelo menu e vê CET e parcelas', async ({ page }) => {
  const number = `CTR-E2E-${Date.now()}`;
  await enterAsOperator(page);
  await page.getByRole('link', { name: 'Cadastrar contrato' }).click();

  await registerExample(page, number);

  await expect(page.getByText(`Contrato ${number} cadastrado.`)).toBeVisible();
  await expect(page.getByText('26,95% ao ano')).toBeVisible();
  const installments = page.locator('tr.installment');
  await expect(installments).toHaveCount(12);
  await expect(installments.last()).toContainText('945,55');
});

test('0005/CT-26: o mesmo número de novo é recusado na tela', async ({ page }) => {
  const number = `CTR-E2E-${Date.now()}`;
  await enterAsOperator(page);
  await page.getByRole('link', { name: 'Cadastrar contrato' }).click();
  await registerExample(page, number);
  await expect(page.getByText(`Contrato ${number} cadastrado.`)).toBeVisible();

  await page.getByRole('button', { name: 'Cadastrar outro' }).click();
  await registerExample(page, number);

  await expect(page.getByRole('alert')).toHaveText('Número do contrato já cadastrado');
});
