import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService, Me, Session } from './auth.service';

const me: Me = {
  id: 'u1',
  name: 'Dr Amit',
  mobile: '+919876543210',
  role: 'OWNER',
  clinic: { id: 'c1', name: 'Apollo Dental', slug: 'apollo-dental', planStatus: 'TRIAL', trialEndsAt: null },
};
const session = (token: string): Session => ({ accessToken: token, expiresInSeconds: 900, user: me });

describe('AuthService + authInterceptor', () => {
  let http: HttpClient;
  let ctrl: HttpTestingController;
  let auth: AuthService;
  let navigate: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    auth.accessToken.set('old');
    auth.user.set(me);
  });

  afterEach(() => ctrl.verify());

  it('attaches the bearer token to /api calls', () => {
    http.get('/api/me').subscribe();
    expect(ctrl.expectOne('/api/me').request.headers.get('Authorization')).toBe('Bearer old');
  });

  it('does not attach the bearer token to /api/auth/*', () => {
    auth.sendOtp('9876543210').subscribe();
    expect(ctrl.expectOne('/api/auth/otp/send').request.headers.has('Authorization')).toBe(false);
  });

  it('on 401 refreshes once and retries with the new token', () => {
    let result: unknown;
    http.get('/api/me').subscribe((r) => (result = r));
    ctrl.expectOne('/api/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    ctrl.expectOne('/api/auth/refresh').flush(session('new'));

    const retry = ctrl.expectOne('/api/me');
    expect(retry.request.headers.get('Authorization')).toBe('Bearer new');
    retry.flush(me);
    expect(result).toEqual(me);
    expect(auth.accessToken()).toBe('new');
  });

  it('shares one refresh between concurrent 401s', () => {
    http.get('/api/me').subscribe();
    http.get('/api/clinic').subscribe();
    ctrl.expectOne('/api/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    ctrl.expectOne('/api/clinic').flush(null, { status: 401, statusText: 'Unauthorized' });

    ctrl.expectOne('/api/auth/refresh').flush(session('new'));

    expect(ctrl.expectOne('/api/me').request.headers.get('Authorization')).toBe('Bearer new');
    expect(ctrl.expectOne('/api/clinic').request.headers.get('Authorization')).toBe('Bearer new');
  });

  it('clears state and goes to /login when refresh fails', () => {
    let failed = false;
    http.get('/api/me').subscribe({ error: () => (failed = true) });
    ctrl.expectOne('/api/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    ctrl.expectOne('/api/auth/refresh').flush({ code: 'refresh_invalid' }, { status: 401, statusText: 'Unauthorized' });

    expect(failed).toBe(true);
    expect(auth.accessToken()).toBeNull();
    expect(auth.user()).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });

  it('verifyOtp stores the session only when LOGGED_IN', () => {
    auth.clear();
    auth.verifyOtp('9876543210', '123456').subscribe();
    ctrl.expectOne('/api/auth/otp/verify').flush({ status: 'SIGNUP_REQUIRED', signupToken: 'st' });
    expect(auth.user()).toBeNull();

    auth.verifyOtp('9876543210', '123456').subscribe();
    ctrl.expectOne('/api/auth/otp/verify').flush({ status: 'LOGGED_IN', ...session('t1') });
    expect(auth.accessToken()).toBe('t1');
    expect(auth.user()).toEqual(me);
  });

  it('logout clears state and calls the server', () => {
    auth.logout().subscribe();
    expect(auth.accessToken()).toBeNull();
    ctrl.expectOne('/api/auth/logout').flush(null, { status: 204, statusText: 'No Content' });
  });
});
