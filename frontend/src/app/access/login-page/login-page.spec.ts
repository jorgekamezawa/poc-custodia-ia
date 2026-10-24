import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import { MatInputHarness } from '@angular/material/input/testing';
import { provideRouter, Router } from '@angular/router';
import { LoginPage } from './login-page';

@Component({ template: '' })
class Blank {}

describe('LoginPage', () => {
  let loader: HarnessLoader;
  let http: HttpTestingController;
  let router: Router;
  let element: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'entrar', component: LoginPage },
          { path: '', component: Blank },
        ]),
      ],
    });
    router = TestBed.inject(Router);
    await router.navigateByUrl('/entrar');
    const fixture = TestBed.createComponent(LoginPage);
    element = fixture.nativeElement;
    loader = TestbedHarnessEnvironment.loader(fixture);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function fill(username: string, password: string): Promise<void> {
    const [user, pass] = await loader.getAllHarnesses(MatInputHarness);
    await user.setValue(username);
    await pass.setValue(password);
  }

  async function submit(): Promise<void> {
    const button = await loader.getHarness(MatButtonHarness.with({ text: 'Entrar' }));
    await button.click();
  }

  it('0004/CT-9: login aceito leva ao Início', async () => {
    await fill('operador', 'operador-demo');
    await submit();

    const request = http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.toString()).toBe('username=operador&password=operador-demo');
    request.flush({ username: 'operador', displayName: 'Operador', roles: ['OPERATOR'] });
    await TestBed.inject(Router).navigated;
    await new Promise((resolve) => setTimeout(resolve));

    expect(router.url).toBe('/');
  });

  it('0004/CT-10: login recusado mostra a mensagem e continua na entrada', async () => {
    await fill('operador', 'senha-errada');
    await submit();

    http
      .expectOne('/api/auth/login')
      .flush(
        { message: 'Usuário ou senha inválidos' },
        { status: 401, statusText: 'Unauthorized' },
      );
    await new Promise((resolve) => setTimeout(resolve));
    TestBed.tick();

    expect(element.textContent).toContain('Usuário ou senha inválidos');
    expect(router.url).toBe('/entrar');
  });

  it('0004/CT-11: campos vazios mostram o que falta e nada é enviado', async () => {
    await submit();
    TestBed.tick();

    expect(element.textContent).toContain('Informe o usuário');
    expect(element.textContent).toContain('Informe a senha');
    http.expectNone('/api/auth/login');
  });
});
