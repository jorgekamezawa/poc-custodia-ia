import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { todayIso } from '../../payments/register-payment-form/register-payment-form';
import { ContractDetail } from '../contract-api.service';
import { ContractDetailPage } from './contract-detail-page';

registerLocaleData(localePt);

function detail(): ContractDetail {
  return {
    id: 'c1',
    number: 'CTR-0001',
    customerName: 'Cliente Exemplo',
    customerCpf: '12345678909',
    chassisNumber: '9BWZZZ377VT004251',
    vehicleDescription: 'Hatch 1.0 2026',
    amortizationSystem: 'PRICE',
    principal: 10000,
    monthlyRatePercent: 2,
    termMonths: 2,
    releaseDate: '2026-09-10',
    firstDueDate: '2026-10-10',
    upfrontCosts: 0,
    annualCetPercent: 26.95,
    status: 'ACTIVE',
    registeredBy: 'operador',
    registeredAt: '2026-09-10T13:00:00Z',
    outstandingBalance: 10000,
    installments: [
      {
        number: 1,
        dueDate: '2026-10-10',
        amount: 5050.5,
        interest: 200,
        amortization: 4850.5,
        balanceAfter: 5149.5,
        situation: 'OVERDUE',
      },
      {
        number: 2,
        dueDate: '2026-11-10',
        amount: 5252.49,
        interest: 102.99,
        amortization: 5149.5,
        balanceAfter: 0,
        situation: 'OPEN',
      },
    ],
  };
}

describe('ContractDetailPage', () => {
  let harness: RouterTestingHarness;
  let http: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'contratos/:id', component: ContractDetailPage }]),
        { provide: LOCALE_ID, useValue: 'pt-BR' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => http.verify());

  async function settle(): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve));
    harness.detectChanges();
  }

  it('0006/CT-15: mostra o saldo devedor e a situação de cada parcela', async () => {
    await harness.navigateByUrl('/contratos/c1', ContractDetailPage);
    http.expectOne('/api/contracts/c1').flush(detail());
    await settle();

    // pt-BR currency puts a no-break space after R$.
    const text = harness.routeNativeElement!.textContent!.replace(/\u00a0/g, ' ');
    expect(text).toContain('CTR-0001');
    expect(text).toContain('R$ 10.000,00');
    expect(text).toContain('26,95%');
    expect(text).toMatch(/Primeiro vencimento\s*10\/10\/2026/);
    expect(text).toMatch(/Cadastrado por\s*operador/);
    const rows = harness.routeNativeElement!.querySelectorAll('tr.installment');
    expect(rows[0].textContent).toContain('Vencida');
    expect(rows[1].textContent).toContain('Em aberto');
  });

  it('0007/CT-21: "Registrar pagamento" só na parcela não paga mais antiga, com o valor dela', async () => {
    const paidFirst = detail();
    paidFirst.installments[0] = {
      ...paidFirst.installments[0],
      situation: 'PAID',
      paidOn: '2026-10-09',
    };
    await harness.navigateByUrl('/contratos/c1', ContractDetailPage);
    http.expectOne('/api/contracts/c1').flush(paidFirst);
    await settle();

    const rows = harness.routeNativeElement!.querySelectorAll('tr.installment');
    expect(rows[0].textContent).toContain('Paga');
    expect(rows[0].querySelector('button')).toBeNull();
    const button = rows[1].querySelector('button') as HTMLButtonElement;
    expect(button.textContent).toContain('Registrar pagamento');

    button.click();
    await settle();
    http
      .expectOne((r) => r.url === '/api/contracts/c1/payments/quote')
      .flush({
        installmentNumber: 2,
        installmentAmount: 5252.49,
        daysLate: 0,
        lateFee: 0,
        defaultInterest: 0,
        total: 5252.49,
      });
    await settle();
    const amount = harness.routeNativeElement!.querySelector(
      'app-register-payment-form input[type="number"]',
    ) as HTMLInputElement;
    expect(amount.value).toBe('5252.49');
    const paidOn = harness.routeNativeElement!.querySelector(
      'app-register-payment-form input[type="date"]',
    ) as HTMLInputElement;
    expect(paidOn.value).toBe(todayIso());
  });

  it('0007/CT-22: depois do pagamento registrado a consulta é feita de novo', async () => {
    await harness.navigateByUrl('/contratos/c1', ContractDetailPage);
    http.expectOne('/api/contracts/c1').flush(detail());
    await settle();
    (
      harness.routeNativeElement!.querySelector('tr.installment button') as HTMLButtonElement
    ).click();
    await settle();
    http
      .expectOne((r) => r.url === '/api/contracts/c1/payments/quote')
      .flush({
        installmentNumber: 1,
        installmentAmount: 5050.5,
        daysLate: 0,
        lateFee: 0,
        defaultInterest: 0,
        total: 5050.5,
      });
    await settle();

    (
      harness.routeNativeElement!.querySelector(
        'app-register-payment-form button[type="submit"]',
      ) as HTMLButtonElement
    ).click();
    http
      .expectOne('/api/contracts/c1/payments')
      .flush({ id: 'p1' }, { status: 201, statusText: 'Created' });
    await settle();

    const paid = detail();
    paid.installments[0] = { ...paid.installments[0], situation: 'PAID', paidOn: '2026-10-24' };
    http.expectOne('/api/contracts/c1').flush(paid);
    await settle();
    expect(harness.routeNativeElement!.querySelector('tr.installment')!.textContent).toContain(
      'Paga',
    );
  });

  it('0006/CT-16: contrato que não existe mostra a mensagem', async () => {
    await harness.navigateByUrl('/contratos/nao-existe', ContractDetailPage);
    http
      .expectOne('/api/contracts/nao-existe')
      .flush({ detail: 'Contrato não encontrado' }, { status: 404, statusText: 'Not Found' });
    await settle();

    expect(harness.routeNativeElement!.textContent).toContain('Contrato não encontrado.');
  });
});
