import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { RegisterPaymentForm, todayIso } from './register-payment-form';

registerLocaleData(localePt);

describe('RegisterPaymentForm', () => {
  let http: HttpTestingController;

  function render() {
    TestBed.configureTestingModule({
      imports: [RegisterPaymentForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'pt-BR' },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(RegisterPaymentForm);
    fixture.componentRef.setInput('contractId', 'c1');
    fixture.componentRef.setInput('installmentNumber', 2);
    fixture.componentRef.setInput('amount', 945.6);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => http.verify());

  async function settle(fixture: ReturnType<typeof render>): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve));
    fixture.detectChanges();
  }

  it('começa com a data de hoje e o valor da parcela', () => {
    const fixture = render();
    const inputs = fixture.nativeElement.querySelectorAll('input') as NodeListOf<HTMLInputElement>;

    expect(inputs[0].value).toBe(todayIso());
    expect(inputs[1].value).toBe('945.6');
  });

  it('0007/CT-22: envio aceito mostra "Pagamento registrado." e avisa a tela', async () => {
    const fixture = render();
    let registered = 0;
    fixture.componentInstance.registered.subscribe(() => registered++);

    (fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    const request = http.expectOne('/api/contracts/c1/payments');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      installmentNumber: 2,
      paidOn: todayIso(),
      amount: 945.6,
    });
    request.flush({ id: 'p1' }, { status: 201, statusText: 'Created' });
    await settle(fixture);

    expect(fixture.nativeElement.textContent).toContain('Pagamento registrado.');
    expect(registered).toBe(1);
  });

  it('0007/CT-23: recusa do servidor aparece no formulário', async () => {
    const fixture = render();

    (fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    http
      .expectOne('/api/contracts/c1/payments')
      .flush(
        { detail: 'Valor deve ser igual ao da parcela: R$ 945,60' },
        { status: 400, statusText: 'Bad Request' },
      );
    await settle(fixture);

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Valor deve ser igual ao da parcela: R$ 945,60',
    );
  });
});
