import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { ContractSummary } from '../contract-api.service';
import { ContractListPage } from './contract-list-page';

registerLocaleData(localePt);

const TWO: ContractSummary[] = [
  {
    id: 'a1',
    number: 'CTR-0002',
    customerName: 'Cliente Dois',
    customerCpf: '52998224725',
    principal: 20000,
    registeredAt: '2027-01-11T13:00:00Z',
  },
  {
    id: 'a2',
    number: 'CTR-0001',
    customerName: 'Cliente Exemplo',
    customerCpf: '12345678909',
    principal: 10000,
    registeredAt: '2027-01-10T13:00:00Z',
  },
];

describe('ContractListPage', () => {
  let harness: RouterTestingHarness;
  let http: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'contratos', component: ContractListPage }]),
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

  it('0006/CT-13: a busca vai para o endereço e lista os contratos', async () => {
    await harness.navigateByUrl('/contratos', ContractListPage);
    http.expectOne('/api/contracts').flush([]);
    await settle();

    const input: HTMLInputElement = harness.routeNativeElement!.querySelector('input')!;
    input.value = 'CTR';
    input.dispatchEvent(new Event('input'));
    (
      harness.routeNativeElement!.querySelector('button[type="submit"]') as HTMLButtonElement
    ).click();
    await settle();

    http
      .expectOne((request) => request.url === '/api/contracts' && request.params.get('q') === 'CTR')
      .flush(TWO);
    await settle();

    expect(TestBed.inject(Router).url).toBe('/contratos?busca=CTR');
    const rows = harness.routeNativeElement!.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('CTR-0002');
    expect(rows[0].textContent).toContain('529.982.247-25');
    expect(rows[0].textContent).toContain('R$');
  });

  it('0006/CT-14: busca sem resultado mostra a mensagem', async () => {
    await harness.navigateByUrl('/contratos?busca=NAO-EXISTE', ContractListPage);
    http.expectOne((request) => request.params.get('q') === 'NAO-EXISTE').flush([]);
    await settle();

    expect(harness.routeNativeElement!.textContent).toContain('Nenhum contrato encontrado.');
  });
});
