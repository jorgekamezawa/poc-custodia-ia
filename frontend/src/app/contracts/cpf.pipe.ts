import { Pipe, PipeTransform } from '@angular/core';

/** Shows 11 CPF digits as 123.456.789-09. */
@Pipe({ name: 'cpf' })
export class CpfPipe implements PipeTransform {
  transform(digits: string): string {
    return /^\d{11}$/.test(digits)
      ? `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`
      : digits;
  }
}
