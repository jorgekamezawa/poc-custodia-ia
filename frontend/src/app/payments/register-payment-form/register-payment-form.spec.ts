import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PaymentQuote } from '../payment-api.service';
import { RegisterPaymentForm, todayIso } from './register-payment-form';

registerLocaleData(localePt);

const ON_TIME: PaymentQuote = {
  installmentNumber: 2,
  installmentAmount: 945.6,
  daysLate: 0,
  lateFee: 0,
  defaultInterest: 0,
  total: 945.6,
};

const LATE: PaymentQuote = {
  installmentNumber: 2,
  installmentAmount: 945.6,
  daysLate: 14,
  lateFee: 18.91,
  defaultInterest: 4.41,
  total: 968.92,
};

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

  function answerQuote(paidOn: string, quote: PaymentQuote): void {
    const request = http.expectOne(
      (r) =>
        r.url === '/api/contracts/c1/payments/quote' &&
        r.params.get('installmentNumber') === '2' &&
        r.params.get('paidOn') === paidOn,
    );
    request.flush(quote);
  }

  function inputs(fixture: ReturnType<typeof render>): HTMLInputElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('input'));
  }

  it('começa com a data de hoje e o valor devido nela', async () => {
    const fixture = render();
    answerQuote(todayIso(), ON_TIME);
    await settle(fixture);

    expect(inputs(fixture)[0].value).toBe(todayIso());
    expect(inputs(fixture)[1].value).toBe('945.6');
  });

  it('0008/CT-11: mostra multa e juros, preenche o total e pede nova simulação ao mudar a data', async () => {
    const fixture = render();
    answerQuote(todayIso(), LATE);
    await settle(fixture);

    const text = fixture.nativeElement.textContent.replace(/\u00a0/g, ' ');
    expect(text).toContain('R$ 18,91');
    expect(text).toContain('R$ 4,41');
    expect(text).toContain('R$ 968,92');
    expect(inputs(fixture)[1].value).toBe('968.92');

    const date = inputs(fixture)[0];
    date.value = '2026-10-11';
    date.dispatchEvent(new Event('input'));
    await settle(fixture);
    answerQuote('2026-10-11', { ...LATE, daysLate: 1, defaultInterest: 0.32, total: 964.83 });
    await settle(fixture);

    expect(inputs(fixture)[1].value).toBe('964.83');
  });

  it('recusa da simulação aparece no formulário', async () => {
    const fixture = render();
    http
      .expectOne((r) => r.url === '/api/contracts/c1/payments/quote')
      .flush(
        { detail: 'Data do pagamento deve ficar entre a liberação e hoje' },
        { status: 400, statusText: 'Bad Request' },
      );
    await settle(fixture);

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Data do pagamento deve ficar entre a liberação e hoje',
    );
  });

  it('0007/CT-22: envio aceito mostra "Pagamento registrado." e avisa a tela', async () => {
    const fixture = render();
    answerQuote(todayIso(), ON_TIME);
    await settle(fixture);
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
    answerQuote(todayIso(), ON_TIME);
    await settle(fixture);

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

describe('todayIso', () => {
  afterEach(() => vi.useRealTimers());

  function at(instant: string): string {
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(new Date(instant));
    return todayIso();
  }

  it('CON-05: às 23:59 de Brasília ainda é o mesmo dia, qualquer que seja o fuso do navegador', () => {
    expect(at('2026-10-25T02:59:59Z')).toBe('2026-10-24');
  });

  it('CON-05: à meia-noite de Brasília já é o dia seguinte', () => {
    expect(at('2026-10-25T03:00:00Z')).toBe('2026-10-25');
  });
});
