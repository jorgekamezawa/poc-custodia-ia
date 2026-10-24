import { expect, Page, test } from '@playwright/test';

function isoDaysFromToday(days: number): string {
  const date = new Date();
  date.setDate(date.getDate() + days);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

async function contractOpenToPay(page: Page): Promise<void> {
  await page.goto('/entrar');
  await page.getByLabel('Usuário').fill('operador');
  await page.getByLabel('Senha').fill('operador-demo');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: 'Bem-vindo, Operador.' })).toBeVisible();

  const number = `CTR-PG-${Date.now()}`;
  await page.getByRole('link', { name: 'Cadastrar contrato' }).click();
  await page.getByLabel('Número do contrato').fill(number);
  await page.getByLabel('Nome').fill('Cliente Exemplo');
  await page.getByLabel('CPF').fill('12345678909');
  await page.getByLabel('Chassi').fill('9BWZZZ377VT004251');
  await page.getByLabel('Descrição do veículo').fill('Hatch 1.0 2026');
  await page.getByLabel('Valor financiado (R$)').fill('10000');
  await page.getByLabel('Taxa ao mês (%)').fill('2');
  await page.getByLabel('Prazo (meses)').fill('12');
  await page.getByLabel('Data de liberação').fill(isoDaysFromToday(-30));
  await page.getByLabel('Primeiro vencimento').fill(isoDaysFromToday(15));
  await page.getByRole('button', { name: 'Cadastrar' }).click();
  await expect(page.getByText(`Contrato ${number} cadastrado.`)).toBeVisible();
  await page.getByRole('link', { name: 'Ver contrato' }).click();
  await page.getByRole('button', { name: 'Registrar pagamento' }).click();
}

test('0007/CT-24: pagamento em dia deixa a parcela paga e o saldo menor', async ({ page }) => {
  await contractOpenToPay(page);

  await page.getByRole('button', { name: 'Registrar', exact: true }).click();

  await expect(page.locator('tr.installment').first()).toContainText('Paga', { timeout: 15_000 });
  await expect(page.locator('.summary div').filter({ hasText: 'Saldo devedor' })).toContainText(
    'R$ 9.254,40',
  );
});

test('0007/CT-25: valor diferente do da parcela é recusado na tela', async ({ page }) => {
  await contractOpenToPay(page);

  await page.getByLabel('Valor (R$)').fill('945.59');
  await page.getByRole('button', { name: 'Registrar', exact: true }).click();

  await expect(page.getByRole('alert')).toContainText('Valor deve ser igual ao da parcela');
});
