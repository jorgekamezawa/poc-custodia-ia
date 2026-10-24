import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ContractApiService, ContractSummary } from '../contract-api.service';
import { CpfPipe } from '../cpf.pipe';

@Component({
  selector: 'app-contract-list-page',
  imports: [
    FormsModule,
    RouterLink,
    CurrencyPipe,
    DatePipe,
    CpfPipe,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './contract-list-page.html',
  styleUrl: './contract-list-page.scss',
})
export class ContractListPage {
  private readonly api = inject(ContractApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly term = signal('');
  protected readonly contracts = signal<ContractSummary[] | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    // The search lives in the URL, so a link or the back button brings the same list.
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const query = params.get('busca') ?? '';
      this.term.set(query);
      void this.load(query);
    });
  }

  protected search(): void {
    const query = this.term().trim();
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { busca: query || null },
    });
  }

  private async load(query: string): Promise<void> {
    this.error.set(null);
    try {
      this.contracts.set(await this.api.search(query));
    } catch {
      this.contracts.set(null);
      this.error.set('Não foi possível buscar agora. Tente de novo.');
    }
  }
}
