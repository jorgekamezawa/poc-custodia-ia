import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export type AmortizationSystem = 'PRICE' | 'SAC';

export interface RegisterContractRequest {
  number: string;
  customerName: string;
  customerCpf: string;
  chassisNumber: string;
  vehicleDescription: string;
  amortizationSystem: AmortizationSystem;
  principal: number;
  monthlyRatePercent: number;
  termMonths: number;
  releaseDate: string;
  firstDueDate: string;
  upfrontCosts: number;
}

export interface ContractInstallment {
  number: number;
  dueDate: string;
  amount: number;
  interest: number;
  amortization: number;
  balanceAfter: number;
}

export interface ContractResponse {
  id: string;
  number: string;
  status: string;
  annualCetPercent: number;
  registeredBy: string;
  registeredAt: string;
  installments: ContractInstallment[];
}

@Injectable({ providedIn: 'root' })
export class ContractApiService {
  private readonly http = inject(HttpClient);

  register(request: RegisterContractRequest): Promise<ContractResponse> {
    return firstValueFrom(this.http.post<ContractResponse>('/api/contracts', request));
  }
}
