import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface RegisterPaymentRequest {
  installmentNumber: number;
  paidOn: string;
  amount: number;
}

@Injectable({ providedIn: 'root' })
export class PaymentApiService {
  private readonly http = inject(HttpClient);

  register(contractId: string, request: RegisterPaymentRequest): Promise<unknown> {
    return firstValueFrom(
      this.http.post(`/api/contracts/${encodeURIComponent(contractId)}/payments`, request),
    );
  }
}
