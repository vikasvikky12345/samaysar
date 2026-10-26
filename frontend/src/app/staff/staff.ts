import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Role } from '../core/auth/auth.service';
import { errorMessage } from '../core/errors';

interface StaffMember {
  id: string;
  name: string;
  mobile: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-staff',
  imports: [ReactiveFormsModule],
  templateUrl: './staff.html',
})
export class Staff {
  private http = inject(HttpClient);

  protected staff = signal<StaffMember[]>([]);
  protected error = signal('');
  protected busy = signal(false);
  protected form = inject(NonNullableFormBuilder).group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    mobile: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
    role: ['RECEPTION' as 'DOCTOR' | 'RECEPTION', Validators.required],
  });

  constructor() {
    this.http.get<StaffMember[]>('/api/staff').subscribe({
      next: (list) => this.staff.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  add() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.http.post<StaffMember>('/api/staff', this.form.getRawValue()).subscribe({
      next: (s) => {
        this.busy.set(false);
        this.staff.update((list) => [...list, s]);
        this.form.reset();
      },
      error: (e: HttpErrorResponse) => {
        this.busy.set(false);
        if (e.error?.code === 'mobile_already_registered') {
          this.form.controls.mobile.setErrors({ taken: true });
        } else {
          this.error.set(errorMessage(e));
        }
      },
    });
  }

  /** On failure the control is put back, since its DOM state already changed. */
  update(s: StaffMember, change: Partial<Pick<StaffMember, 'active' | 'role'>>, control: HTMLInputElement | HTMLSelectElement) {
    this.error.set('');
    this.http.patch<StaffMember>(`/api/staff/${s.id}`, change).subscribe({
      next: (updated) => this.staff.update((list) => list.map((x) => (x.id === updated.id ? updated : x))),
      error: (e) => {
        if (control instanceof HTMLInputElement) control.checked = s.active;
        else control.value = s.role;
        this.error.set(errorMessage(e));
      },
    });
  }

  protected showError(name: 'name' | 'mobile') {
    const c = this.form.controls[name];
    return c.touched && c.invalid;
  }
}
