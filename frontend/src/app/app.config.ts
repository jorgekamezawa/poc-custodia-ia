import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { unauthorizedInterceptor } from './access/unauthorized.interceptor';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // The default XSRF support reads the XSRF-TOKEN cookie and sends X-XSRF-TOKEN, as the backend expects.
    provideHttpClient(withInterceptors([unauthorizedInterceptor])),
  ],
};
