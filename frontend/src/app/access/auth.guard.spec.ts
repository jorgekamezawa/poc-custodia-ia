import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';

@Component({ template: '' })
class Blank {}

describe('authGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'entrar', component: Blank },
          { path: '', component: Blank, canActivate: [authGuard] },
        ]),
      ],
    });
  });

  it('0004/CT-12: sem sessão a rota do portal leva à entrada', async () => {
    const router = TestBed.inject(Router);
    const navigation = router.navigateByUrl('/');
    await new Promise((resolve) => setTimeout(resolve));

    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/me')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    await navigation;

    expect(router.url).toBe('/entrar');
  });

  it('com sessão a rota do portal abre', async () => {
    const router = TestBed.inject(Router);
    const navigation = router.navigateByUrl('/');
    await new Promise((resolve) => setTimeout(resolve));

    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/me')
      .flush({ username: 'operador', displayName: 'Operador', roles: ['OPERATOR'] });
    await navigation;

    expect(router.url).toBe('/');
  });
});
