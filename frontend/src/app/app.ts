import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly colors = [
    { name: 'Primary', hex: '#0F766E', swatch: 'bg-primary' },
    { name: 'Accent', hex: '#F59E0B', swatch: 'bg-accent' },
    { name: 'Secondary', hex: '#CCFBF1', swatch: 'bg-secondary' },
    { name: 'Neutral', hex: '#1E293B', swatch: 'bg-neutral' },
    { name: 'Success', hex: '#15803D', swatch: 'bg-success' },
    { name: 'Error', hex: '#DC2626', swatch: 'bg-error' },
    { name: 'Info', hex: '#0369A1', swatch: 'bg-info' },
    { name: 'Base', hex: '#F8FAFC', swatch: 'bg-base-200' },
  ];
}
