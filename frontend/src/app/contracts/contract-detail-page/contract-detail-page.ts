import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute } from '@angular/router';
import { ContractApiService, ContractDetail, InstallmentSituation } from '../contract-api.service';
import { RegisterPaymentForm } from '../../payments/register-payment-form/register-payment-form';
import { CpfPipe } from '../cpf.pipe';

const SITUATION_LABELS: Record<InstallmentSituation, string> = {
  OPEN: 'Em aberto',
  OVERDUE: 'Vencida',
  PAID: 'Paga',
};
// The contract learns of a payment through an event, a moment after it is registered.
const REFRESH_ATTEMPTS = 20;
const REFRESH_INTERVAL_MS = 500;

@Component({
  selector: 'app-contract-detail-page',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, CpfPipe, MatButtonModule, RegisterPaymentForm],
  templateUrl: './contract-detail-page.html',
  styleUrl: './contract-detail-page.scss',
})
export class ContractDetailPage {
  private readonly api = inject(ContractApiService);

  protected readonly contract = signal<ContractDetail | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly situationLabels = SITUATION_LABELS;
  protected readonly paying = signal(false);
  protected readonly nextToPay = computed(
    () =>
      this.contract()?.installments.find((installment) => installment.situation !== 'PAID') ?? null,
  );
  private contractId = '';

  constructor() {
    inject(ActivatedRoute)
      .paramMap.pipe(takeUntilDestroyed())
      .subscribe((params) => void this.load(params.get('id') ?? ''));
  }

  protected async onPaymentRegistered(paidInstallment: number): Promise<void> {
    for (let attempt = 0; attempt < REFRESH_ATTEMPTS; attempt++) {
      await this.load(this.contractId);
      const installment = this.contract()?.installments.find((i) => i.number === paidInstallment);
      if (!installment || installment.situation === 'PAID') {
        break;
      }
      await new Promise((resolve) => setTimeout(resolve, REFRESH_INTERVAL_MS));
    }
    this.paying.set(false);
  }

  private async load(id: string): Promise<void> {
    this.contractId = id;
    this.error.set(null);
    try {
      this.contract.set(await this.api.get(id));
    } catch (error) {
      this.contract.set(null);
      this.error.set(
        error instanceof HttpErrorResponse && error.status === 404
          ? 'Contrato não encontrado.'
          : 'Não foi possível consultar agora. Tente de novo.',
      );
    }
  }
}
