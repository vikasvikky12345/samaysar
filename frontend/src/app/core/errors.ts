import { HttpErrorResponse } from '@angular/common/http';

const messages: Record<string, string> = {
  invalid_mobile: 'Please enter a valid 10-digit Indian mobile number.',
  otp_invalid: 'That code is wrong or has expired. Please try again.',
  otp_rate_limited: 'Too many codes requested. Please wait 15 minutes and try again.',
  otp_attempts_exceeded: 'Too many wrong attempts. Please request a new code.',
  user_inactive: 'Your account is inactive. Please contact your clinic owner.',
  signup_token_invalid: 'Your verification expired. Please log in again.',
  mobile_already_registered: 'This mobile number is already registered.',
  cannot_modify_self: 'You cannot deactivate or change your own account.',
  validation_failed: 'Please check the details and try again.',
};

/** Maps a ProblemDetail `code` from the API to a message we can show a receptionist. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse && err.status === 0) {
    return 'Cannot reach the server. Please check your internet connection.';
  }
  const code = err instanceof HttpErrorResponse ? err.error?.code : undefined;
  return messages[code] ?? 'Something went wrong. Please try again.';
}
