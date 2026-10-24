import { HttpClient, HttpParams } from '@angular/common/http';
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

export interface ContractSummary {
  id: string;
  number: string;
  customerName: string;
  customerCpf: string;
  principal: number;
  registeredAt: string;
}

export type InstallmentSituation = 'OPEN' | 'OVERDUE';

export interface ContractDetail {
  id: string;
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
  annualCetPercent: number;
  status: string;
  registeredBy: string;
  registeredAt: string;
  outstandingBalance: number;
  installments: (ContractInstallment & { situation: InstallmentSituation })[];
}

@Injectable({ providedIn: 'root' })
export class ContractApiService {
  private readonly http = inject(HttpClient);

  register(request: RegisterContractRequest): Promise<ContractResponse> {
    return firstValueFrom(this.http.post<ContractResponse>('/api/contracts', request));
  }

  search(query: string): Promise<ContractSummary[]> {
    const params = query ? new HttpParams().set('q', query) : undefined;
    return firstValueFrom(this.http.get<ContractSummary[]>('/api/contracts', { params }));
  }

  get(id: string): Promise<ContractDetail> {
    return firstValueFrom(
      this.http.get<ContractDetail>(`/api/contracts/${encodeURIComponent(id)}`),
    );
  }
}
