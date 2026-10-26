import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './errors';

const problem = (code: string, status = 400) => new HttpErrorResponse({ status, error: { code } });

describe('errorMessage', () => {
  it.each([
    ['invalid_mobile', /valid 10-digit/],
    ['otp_invalid', /wrong or has expired/],
    ['otp_rate_limited', /Too many codes/],
    ['otp_attempts_exceeded', /Too many wrong attempts/],
    ['user_inactive', /inactive/],
  ])('maps %s', (code, expected) => {
    expect(errorMessage(problem(code))).toMatch(expected);
  });

  it('falls back to a generic message', () => {
    expect(errorMessage(problem('something_new', 500))).toBe('Something went wrong. Please try again.');
    expect(errorMessage(new Error('x'))).toBe('Something went wrong. Please try again.');
  });

  it('reports a network failure', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toMatch(/internet/);
  });
});
