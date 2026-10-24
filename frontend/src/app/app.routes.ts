import { Routes } from '@angular/router';
import { authGuard } from './access/auth.guard';
import { Shell } from './shared/shell/shell';

// Pages load on demand, so a screen's Material components only download when it opens.
export const routes: Routes = [
  {
    path: 'entrar',
    loadComponent: () => import('./access/login-page/login-page').then((m) => m.LoginPage),
    title: 'Entrar · Custódia',
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./home/home-page/home-page').then((m) => m.HomePage),
        title: 'Início · Custódia',
      },
      {
        path: 'contratos/novo',
        loadComponent: () =>
          import('./contracts/register-contract-page/register-contract-page').then(
            (m) => m.RegisterContractPage,
          ),
        title: 'Cadastrar contrato · Custódia',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
