import { Routes } from '@angular/router';
import { authGuard, ownerGuard, signupGuard } from './core/auth/auth.guards';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./login/login').then((m) => m.Login) },
  {
    path: 'signup',
    canActivate: [signupGuard],
    loadComponent: () => import('./signup/signup').then((m) => m.Signup),
  },
  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () => import('./shell/shell').then((m) => m.Shell),
    children: [
      { path: '', loadComponent: () => import('./today/today').then((m) => m.Today) },
      {
        path: 'settings/clinic',
        loadComponent: () => import('./clinic-settings/clinic-settings').then((m) => m.ClinicSettings),
      },
      {
        path: 'staff',
        canActivate: [ownerGuard],
        loadComponent: () => import('./staff/staff').then((m) => m.Staff),
      },
    ],
  },
  { path: '', pathMatch: 'full', redirectTo: 'app' },
  { path: '**', redirectTo: 'app' },
];
