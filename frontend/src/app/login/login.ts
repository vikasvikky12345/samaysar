import { Component, DestroyRef, ElementRef, afterRenderEffect, inject, signal, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { errorMessage } from '../core/errors';

const RESEND_SECONDS = 30;

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  protected mobile = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)],
  });
  protected code = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/^\d{6}$/)],
  });
  protected step = signal<'mobile' | 'otp'>('mobile');
  protected busy = signal(false);
  protected error = signal('');
  protected resendIn = signal(0);

  private otpInput = viewChild<ElementRef<HTMLInputElement>>('otpInput');
  private timer?: ReturnType<typeof setInterval>;

  constructor() {
    // Move focus to the OTP field as soon as it appears.
    afterRenderEffect(() => this.otpInput()?.nativeElement.focus());
    inject(DestroyRef).onDestroy(() => clearInterval(this.timer));
  }

  sendOtp() {
    if (this.mobile.invalid) {
      this.mobile.markAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.sendOtp(this.mobile.value).subscribe({
      next: () => {
        this.busy.set(false);
        this.step.set('otp');
        this.startCountdown();
      },
      error: (e) => this.fail(e),
    });
  }

  verify() {
    if (this.code.invalid) {
      this.code.markAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.verifyOtp(this.mobile.value, this.code.value).subscribe({
      next: (r) => {
        this.busy.set(false);
        if (r.status === 'LOGGED_IN') {
          this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') ?? '/app');
        } else {
          this.router.navigate(['/signup'], { state: { signupToken: r.signupToken } });
        }
      },
      error: (e) => this.fail(e),
    });
  }

  changeNumber() {
    clearInterval(this.timer);
    this.code.reset();
    this.error.set('');
    this.step.set('mobile');
  }

  private startCountdown() {
    clearInterval(this.timer);
    this.resendIn.set(RESEND_SECONDS);
    this.timer = setInterval(() => {
      this.resendIn.update((s) => s - 1);
      if (this.resendIn() <= 0) clearInterval(this.timer);
    }, 1000);
  }

  private fail(e: unknown) {
    this.busy.set(false);
    this.error.set(errorMessage(e));
  }
}
