import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { ContractApiService, ContractDetail, InstallmentSituation } from '../contract-api.service';
import { CpfPipe } from '../cpf.pipe';

const SITUATION_LABELS: Record<InstallmentSituation, string> = {
  OPEN: 'Em aberto',
  OVERDUE: 'Vencida',
};

@Component({
  selector: 'app-contract-detail-page',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, CpfPipe],
  templateUrl: './contract-detail-page.html',
  styleUrl: './contract-detail-page.scss',
})
export class ContractDetailPage {
  private readonly api = inject(ContractApiService);

  protected readonly contract = signal<ContractDetail | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly situationLabels = SITUATION_LABELS;

  constructor() {
    inject(ActivatedRoute)
      .paramMap.pipe(takeUntilDestroyed())
      .subscribe((params) => void this.load(params.get('id') ?? ''));
  }

  private async load(id: string): Promise<void> {
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
