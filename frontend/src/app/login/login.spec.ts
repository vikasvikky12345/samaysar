import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Login } from './login';

describe('Login', () => {
  let el: HTMLElement;
  let ctrl: HttpTestingController;
  let fixture: ReturnType<typeof TestBed.createComponent<Login>>;

  const type = (selector: string, value: string) => {
    const input = el.querySelector<HTMLInputElement>(selector)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  };
  const submit = async () => {
    el.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
  };

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    ctrl = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Login);
    el = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('blocks submit for an invalid mobile and shows the error', async () => {
    type('#mobile', '5123456789');
    await submit();
    ctrl.expectNone('/api/auth/otp/send');
    const input = el.querySelector('#mobile')!;
    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(el.querySelector('#mobile-error')?.textContent).toContain('10-digit');
  });

  it('goes to /signup with the token in router state on SIGNUP_REQUIRED', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    type('#mobile', '9876543210');
    await submit();
    ctrl.expectOne('/api/auth/otp/send').flush({ expiresInSeconds: 300 });
    await fixture.whenStable();

    expect(document.activeElement?.id).toBe('otp');
    type('#otp', '123456');
    await submit();
    const req = ctrl.expectOne('/api/auth/otp/verify');
    expect(req.request.body).toEqual({ mobile: '9876543210', code: '123456' });
    req.flush({ status: 'SIGNUP_REQUIRED', signupToken: 'st' });

    expect(navigate).toHaveBeenCalledWith(['/signup'], { state: { signupToken: 'st' } });
  });

  it('shows a friendly message for a wrong code', async () => {
    type('#mobile', '9876543210');
    await submit();
    ctrl.expectOne('/api/auth/otp/send').flush({ expiresInSeconds: 300 });
    await fixture.whenStable();
    type('#otp', '000000');
    await submit();
    ctrl.expectOne('/api/auth/otp/verify').flush({ code: 'otp_invalid' }, { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();
    expect(el.querySelector('[role=alert]')?.textContent).toContain('wrong or has expired');
  });
});
