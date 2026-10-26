import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../core/auth/auth.service';
import { errorMessage } from '../core/errors';

interface Clinic {
  id: string;
  slug: string;
  name: string;
  address: string | null;
  city: string | null;
  phone: string | null;
  whatsapp: string | null;
  mapUrl: string | null;
  about: string | null;
  timezone: string;
  planStatus: string;
  trialEndsAt: string | null;
}

type Field = 'name' | 'address' | 'city' | 'phone' | 'whatsapp' | 'mapUrl' | 'about';

@Component({
  selector: 'app-clinic-settings',
  imports: [ReactiveFormsModule],
  templateUrl: './clinic-settings.html',
})
export class ClinicSettings {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  protected isOwner = this.auth.user()?.role === 'OWNER';
  protected form = inject(NonNullableFormBuilder).group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    address: ['', Validators.maxLength(300)],
    city: ['', Validators.maxLength(80)],
    phone: ['', Validators.pattern(/^\d{10}$/)],
    whatsapp: ['', Validators.pattern(/^\d{10}$/)],
    mapUrl: ['', Validators.pattern(/^https:\/\/\S+$/)],
    about: ['', Validators.maxLength(1000)],
  });
  protected loaded = signal(false);
  protected busy = signal(false);
  protected error = signal('');
  protected saved = signal(false);

  constructor() {
    if (!this.isOwner) this.form.disable();
    this.http.get<Clinic>('/api/clinic').subscribe({
      next: (c) => {
        this.fill(c);
        this.loaded.set(true);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.saved.set(false);
    // Blank optional fields are sent as null so the server clears them.
    const v = this.form.getRawValue();
    const body = Object.fromEntries(Object.entries(v).map(([k, x]) => [k, x.trim() || null]));
    this.http.put<Clinic>('/api/clinic', body).subscribe({
      next: (c) => {
        this.fill(c);
        this.busy.set(false);
        this.saved.set(true);
        this.auth.user.update((u) => u && { ...u, clinic: { ...u.clinic, name: c.name } });
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  protected showError(name: Field) {
    const c = this.form.controls[name];
    return c.touched && c.invalid;
  }

  private fill(c: Clinic) {
    this.form.reset({
      name: c.name,
      address: c.address ?? '',
      city: c.city ?? '',
      // Accept either 10 digits or E.164 from the server; the form edits 10 digits.
      phone: c.phone?.replace(/^\+91/, '') ?? '',
      whatsapp: c.whatsapp?.replace(/^\+91/, '') ?? '',
      mapUrl: c.mapUrl ?? '',
      about: c.about ?? '',
    });
  }
}
