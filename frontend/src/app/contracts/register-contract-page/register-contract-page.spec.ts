import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import { MatInputHarness } from '@angular/material/input/testing';
import { ContractResponse } from '../contract-api.service';
import { RegisterContractPage } from './register-contract-page';

registerLocaleData(localePt);

const FILLED: Record<string, string> = {
  number: 'CTR-0001',
  customerName: 'Cliente Exemplo',
  customerCpf: '123.456.789-09',
  chassisNumber: '9bwzzz377vt004251',
  vehicleDescription: 'Hatch 1.0 2026',
  principal: '10000',
  monthlyRatePercent: '2',
  termMonths: '12',
  releaseDate: '2027-01-10',
  firstDueDate: '2027-02-10',
};

function response(): ContractResponse {
  return {
    id: '3f2a6c1e-0000-4000-8000-000000000001',
    number: 'CTR-0001',
    status: 'ACTIVE',
    annualCetPercent: 26.95,
    registeredBy: 'operador',
    registeredAt: '2027-01-10T13:00:00Z',
    installments: Array.from({ length: 12 }, (_, index) => ({
      number: index + 1,
      dueDate: `2027-${String(index + 2).padStart(2, '0')}-10`,
      amount: index === 11 ? 945.55 : 945.6,
      interest: 10,
      amortization: 935.6,
      balanceAfter: 100,
    })),
  };
}

describe('RegisterContractPage', () => {
  let loader: HarnessLoader;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RegisterContractPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'pt-BR' },
      ],
    });
    const fixture = TestBed.createComponent(RegisterContractPage);
    element = fixture.nativeElement;
    loader = TestbedHarnessEnvironment.loader(fixture);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function fill(): Promise<void> {
    for (const [field, value] of Object.entries(FILLED)) {
      const input = await loader.getHarness(
        MatInputHarness.with({ selector: `[formcontrolname="${field}"]` }),
      );
      await input.setValue(value);
    }
  }

  async function submit(): Promise<void> {
    await (await loader.getHarness(MatButtonHarness.with({ text: 'Cadastrar' }))).click();
  }

  async function settle(): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve));
    TestBed.tick();
  }

  it('0005/CT-22: campos vazios mostram "Campo obrigatório" e nada é enviado', async () => {
    await submit();
    TestBed.tick();

    expect(element.querySelectorAll('mat-error').length).toBeGreaterThanOrEqual(10);
    expect(element.textContent).toContain('Campo obrigatório');
    http.expectNone('/api/contracts');
  });

  it('0005/CT-23: cadastro aceito mostra número, CET e as 12 parcelas', async () => {
    await fill();
    await submit();

    const request = http.expectOne('/api/contracts');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      number: 'CTR-0001',
      customerName: 'Cliente Exemplo',
      customerCpf: '12345678909',
      chassisNumber: '9BWZZZ377VT004251',
      vehicleDescription: 'Hatch 1.0 2026',
      amortizationSystem: 'PRICE',
      principal: 10000,
      monthlyRatePercent: 2,
      termMonths: 12,
      releaseDate: '2027-01-10',
      firstDueDate: '2027-02-10',
      upfrontCosts: 0,
    });
    request.flush(response());
    await settle();

    expect(element.textContent).toContain('Contrato CTR-0001 cadastrado.');
    expect(element.textContent).toContain('26,95%');
    expect(element.querySelectorAll('tr.installment').length).toBe(12);
    expect(element.textContent).toContain('945,55');
  });

  it('0005/CT-24: recusa do servidor aparece no topo e os dados ficam', async () => {
    await fill();
    await submit();

    http
      .expectOne('/api/contracts')
      .flush(
        { detail: 'Número do contrato já cadastrado' },
        { status: 409, statusText: 'Conflict' },
      );
    await settle();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain(
      'Número do contrato já cadastrado',
    );
    const number = await loader.getHarness(
      MatInputHarness.with({ selector: '[formcontrolname="number"]' }),
    );
    expect(await number.getValue()).toBe('CTR-0001');
  });
});
