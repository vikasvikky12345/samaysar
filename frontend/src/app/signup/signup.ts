import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { signupToken } from '../core/auth/auth.guards';
import { AuthService } from '../core/auth/auth.service';
import { errorMessage } from '../core/errors';

@Component({
  selector: 'app-signup',
  imports: [ReactiveFormsModule],
  templateUrl: './signup.html',
})
export class Signup {
  private auth = inject(AuthService);
  private router = inject(Router);
  // signupGuard guarantees the token is present while this navigation runs.
  private token = signupToken()!;

  protected form = inject(NonNullableFormBuilder).group({
    ownerName: ['', [Validators.required, Validators.maxLength(120)]],
    clinicName: ['', [Validators.required, Validators.maxLength(120)]],
    city: ['', [Validators.required, Validators.maxLength(80)]],
  });
  protected busy = signal(false);
  protected error = signal('');

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.auth.signup({ signupToken: this.token, ...this.form.getRawValue() }).subscribe({
      next: () => this.router.navigateByUrl('/app'),
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  protected showError(name: 'ownerName' | 'clinicName' | 'city') {
    const c = this.form.controls[name];
    return c.touched && c.invalid;
  }
}
