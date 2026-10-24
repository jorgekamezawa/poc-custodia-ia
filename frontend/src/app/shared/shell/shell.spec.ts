import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService, CurrentUser } from '../../access/auth.service';
import { Shell } from './shell';

@Component({ template: '' })
class Blank {}

describe('Shell', () => {
  function render(user: CurrentUser) {
    TestBed.configureTestingModule({
      imports: [Shell],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'entrar', component: Blank }]),
      ],
    });
    TestBed.inject(AuthService).user.set(user);
    const fixture = TestBed.createComponent(Shell);
    fixture.detectChanges();
    return fixture;
  }

  it('0004/CT-13: mostra o nome, o perfil e o botão Sair', async () => {
    const fixture = render({ username: 'operador', displayName: 'Operador', roles: ['OPERATOR'] });
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Custódia · Banco Exemplo');
    expect(text).toContain('Operador');
    expect(fixture.nativeElement.querySelector('[data-testid="role"]').textContent).toBe(
      'Operador',
    );
    expect(
      await TestbedHarnessEnvironment.loader(fixture).getHarness(
        MatButtonHarness.with({ text: 'Sair' }),
      ),
    ).toBeTruthy();
  });

  it('mostra Supervisor para quem tem esse perfil', () => {
    const fixture = render({
      username: 'supervisor',
      displayName: 'Supervisor',
      roles: ['OPERATOR', 'SUPERVISOR'],
    });

    expect(fixture.nativeElement.querySelector('[data-testid="role"]').textContent).toContain(
      'Supervisor',
    );
  });

  it('0004/CT-14: Sair encerra a sessão e leva à entrada', async () => {
    const fixture = render({ username: 'operador', displayName: 'Operador', roles: ['OPERATOR'] });
    const button = await TestbedHarnessEnvironment.loader(fixture).getHarness(
      MatButtonHarness.with({ text: 'Sair' }),
    );

    await button.click();
    const request = TestBed.inject(HttpTestingController).expectOne('/api/auth/logout');
    expect(request.request.method).toBe('POST');
    request.flush(null, { status: 204, statusText: 'No Content' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(TestBed.inject(Router).url).toBe('/entrar');
    expect(TestBed.inject(AuthService).user()).toBeNull();
  });
});
