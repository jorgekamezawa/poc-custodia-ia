import { Routes } from '@angular/router';
import { authGuard } from './access/auth.guard';
import { LoginPage } from './access/login-page/login-page';
import { HomePage } from './home/home-page/home-page';
import { Shell } from './shared/shell/shell';

export const routes: Routes = [
  { path: 'entrar', component: LoginPage, title: 'Entrar · Custódia' },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [{ path: '', component: HomePage, title: 'Início · Custódia' }],
  },
  { path: '**', redirectTo: '' },
];
