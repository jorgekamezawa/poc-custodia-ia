import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { AmortizationSystem, ContractApiService, ContractResponse } from '../contract-api.service';

@Component({
  selector: 'app-register-contract-page',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    RouterLink,
  ],
  templateUrl: './register-contract-page.html',
  styleUrl: './register-contract-page.scss',
})
export class RegisterContractPage {
  private readonly api = inject(ContractApiService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    number: ['', Validators.required],
    customerName: ['', Validators.required],
    customerCpf: ['', Validators.required],
    chassisNumber: ['', Validators.required],
    vehicleDescription: ['', Validators.required],
    amortizationSystem: ['PRICE' as AmortizationSystem, Validators.required],
    principal: ['', Validators.required],
    monthlyRatePercent: ['', Validators.required],
    termMonths: ['', Validators.required],
    releaseDate: ['', Validators.required],
    firstDueDate: ['', Validators.required],
    upfrontCosts: ['0', Validators.required],
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly registered = signal<ContractResponse | null>(null);

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();
    try {
      this.registered.set(
        await this.api.register({
          number: value.number.trim(),
          customerName: value.customerName,
          customerCpf: value.customerCpf.replace(/\D/g, ''),
          chassisNumber: value.chassisNumber.trim().toUpperCase(),
          vehicleDescription: value.vehicleDescription,
          amortizationSystem: value.amortizationSystem,
          principal: Number(value.principal),
          monthlyRatePercent: Number(value.monthlyRatePercent),
          termMonths: Number(value.termMonths),
          releaseDate: value.releaseDate,
          firstDueDate: value.firstDueDate,
          upfrontCosts: Number(value.upfrontCosts),
        }),
      );
    } catch (error) {
      this.error.set(
        error instanceof HttpErrorResponse && typeof error.error?.detail === 'string'
          ? error.error.detail
          : 'Não foi possível cadastrar agora. Tente de novo.',
      );
    } finally {
      this.submitting.set(false);
    }
  }

  protected registerAnother(): void {
    this.registered.set(null);
    this.form.reset();
  }
}
