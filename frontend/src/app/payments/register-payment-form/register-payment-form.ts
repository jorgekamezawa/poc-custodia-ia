import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PaymentApiService, PaymentQuote } from '../payment-api.service';

/** Today in the browser's time zone, as the yyyy-MM-dd a date input expects. */
export function todayIso(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

function serverMessage(error: unknown, fallback: string): string {
  return error instanceof HttpErrorResponse && typeof error.error?.detail === 'string'
    ? error.error.detail
    : fallback;
}

@Component({
  selector: 'app-register-payment-form',
  imports: [ReactiveFormsModule, CurrencyPipe, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './register-payment-form.html',
  styleUrl: './register-payment-form.scss',
})
export class RegisterPaymentForm implements OnInit {
  private readonly api = inject(PaymentApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly contractId = input.required<string>();
  readonly installmentNumber = input.required<number>();
  readonly amount = input.required<number>();
  readonly registered = output<void>();

  protected readonly form = inject(NonNullableFormBuilder).group({
    paidOn: [todayIso(), Validators.required],
    amount: [0, Validators.required],
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly done = signal(false);
  protected readonly quote = signal<PaymentQuote | null>(null);

  ngOnInit(): void {
    this.form.controls.amount.setValue(this.amount());
    // The amount due depends on the date, and only the backend knows the late charges rules.
    void this.requestQuote(this.form.controls.paidOn.value);
    this.form.controls.paidOn.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((paidOn) => void this.requestQuote(paidOn));
  }

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();
    try {
      await this.api.register(this.contractId(), {
        installmentNumber: this.installmentNumber(),
        paidOn: value.paidOn,
        amount: Number(value.amount),
      });
      this.done.set(true);
      this.registered.emit();
    } catch (error) {
      this.error.set(serverMessage(error, 'Não foi possível registrar agora. Tente de novo.'));
    } finally {
      this.submitting.set(false);
    }
  }

  private async requestQuote(paidOn: string): Promise<void> {
    if (!paidOn) {
      return;
    }
    this.error.set(null);
    try {
      const quote = await this.api.quote(this.contractId(), this.installmentNumber(), paidOn);
      this.quote.set(quote);
      this.form.controls.amount.setValue(quote.total);
    } catch (error) {
      this.quote.set(null);
      this.error.set(serverMessage(error, 'Não foi possível calcular o valor devido agora.'));
    }
  }
}
