import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard, ownerGuard } from './auth.guards';
import { AuthService, Me, Role } from './auth.service';

const user = (role: Role) => ({ id: 'u1', name: 'A', mobile: '+919876543210', role, clinic: {} }) as Me;

describe('guards', () => {
  let auth: AuthService;
  const run = (guard: typeof authGuard, url = '/app/staff') =>
    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    );
  const serialize = (r: unknown) => TestBed.inject(Router).serializeUrl(r as UrlTree);

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    auth = TestBed.inject(AuthService);
  });

  it('authGuard redirects to /login with returnUrl when logged out', () => {
    expect(serialize(run(authGuard))).toBe('/login?returnUrl=%2Fapp%2Fstaff');
  });

  it('authGuard allows a logged-in user', () => {
    auth.user.set(user('RECEPTION'));
    expect(run(authGuard)).toBe(true);
  });

  it('ownerGuard allows OWNER only', () => {
    auth.user.set(user('OWNER'));
    expect(run(ownerGuard)).toBe(true);
    auth.user.set(user('DOCTOR'));
    expect(serialize(run(ownerGuard))).toBe('/app');
  });
});
