import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PaymentApiService } from '../payment-api.service';

/** Today in the browser's time zone, as the yyyy-MM-dd a date input expects. */
export function todayIso(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

@Component({
  selector: 'app-register-payment-form',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './register-payment-form.html',
  styleUrl: './register-payment-form.scss',
})
export class RegisterPaymentForm implements OnInit {
  private readonly api = inject(PaymentApiService);

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

  ngOnInit(): void {
    this.form.controls.amount.setValue(this.amount());
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
      this.error.set(
        error instanceof HttpErrorResponse && typeof error.error?.detail === 'string'
          ? error.error.detail
          : 'Não foi possível registrar agora. Tente de novo.',
      );
    } finally {
      this.submitting.set(false);
    }
  }
}
