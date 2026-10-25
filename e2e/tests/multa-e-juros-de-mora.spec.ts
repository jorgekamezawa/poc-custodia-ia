import { expect, test } from '@playwright/test';
import { isoDaysFromToday } from './brasilia-dates';

test('0008/CT-12: parcela vencida há 14 dias é paga com multa e juros de mora', async ({
  page,
}) => {
  await page.goto('/entrar');
  await page.getByLabel('Usuário').fill('operador');
  await page.getByLabel('Senha').fill('operador-demo');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: 'Bem-vindo, Operador.' })).toBeVisible();

  const number = `CTR-MJ-${Date.now()}`;
  await page.getByRole('link', { name: 'Cadastrar contrato' }).click();
  await page.getByLabel('Número do contrato').fill(number);
  await page.getByLabel('Nome').fill('Cliente Exemplo');
  await page.getByLabel('CPF').fill('12345678909');
  await page.getByLabel('Chassi').fill('9BWZZZ377VT004251');
  await page.getByLabel('Descrição do veículo').fill('Hatch 1.0 2026');
  await page.getByLabel('Valor financiado (R$)').fill('10000');
  await page.getByLabel('Taxa ao mês (%)').fill('2');
  await page.getByLabel('Prazo (meses)').fill('12');
  await page.getByLabel('Data de liberação').fill(isoDaysFromToday(-45));
  await page.getByLabel('Primeiro vencimento').fill(isoDaysFromToday(-14));
  await page.getByRole('button', { name: 'Cadastrar' }).click();
  await expect(page.getByText(`Contrato ${number} cadastrado.`)).toBeVisible();
  await page.getByRole('link', { name: 'Ver contrato' }).click();
  await expect(page.locator('tr.installment').first()).toContainText('Vencida');

  await page.getByRole('button', { name: 'Registrar pagamento' }).click();
  const form = page.locator('app-register-payment-form');
  await expect(form).toContainText('R$ 18,91');
  await expect(form).toContainText('R$ 4,41');
  await expect(form).toContainText('R$ 968,92');
  await page.getByRole('button', { name: 'Registrar', exact: true }).click();

  await expect(page.locator('tr.installment').first()).toContainText('Paga', { timeout: 15_000 });
});
