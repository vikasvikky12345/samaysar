import { Component, computed, inject } from '@angular/core';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-today',
  template: `
    <h1 class="text-2xl font-bold">{{ greeting }}, {{ name() }}</h1>
    <div class="card bg-base-100 border border-base-300 mt-4">
      <div class="card-body items-center text-center">
        <h2 class="card-title">No queue yet</h2>
        <p>Queue coming in Phase 3.</p>
      </div>
    </div>
  `,
})
export class Today {
  private user = inject(AuthService).user;
  protected name = computed(() => this.user()?.name ?? '');
  protected greeting = greetingFor(new Date().getHours());
}

function greetingFor(hour: number) {
  return hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
}
