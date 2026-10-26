import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
})
export class Shell {
  private auth = inject(AuthService);
  private router = inject(Router);

  protected user = this.auth.user;
  protected isOwner = computed(() => this.user()?.role === 'OWNER');
  protected trialDaysLeft = computed(() => {
    const clinic = this.user()?.clinic;
    if (clinic?.planStatus !== 'TRIAL' || !clinic.trialEndsAt) return null;
    return Math.max(0, Math.ceil((Date.parse(clinic.trialEndsAt) - Date.now()) / 86_400_000));
  });

  logout() {
    // State is cleared immediately; the cookie revoke can finish (or fail) in the background.
    this.auth.logout().subscribe({ error: () => undefined });
    this.router.navigateByUrl('/login');
  }
}
