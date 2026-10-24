import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  // inject() only works before the first await.
  const router = inject(Router);
  const user = auth.user() ?? (await auth.loadCurrentUser());
  return user ? true : router.createUrlTree(['/entrar']);
};
