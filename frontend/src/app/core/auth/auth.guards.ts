import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) =>
  !!inject(AuthService).user() ||
  inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });

export const ownerGuard: CanActivateFn = () =>
  inject(AuthService).user()?.role === 'OWNER' || inject(Router).createUrlTree(['/app']);

/** The signup token travels in router state (not the URL) so it never lands in history or logs. */
export const signupToken = (): string | undefined =>
  inject(Router).currentNavigation()?.extras.state?.['signupToken'];

export const signupGuard: CanActivateFn = () =>
  !!signupToken() || inject(Router).createUrlTree(['/login']);
