import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface RegisterPaymentRequest {
  installmentNumber: number;
  paidOn: string;
  amount: number;
}

export interface PaymentQuote {
  installmentNumber: number;
  installmentAmount: number;
  daysLate: number;
  lateFee: number;
  defaultInterest: number;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class PaymentApiService {
  private readonly http = inject(HttpClient);

  register(contractId: string, request: RegisterPaymentRequest): Promise<unknown> {
    return firstValueFrom(
      this.http.post(`/api/contracts/${encodeURIComponent(contractId)}/payments`, request),
    );
  }

  quote(contractId: string, installmentNumber: number, paidOn: string): Promise<PaymentQuote> {
    const params = new HttpParams()
      .set('installmentNumber', installmentNumber)
      .set('paidOn', paidOn);
    return firstValueFrom(
      this.http.get<PaymentQuote>(
        `/api/contracts/${encodeURIComponent(contractId)}/payments/quote`,
        { params },
      ),
    );
  }
}
